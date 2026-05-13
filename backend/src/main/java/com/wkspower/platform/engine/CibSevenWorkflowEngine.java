package com.wkspower.platform.engine;

import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.exception.WksWorkflowEngineException;
import com.wkspower.platform.domain.model.CrossCaseTaskListResult;
import com.wkspower.platform.domain.model.Task;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.workflow.DeploymentInfo;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import com.wkspower.platform.engine.properties.CamundaPropertyReader;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.cibseven.bpm.engine.MismatchingMessageCorrelationException;
import org.cibseven.bpm.engine.OptimisticLockingException;
import org.cibseven.bpm.engine.ProcessEngineException;
import org.cibseven.bpm.engine.RepositoryService;
import org.cibseven.bpm.engine.RuntimeService;
import org.cibseven.bpm.engine.TaskAlreadyClaimedException;
import org.cibseven.bpm.engine.TaskService;
import org.cibseven.bpm.engine.exception.NullValueException;
import org.cibseven.bpm.engine.repository.Deployment;
import org.cibseven.bpm.engine.repository.ProcessDefinition;
import org.cibseven.bpm.engine.runtime.ProcessInstance;
import org.cibseven.bpm.model.bpmn.BpmnModelInstance;
import org.cibseven.bpm.model.bpmn.instance.UserTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CIB seven adapter for the {@link WorkflowEngine} port. Sole class outside the test tree allowed
 * to import {@code org.cibseven.*} (enforced by {@code
 * ArchitectureTest.onlyEngineAdapterImportsTheBpmnEngineSdk}).
 *
 * <p>{@code enableDuplicateFiltering(true)} makes redeploy of an unchanged BPMN a no-op — CIB seven
 * hashes the resource bytes. This is the engine-side counterpart to the case-type registry's
 * idempotent same-version path.
 *
 * <p>Story 2.4 expands the surface with task lifecycle ({@link #findTask}, {@link #completeTask},
 * {@link #claimTask}) and message-correlation transitions ({@link #signalTransition}). Engine-side
 * exceptions translate inside this adapter so domain/api code never sees an {@code org.cibseven.*}
 * type.
 */
@Component
public class CibSevenWorkflowEngine implements WorkflowEngine {

  private static final Logger log = LoggerFactory.getLogger(CibSevenWorkflowEngine.class);

  /** Soft SLA — Story 2.2 AC3 sets the test ceiling at 3000 ms; 2000 ms warns early. */
  private static final long DEPLOY_WARN_THRESHOLD_MS = 2000L;

  private final RepositoryService repositoryService;
  private final RuntimeService runtimeService;
  private final TaskService taskService;

  public CibSevenWorkflowEngine(
      RepositoryService repositoryService, RuntimeService runtimeService, TaskService taskService) {
    this.repositoryService = repositoryService;
    this.runtimeService = runtimeService;
    this.taskService = taskService;
  }

  @Override
  public DeploymentResult deploy(DeploymentRequest request) {
    Instant start = Instant.now();
    String resourceName = request.processDefinitionKey() + ".bpmn";
    Deployment deployment;
    try {
      deployment =
          repositoryService
              .createDeployment()
              .name(request.name())
              .addInputStream(resourceName, new ByteArrayInputStream(request.bpmnXml()))
              .enableDuplicateFiltering(true)
              .deploy();
    } catch (RuntimeException ex) {
      throw new WksWorkflowEngineException(
          "CIB seven deploy failed for processDefinitionKey="
              + request.processDefinitionKey()
              + " (caseType="
              + request.caseTypeId()
              + " v"
              + request.caseTypeVersion()
              + ")",
          ex);
    }

    ProcessDefinition definition;
    try {
      definition =
          repositoryService
              .createProcessDefinitionQuery()
              .deploymentId(deployment.getId())
              .processDefinitionKey(request.processDefinitionKey())
              .singleResult();
    } catch (RuntimeException ex) {
      throw new WksWorkflowEngineException(
          "CIB seven post-deploy lookup failed for deploymentId=" + deployment.getId(), ex);
    }
    if (definition == null) {
      throw new WksWorkflowEngineException(
          "CIB seven returned no ProcessDefinition for deploymentId="
              + deployment.getId()
              + " key="
              + request.processDefinitionKey());
    }

    Duration elapsed = Duration.between(start, Instant.now());
    if (elapsed.toMillis() > DEPLOY_WARN_THRESHOLD_MS) {
      log.warn(
          "WKS engine deploy slow: key={} elapsedMs={} (threshold {} ms — Story 2.2 AC3)",
          request.processDefinitionKey(),
          elapsed.toMillis(),
          DEPLOY_WARN_THRESHOLD_MS);
    }

    if (deployment.getDeploymentTime() == null) {
      throw new WksWorkflowEngineException(
          "CIB seven deployment row has null deploymentTime for deploymentId="
              + deployment.getId()
              + " key="
              + request.processDefinitionKey());
    }
    return new DeploymentResult(
        deployment.getId(),
        definition.getKey(),
        definition.getId(),
        definition.getVersion(),
        deployment.getDeploymentTime().toInstant());
  }

  @Override
  public Optional<DeploymentInfo> latestDeployment(String processDefinitionKey) {
    ProcessDefinition definition;
    try {
      definition =
          repositoryService
              .createProcessDefinitionQuery()
              .processDefinitionKey(processDefinitionKey)
              .latestVersion()
              .singleResult();
    } catch (RuntimeException ex) {
      throw new WksWorkflowEngineException(
          "CIB seven query failed for processDefinitionKey=" + processDefinitionKey, ex);
    }
    if (definition == null) {
      return Optional.empty();
    }

    Deployment deployment;
    try {
      deployment =
          repositoryService
              .createDeploymentQuery()
              .deploymentId(definition.getDeploymentId())
              .singleResult();
    } catch (RuntimeException ex) {
      throw new WksWorkflowEngineException(
          "CIB seven deployment lookup failed for deploymentId=" + definition.getDeploymentId(),
          ex);
    }

    if (deployment == null || deployment.getDeploymentTime() == null) {
      // CIB seven returned a process-definition row but no matching deployment row (or one with
      // no deployment time). Treat as "not yet observable" rather than synthesise a sentinel.
      return Optional.empty();
    }
    return Optional.of(
        new DeploymentInfo(
            definition.getDeploymentId(),
            definition.getId(),
            definition.getVersion(),
            deployment.getDeploymentTime().toInstant()));
  }

  @Override
  public String startProcessInstance(String processDefinitionKey, Map<String, Object> variables) {
    Objects.requireNonNull(processDefinitionKey, "processDefinitionKey");
    Map<String, Object> safeVariables = variables == null ? Map.of() : Map.copyOf(variables);
    ProcessInstance instance;
    try {
      instance = runtimeService.startProcessInstanceByKey(processDefinitionKey, safeVariables);
    } catch (ProcessEngineException ex) {
      throw new WksWorkflowEngineException(
          "CIB seven startProcessInstance failed for processDefinitionKey=" + processDefinitionKey,
          ex);
    }
    if (instance == null || instance.getId() == null) {
      throw new WksWorkflowEngineException(
          "CIB seven returned no ProcessInstance for processDefinitionKey=" + processDefinitionKey);
    }
    return instance.getId();
  }

  @Override
  public Optional<Task> findTask(String taskId) {
    Objects.requireNonNull(taskId, "taskId");
    org.cibseven.bpm.engine.task.Task engineTask;
    try {
      engineTask = taskService.createTaskQuery().taskId(taskId).singleResult();
    } catch (ProcessEngineException ex) {
      throw new WksWorkflowEngineException("CIB seven task query failed for taskId=" + taskId, ex);
    }
    if (engineTask == null) {
      return Optional.empty();
    }

    Map<String, Object> processVars;
    try {
      processVars = runtimeService.getVariables(engineTask.getProcessInstanceId());
    } catch (NullValueException ex) {
      // Process instance completed between the task query and the variable read — surface as
      // not-found rather than 500 (Story 2.4 review — findTask TOCTOU).
      return Optional.empty();
    } catch (ProcessEngineException ex) {
      String msg = ex.getMessage() == null ? "" : ex.getMessage();
      if (msg.contains("execution") && msg.contains("doesn't exist")) {
        // Defensive: some CIB seven paths surface execution-gone as ProcessEngineException without
        // a NullValueException subtype.
        return Optional.empty();
      }
      throw new WksWorkflowEngineException(
          "CIB seven process-variable lookup failed for taskId=" + taskId, ex);
    }
    UUID caseId = parseCaseId(processVars.get("caseId"), taskId);
    String caseTypeId = (String) processVars.get("caseTypeId");
    if (caseTypeId == null) {
      throw new WksWorkflowEngineException(
          "Process instance "
              + engineTask.getProcessInstanceId()
              + " is missing the 'caseTypeId' variable expected on every WKS process — task="
              + taskId);
    }

    String archetype =
        readArchetype(engineTask.getProcessDefinitionId(), engineTask.getTaskDefinitionKey());
    UUID assignee = parseAssignee(engineTask.getAssignee());

    Instant createdAt =
        engineTask.getCreateTime() == null ? Instant.EPOCH : engineTask.getCreateTime().toInstant();
    Instant dueAt = engineTask.getDueDate() == null ? null : engineTask.getDueDate().toInstant();

    return Optional.of(
        new Task(
            engineTask.getId(),
            engineTask.getProcessInstanceId(),
            engineTask.getProcessDefinitionId(),
            caseId,
            caseTypeId,
            engineTask.getTaskDefinitionKey(),
            engineTask.getName(),
            assignee,
            archetype,
            createdAt,
            dueAt));
  }

  @Override
  public List<Task> findTasksByCase(UUID caseId) {
    Objects.requireNonNull(caseId, "caseId");
    List<org.cibseven.bpm.engine.task.Task> engineTasks;
    try {
      engineTasks =
          taskService
              .createTaskQuery()
              .processVariableValueEquals("caseId", caseId.toString())
              .active()
              .orderByTaskCreateTime()
              .asc()
              .list();
    } catch (ProcessEngineException ex) {
      throw new WksWorkflowEngineException("CIB seven task query failed for caseId=" + caseId, ex);
    }
    if (engineTasks.isEmpty()) {
      return List.of();
    }
    // Cache caseTypeId by processInstanceId — multiple pending tasks on the same process instance
    // share variables, so a single getVariables call serves them all.
    Map<String, String> caseTypeByProcessInstance = new java.util.HashMap<>();
    java.util.ArrayList<Task> out = new java.util.ArrayList<>(engineTasks.size());
    for (org.cibseven.bpm.engine.task.Task engineTask : engineTasks) {
      String pi = engineTask.getProcessInstanceId();
      String caseTypeId =
          caseTypeByProcessInstance.computeIfAbsent(pi, k -> readCaseTypeId(engineTask, caseId));
      if (caseTypeId == null) {
        // Process instance terminated between query and variable read — drop the task rather than
        // 500 the whole list.
        continue;
      }
      String archetype =
          readArchetype(engineTask.getProcessDefinitionId(), engineTask.getTaskDefinitionKey());
      UUID assignee = parseAssignee(engineTask.getAssignee());
      Instant createdAt =
          engineTask.getCreateTime() == null
              ? Instant.EPOCH
              : engineTask.getCreateTime().toInstant();
      Instant dueAt = engineTask.getDueDate() == null ? null : engineTask.getDueDate().toInstant();
      out.add(
          new Task(
              engineTask.getId(),
              engineTask.getProcessInstanceId(),
              engineTask.getProcessDefinitionId(),
              caseId,
              caseTypeId,
              engineTask.getTaskDefinitionKey(),
              engineTask.getName(),
              assignee,
              archetype,
              createdAt,
              dueAt));
    }
    return List.copyOf(out);
  }

  @Override
  public CrossCaseTaskListResult listPendingTasks(Set<String> permittedCaseTypeIds, int limit) {
    Objects.requireNonNull(permittedCaseTypeIds, "permittedCaseTypeIds");
    if (limit <= 0 || permittedCaseTypeIds.isEmpty()) {
      return CrossCaseTaskListResult.empty();
    }
    // Sprint 12 demo scale: fetch all active tasks ordered by createTime ASC and filter
    // client-side by caseTypeId process variable membership. CIB seven's TaskQuery does not
    // expose a stable {@code processVariableValueIn} surface in this SDK version, and we own the
    // {@code caseTypeId} convention so reading from process variables is the source of truth.
    // Story 13-4 introduces filtering at the query layer when scale demands it.
    List<org.cibseven.bpm.engine.task.Task> engineTasks;
    try {
      engineTasks = taskService.createTaskQuery().active().orderByTaskCreateTime().asc().list();
    } catch (ProcessEngineException ex) {
      throw new WksWorkflowEngineException("CIB seven cross-case task query failed", ex);
    }
    if (engineTasks.isEmpty()) {
      return CrossCaseTaskListResult.empty();
    }
    // Cache (caseId, caseTypeId) by processInstanceId — multiple pending tasks on the same
    // process instance share variables, so a single getVariables call serves them all.
    Map<String, java.util.Map.Entry<UUID, String>> piVarsCache = new java.util.HashMap<>();
    java.util.ArrayList<Task> matched = new java.util.ArrayList<>();
    for (org.cibseven.bpm.engine.task.Task engineTask : engineTasks) {
      String pi = engineTask.getProcessInstanceId();
      java.util.Map.Entry<UUID, String> piVars = piVarsCache.get(pi);
      if (piVars == null) {
        piVars = readPiVars(engineTask);
        if (piVars == null) {
          // Process instance terminated between query and variable read — drop the task rather
          // than 500 the whole list. Matches the existing findTasksByCase defensive path.
          piVarsCache.put(pi, MISSING_PI_SENTINEL);
          continue;
        }
        piVarsCache.put(pi, piVars);
      } else if (piVars == MISSING_PI_SENTINEL) {
        continue;
      }
      String caseTypeId = piVars.getValue();
      if (!permittedCaseTypeIds.contains(caseTypeId)) {
        continue;
      }
      UUID caseId = piVars.getKey();
      String archetype =
          readArchetype(engineTask.getProcessDefinitionId(), engineTask.getTaskDefinitionKey());
      UUID assignee = parseAssignee(engineTask.getAssignee());
      Instant createdAt =
          engineTask.getCreateTime() == null
              ? Instant.EPOCH
              : engineTask.getCreateTime().toInstant();
      Instant dueAt = engineTask.getDueDate() == null ? null : engineTask.getDueDate().toInstant();
      matched.add(
          new Task(
              engineTask.getId(),
              engineTask.getProcessInstanceId(),
              engineTask.getProcessDefinitionId(),
              caseId,
              caseTypeId,
              engineTask.getTaskDefinitionKey(),
              engineTask.getName(),
              assignee,
              archetype,
              createdAt,
              dueAt));
    }
    // Stable tiebreak by caseId — engine ordering is createTime ASC at millisecond resolution; on
    // a tie we want a deterministic order so paging is stable across reads.
    matched.sort(
        java.util.Comparator.comparing(Task::createdAt).thenComparing(t -> t.caseId().toString()));
    boolean truncated = matched.size() > limit;
    List<Task> capped = truncated ? matched.subList(0, limit) : matched;
    return new CrossCaseTaskListResult(List.copyOf(capped), truncated);
  }

  /**
   * Sentinel entry stored in the per-call process-instance variable cache when a process instance
   * has terminated between the task-list query and the variable read. Subsequent tasks on the same
   * process instance can short-circuit without re-issuing the failing {@code getVariables} call.
   */
  private static final java.util.Map.Entry<UUID, String> MISSING_PI_SENTINEL =
      java.util.Map.entry(new UUID(0L, 0L), "");

  private java.util.Map.Entry<UUID, String> readPiVars(
      org.cibseven.bpm.engine.task.Task engineTask) {
    Map<String, Object> processVars;
    try {
      processVars = runtimeService.getVariables(engineTask.getProcessInstanceId());
    } catch (NullValueException ex) {
      return null;
    } catch (ProcessEngineException ex) {
      String msg = ex.getMessage() == null ? "" : ex.getMessage();
      if (msg.contains("execution") && msg.contains("doesn't exist")) {
        return null;
      }
      throw new WksWorkflowEngineException(
          "CIB seven process-variable lookup failed for taskId=" + engineTask.getId(), ex);
    }
    Object caseIdRaw = processVars.get("caseId");
    Object caseTypeIdRaw = processVars.get("caseTypeId");
    if (caseIdRaw == null || caseTypeIdRaw == null) {
      throw new WksWorkflowEngineException(
          "Process instance "
              + engineTask.getProcessInstanceId()
              + " is missing 'caseId'/'caseTypeId' variables expected on every WKS process — task="
              + engineTask.getId());
    }
    UUID caseId;
    try {
      caseId = UUID.fromString(caseIdRaw.toString());
    } catch (IllegalArgumentException ex) {
      throw new WksWorkflowEngineException(
          "Process variable 'caseId' is not a UUID for task="
              + engineTask.getId()
              + " value="
              + caseIdRaw,
          ex);
    }
    return java.util.Map.entry(caseId, caseTypeIdRaw.toString());
  }

  @Override
  public String readActionLabel(String processDefinitionId, String taskDefinitionKey) {
    if (processDefinitionId == null || taskDefinitionKey == null) {
      return null;
    }
    BpmnModelInstance model;
    try {
      model = repositoryService.getBpmnModelInstance(processDefinitionId);
    } catch (ProcessEngineException ex) {
      throw new WksWorkflowEngineException(
          "CIB seven BPMN model lookup failed for processDefinitionId=" + processDefinitionId, ex);
    }
    if (model == null) {
      return null;
    }
    var element = model.getModelElementById(taskDefinitionKey);
    if (!(element instanceof UserTask userTask)) {
      return null;
    }
    String fromProperty = readUserTaskProperty(userTask, "actionLabel");
    if (fromProperty != null && !fromProperty.isBlank()) {
      return fromProperty;
    }
    String name = userTask.getName();
    return (name == null || name.isBlank()) ? null : name;
  }

  private String readCaseTypeId(org.cibseven.bpm.engine.task.Task engineTask, UUID caseId) {
    Map<String, Object> processVars;
    try {
      processVars = runtimeService.getVariables(engineTask.getProcessInstanceId());
    } catch (NullValueException ex) {
      return null;
    } catch (ProcessEngineException ex) {
      String msg = ex.getMessage() == null ? "" : ex.getMessage();
      if (msg.contains("execution") && msg.contains("doesn't exist")) {
        return null;
      }
      throw new WksWorkflowEngineException(
          "CIB seven process-variable lookup failed for taskId=" + engineTask.getId(), ex);
    }
    String caseTypeId = (String) processVars.get("caseTypeId");
    if (caseTypeId == null) {
      throw new WksWorkflowEngineException(
          "Process instance "
              + engineTask.getProcessInstanceId()
              + " is missing the 'caseTypeId' variable expected on every WKS process — caseId="
              + caseId);
    }
    return caseTypeId;
  }

  @Override
  public void completeTask(String taskId, Map<String, Object> variables) {
    Objects.requireNonNull(taskId, "taskId");
    Map<String, Object> safeVars = variables == null ? Map.of() : Map.copyOf(variables);
    try {
      taskService.complete(taskId, safeVars);
    } catch (NullValueException ex) {
      // Task is gone between the load and the complete call — typically another action (another
      // tab, another user) already completed it. AC5 requires this surface as a conflict (the
      // truth has moved on), not 404 — the [Refresh case] recovery action depends on the 409
      // envelope.
      throw new WksConflictException(
          "Task " + taskId + " was already completed. Refresh to see the latest case state.", ex);
    } catch (TaskAlreadyClaimedException ex) {
      // Spec Task 2.2 names this exception explicitly. Surfaces when complete() runs against a
      // task whose assignee differs from the actor at the engine boundary (defence-in-depth on
      // top of the service-layer assignee check).
      throw new WksConflictException(
          "Task " + taskId + " is assigned to another user — complete denied", ex);
    } catch (OptimisticLockingException ex) {
      throw new WksConflictException(
          "Task " + taskId + " was modified by another transaction; reload and retry", ex);
    } catch (ProcessEngineException ex) {
      // ProcessEngineException with no specific subtype here is genuine engine failure (DB error,
      // expression evaluation, etc.) — surface as 500. The two known client-fixable conditions
      // (not-found, claimed-by-other) are caught above by their explicit subtypes.
      throw new WksWorkflowEngineException(
          "CIB seven completeTask failed for taskId=" + taskId, ex);
    }
  }

  @Override
  public void claimTask(String taskId, UUID userId) {
    Objects.requireNonNull(taskId, "taskId");
    Objects.requireNonNull(userId, "userId");
    try {
      taskService.claim(taskId, userId.toString());
    } catch (TaskAlreadyClaimedException ex) {
      throw new WksConflictException(
          "Task " + taskId + " is already claimed (current assignee follows engine state)", ex);
    } catch (NullValueException ex) {
      throw new WksNotFoundException("Task " + taskId + " not found");
    } catch (ProcessEngineException ex) {
      // Genuine engine failure (DB / persistence). Surface as 500 — the two client-fixable
      // conditions (not-found, already-claimed) are caught above by their explicit subtypes.
      throw new WksWorkflowEngineException("CIB seven claimTask failed for taskId=" + taskId, ex);
    }
  }

  @Override
  public void signalTransition(
      String processInstanceId, String action, Map<String, Object> variables) {
    Objects.requireNonNull(processInstanceId, "processInstanceId");
    Objects.requireNonNull(action, "action");
    Map<String, Object> safeVars = variables == null ? Map.of() : Map.copyOf(variables);
    try {
      runtimeService
          .createMessageCorrelation(action)
          .processInstanceId(processInstanceId)
          .setVariables(safeVars)
          .correlate();
    } catch (MismatchingMessageCorrelationException ex) {
      throw new WksConflictException(
          "No active receiver for action '" + action + "' on process instance " + processInstanceId,
          ex);
    } catch (OptimisticLockingException ex) {
      throw new WksConflictException(
          "Process instance "
              + processInstanceId
              + " was modified by another transaction; reload and retry",
          ex);
    } catch (ProcessEngineException ex) {
      // Genuine engine failure — DB/persistence/expression. Conflict-shaped errors are caught
      // above by their explicit subtypes (Story 2.4 review — narrow conflict mapping).
      throw new WksWorkflowEngineException(
          "CIB seven signalTransition failed for processInstanceId=" + processInstanceId, ex);
    }
  }

  /** Read the {@code archetype} camunda:property of a user task from the parsed BPMN model. */
  private String readArchetype(String processDefinitionId, String taskDefinitionKey) {
    if (processDefinitionId == null || taskDefinitionKey == null) {
      return null;
    }
    BpmnModelInstance model;
    try {
      model = repositoryService.getBpmnModelInstance(processDefinitionId);
    } catch (ProcessEngineException ex) {
      throw new WksWorkflowEngineException(
          "CIB seven BPMN model lookup failed for processDefinitionId=" + processDefinitionId, ex);
    }
    if (model == null) {
      return null;
    }
    var element = model.getModelElementById(taskDefinitionKey);
    if (!(element instanceof UserTask userTask)) {
      return null;
    }
    return readUserTaskProperty(userTask, "archetype");
  }

  /**
   * Read a single named {@code camunda:property} from a user task's extension elements. Story 4.4a
   * — delegates to {@link CamundaPropertyReader} (consolidation per {@code
   * feedback_consolidate_property_readers.md}).
   */
  private static String readUserTaskProperty(UserTask userTask, String name) {
    return CamundaPropertyReader.read(userTask, name);
  }

  private static UUID parseCaseId(Object raw, String taskId) {
    if (raw == null) {
      throw new WksWorkflowEngineException(
          "Process instance is missing the 'caseId' variable expected on every WKS process — task="
              + taskId);
    }
    try {
      return UUID.fromString(raw.toString());
    } catch (IllegalArgumentException ex) {
      throw new WksWorkflowEngineException(
          "Process variable 'caseId' is not a UUID for task=" + taskId + " value=" + raw, ex);
    }
  }

  private static UUID parseAssignee(String engineAssignee) {
    if (engineAssignee == null || engineAssignee.isBlank()) {
      return null;
    }
    try {
      return UUID.fromString(engineAssignee);
    } catch (IllegalArgumentException ex) {
      // Engine accepts arbitrary string assignees; non-UUID strings are pre-2.4 / external. Map to
      // null so the API surfaces "unassigned" rather than 500. Logged at debug to aid diagnosis.
      log.debug(
          "Task assignee '{}' is not a UUID; surfacing as unassigned in domain Task",
          engineAssignee);
      return null;
    }
  }
}
