package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.exception.WksValidationException;
import com.wkspower.platform.domain.model.Task;
import com.wkspower.platform.domain.port.WorkflowEngine;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Domain service for BPMN user-task lifecycle (Story 2.4 AC6). Framework-free — collaborators are
 * reached via the {@link WorkflowEngine} port; engine-side exception translation lives inside the
 * adapter so this service sees only the {@code Wks*Exception} hierarchy.
 *
 * <p>The service intentionally does not gate permissions — that is the controller's job (verb gate
 * via {@code CaseTypePermissionEvaluator}). Keeping authz at the edge keeps the service usable from
 * background jobs and tests without a security context.
 */
public class TaskService {

  /**
   * Engine-managed process variables that callers MUST NOT inject via {@code variables}. Allowing
   * client-supplied {@code caseId} would let the {@code CaseStatusListener} write the status of a
   * different case (Story 2.4 review — variable injection cross-case corruption).
   */
  static final Set<String> RESERVED_PROCESS_VARIABLES =
      Set.of("caseId", "caseTypeId", "taskAssignee", "caseStatus");

  private final WorkflowEngine workflowEngine;

  public TaskService(WorkflowEngine workflowEngine) {
    this.workflowEngine = Objects.requireNonNull(workflowEngine, "workflowEngine");
  }

  /**
   * List active user tasks for a case (Story 2.8 AC1). Empty list when the case is at a terminal
   * end-event or no active tasks remain — callers MUST NOT translate empty to 404.
   */
  public List<Task> findByCase(UUID caseId) {
    Objects.requireNonNull(caseId, "caseId");
    return workflowEngine.findTasksByCase(caseId);
  }

  /**
   * Read the {@code actionLabel} for a task definition (Story 2.8 AC1). Falls back to the
   * userTask's {@code name} attribute when the property is absent.
   */
  public String readActionLabel(String processDefinitionId, String taskDefinitionKey) {
    return workflowEngine.readActionLabel(processDefinitionId, taskDefinitionKey);
  }

  /** Lookup a task by id; 404 if unknown. */
  public Task findById(String taskId) {
    Objects.requireNonNull(taskId, "taskId");
    return workflowEngine
        .findTask(taskId)
        .orElseThrow(() -> new WksNotFoundException("Task " + taskId + " not found"));
  }

  /**
   * Complete a user task and return the {@link Task} snapshot taken before the engine completed it
   * (so callers — i.e. the controller — can read the {@code archetype} and {@code caseId} for the
   * response without a follow-up query that the engine would now answer with not-found). The
   * post-completion {@code cases.status} update fires asynchronously to this call from the {@code
   * CaseStatusListener} engine-callback (AC5).
   */
  public Task complete(String taskId, Map<String, Object> variables, UUID actorId) {
    Objects.requireNonNull(taskId, "taskId");
    Objects.requireNonNull(actorId, "actorId");
    Map<String, Object> safeVars = sanitiseVariables(variables);
    Task snapshot =
        workflowEngine
            .findTask(taskId)
            .orElseThrow(() -> new WksNotFoundException("Task " + taskId + " not found"));
    // AC2 — caller is the assignee, OR caller holds the verb (controller's job) AND the task is
    // unassigned. The controller already enforces the verb; this layer enforces the assignee
    // contract so a caller with the verb cannot complete a task assigned to someone else.
    UUID assignee = snapshot.assignee();
    if (assignee != null && !assignee.equals(actorId)) {
      throw new WksConflictException(
          "Task " + taskId + " is assigned to another user; complete denied");
    }
    workflowEngine.completeTask(taskId, safeVars);
    return snapshot;
  }

  /**
   * Reject client-supplied keys that the engine treats as system variables. Without this guard, a
   * caller could pass {@code "caseId":"<other-uuid>"} and the {@code CaseStatusListener} would
   * update the wrong case's status row on the next transition (Story 2.4 review — critical).
   */
  static Map<String, Object> sanitiseVariables(Map<String, Object> variables) {
    if (variables == null || variables.isEmpty()) {
      return Map.of();
    }
    for (String key : variables.keySet()) {
      if (RESERVED_PROCESS_VARIABLES.contains(key)) {
        throw new WksValidationException(
            ErrorCode.WKS_API_001,
            "Process variable '" + key + "' is reserved and cannot be supplied by the client",
            "variables." + key);
      }
    }
    return Map.copyOf(variables);
  }

  /**
   * Claim an unassigned user task. Returns the post-claim {@link Task} snapshot — re-fetched after
   * the claim so {@link Task#assignee()} reflects the new owner.
   */
  public Task claim(String taskId, UUID actorId) {
    Objects.requireNonNull(taskId, "taskId");
    Objects.requireNonNull(actorId, "actorId");
    workflowEngine.claimTask(taskId, actorId);
    return workflowEngine
        .findTask(taskId)
        .orElseThrow(
            () -> new WksNotFoundException("Task " + taskId + " not found after claim — race?"));
  }
}
