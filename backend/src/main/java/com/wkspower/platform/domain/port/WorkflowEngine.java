package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.model.CrossCaseTaskListResult;
import com.wkspower.platform.domain.model.Task;
import com.wkspower.platform.domain.workflow.DeploymentInfo;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Outbound port for the embedded BPMN engine.
 *
 * <ul>
 *   <li>Story 2.2 surface — {@link #deploy(DeploymentRequest)} and {@link
 *       #latestDeployment(String)}.
 *   <li>Story 2.3 surface — {@link #startProcessInstance(String, Map)}.
 *   <li>Story 2.4 surface — {@link #findTask(String)}, {@link #completeTask(String, Map)}, {@link
 *       #claimTask(String, UUID)}, {@link #signalTransition(String, String, Map)} for case status
 *       transitions and user-task lifecycle.
 * </ul>
 *
 * <p>Implementations live in {@code engine/} only.
 */
public interface WorkflowEngine {

  /**
   * Deploy a single BPMN definition. Returns the engine-assigned deployment id. Implementations
   * MUST be idempotent on identical content (deploying the same bytes twice returns the original
   * deployment id rather than producing a new version).
   */
  DeploymentResult deploy(DeploymentRequest request);

  /**
   * Snapshot of the latest deployment for a process definition key, or {@link Optional#empty()} if
   * the key has never been deployed.
   */
  Optional<DeploymentInfo> latestDeployment(String processDefinitionKey);

  /**
   * Start a process instance for the given deployed process definition. {@code variables} are
   * passed to the engine as initial process variables; pass simple scalars only ({@code String},
   * {@code UUID.toString()}, {@code Long}). Returns the engine-assigned process instance id.
   *
   * <p>Implementations MUST translate engine-side failures (unknown key, JUEL evaluation errors)
   * into {@link com.wkspower.platform.domain.exception.WksWorkflowEngineException}.
   */
  String startProcessInstance(String processDefinitionKey, Map<String, Object> variables);

  /**
   * Lookup a single user task by engine-assigned id. Returns {@link Optional#empty()} when the task
   * does not exist (already completed, or never existed); the calling service translates that to
   * {@link com.wkspower.platform.domain.exception.WksNotFoundException}. Implementations MUST
   * translate engine exceptions other than not-found into {@link
   * com.wkspower.platform.domain.exception.WksWorkflowEngineException}.
   */
  Optional<Task> findTask(String taskId);

  /**
   * List active (uncompleted, unsuspended) user tasks for a case, ordered by engine create time.
   * Story 2.8 AC1 — backs {@code GET /api/cases/{id}/tasks}. Returns an empty list when the case
   * has reached a terminal end-event (no active tasks); callers MUST NOT translate empty to 404.
   *
   * <p>Implementations MUST translate engine exceptions into {@link
   * com.wkspower.platform.domain.exception.WksWorkflowEngineException}.
   */
  List<Task> findTasksByCase(UUID caseId);

  /**
   * Story 13-1 AC1 — list pending (active, uncompleted) BPMN user tasks across every case whose
   * case-type id is in {@code permittedCaseTypeIds}, ordered by engine {@code createdAt ASC} with a
   * stable {@code caseId ASC} tiebreak. Returns at most {@code limit} tasks; the {@link
   * CrossCaseTaskListResult#truncated()} flag is {@code true} when the engine had strictly more
   * matching rows than {@code limit}.
   *
   * <p>RBAC is the caller's responsibility — the engine knows nothing about user roles. The
   * controller computes the permitted case-type set via {@code CaseTypePermissionEvaluator} and
   * passes it here so the engine query can pre-filter (no post-pagination filter holes). An empty
   * permitted set short-circuits to {@link CrossCaseTaskListResult#empty()}.
   *
   * <p>Default implementation returns empty — concrete adapters override. Default makes in-memory
   * stubs in tests opt-in to the new surface without forcing them to implement engine paginated
   * reads they do not exercise.
   *
   * <p>Implementations MUST translate engine exceptions into {@link
   * com.wkspower.platform.domain.exception.WksWorkflowEngineException}.
   */
  default CrossCaseTaskListResult listPendingTasks(Set<String> permittedCaseTypeIds, int limit) {
    return CrossCaseTaskListResult.empty();
  }

  /**
   * Read the {@code actionLabel} {@code camunda:property} for a user task definition, falling back
   * to the {@code userTask.name} when the property is absent or blank. Returns {@code null} only
   * when the BPMN cannot be located (defensive — should not happen for an active task). Story 2.8
   * AC1 surfaces this on {@code TaskDto.actionLabel}; the domain {@link Task} model intentionally
   * does not carry it (lean domain).
   */
  String readActionLabel(String processDefinitionId, String taskDefinitionKey);

  /**
   * Complete a user task. {@code variables} are merged into process variables — pass simple scalars
   * only. Implementations MUST translate already-completed / wrong-assignee / optimistic lock
   * failures into {@link com.wkspower.platform.domain.exception.WksConflictException} and
   * unknown-task into {@link com.wkspower.platform.domain.exception.WksNotFoundException}.
   */
  void completeTask(String taskId, Map<String, Object> variables);

  /**
   * Claim an unassigned user task for {@code userId}. Implementations MUST translate
   * already-claimed into {@link com.wkspower.platform.domain.exception.WksConflictException} (with
   * a message identifying the current assignee) and unknown-task into {@link
   * com.wkspower.platform.domain.exception.WksNotFoundException}.
   */
  void claimTask(String taskId, UUID userId);

  /**
   * Advance a process instance via message correlation (Story 2.4 Phase 0 supports message
   * correlation only — see Dev Notes §Transition dispatch). The {@code action} string is the BPMN
   * message name. {@code variables} flow into the correlation as process variables.
   *
   * <p>Implementations MUST translate "no enabled receiver" / mismatching-correlation into {@link
   * com.wkspower.platform.domain.exception.WksConflictException}.
   */
  void signalTransition(String processInstanceId, String action, Map<String, Object> variables);
}
