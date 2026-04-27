package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.request.CompleteTaskRequest;
import com.wkspower.platform.api.dto.response.TaskActionResponse;
import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.model.Task;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.service.TaskService;
import com.wkspower.platform.security.CaseTypePermissionEvaluator;
import com.wkspower.platform.security.WksUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST surface for BPMN user-task lifecycle (Story 2.4 AC2 / AC3). Permission gating: load the task
 * first so unknown id surfaces 404, then check the {@code transition} verb against the task's
 * case-type. Engine-side conflicts (already-completed, claimed-by-other) are translated inside
 * {@code CibSevenWorkflowEngine} and surface as {@code WKS-RTM-409}.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskService taskService;
  private final CaseTypePermissionEvaluator evaluator;
  private final Clock clock;

  public TaskController(
      TaskService taskService, CaseTypePermissionEvaluator evaluator, Clock clock) {
    this.taskService = taskService;
    this.evaluator = evaluator;
    this.clock = clock;
  }

  @PostMapping("/{id}/complete")
  public ResponseEntity<ApiResponse<TaskActionResponse>> complete(
      @PathVariable("id") String id,
      @RequestBody(required = false) CompleteTaskRequest request,
      @AuthenticationPrincipal WksUserPrincipal actor) {
    Task task;
    try {
      task = taskService.findById(id);
    } catch (WksNotFoundException ex) {
      // The client thought this task was active when they clicked, but the engine no longer has
      // it — another action (typically another user / another tab) already completed it. AC5
      // requires this surface as 409 (conflict), not 404 (not found): from the client's point of
      // view the truth has moved on, and the [Refresh case] action is the right recovery.
      throw alreadyCompleted(id, ex);
    }
    requireVerb(actor, task.caseTypeId());
    Task completed;
    try {
      completed =
          taskService.complete(id, request == null ? null : request.variables(), actor.id());
    } catch (WksNotFoundException ex) {
      // Same TOCTOU window — task disappeared between findById and the engine call.
      throw alreadyCompleted(id, ex);
    }
    // Snapshot is taken before engine.complete, so processInstanceId is still populated. The
    // assignee surfaced on /complete is the actor (which the service has just verified) — null
    // would suggest the action was anonymous, which it never is.
    return ResponseEntity.ok(
        ApiResponse.success(
            new TaskActionResponse(
                completed.id(),
                completed.processInstanceId(),
                completed.caseId(),
                completed.archetype(),
                actor.id(),
                clock.now())));
  }

  @PostMapping("/{id}/claim")
  public ResponseEntity<ApiResponse<TaskActionResponse>> claim(
      @PathVariable("id") String id, @AuthenticationPrincipal WksUserPrincipal actor) {
    Task task = taskService.findById(id);
    requireVerb(actor, task.caseTypeId());
    Task claimed = taskService.claim(id, actor.id());
    return ResponseEntity.ok(
        ApiResponse.success(
            new TaskActionResponse(
                claimed.id(),
                claimed.processInstanceId(),
                claimed.caseId(),
                claimed.archetype(),
                claimed.assignee(),
                clock.now())));
  }

  /**
   * Translate "task is gone in the engine" into the AC5 conflict envelope. Phase 0 cannot
   * distinguish "task never existed" from "task already completed" at the engine layer — both paths
   * surface as the same conflict copy because that's the realistic root cause from a UI driven by
   * server-supplied task ids.
   */
  private static WksConflictException alreadyCompleted(String taskId, Throwable cause) {
    return new WksConflictException(
        "Task " + taskId + " was already completed. Refresh to see the latest case state.", cause);
  }

  private void requireVerb(WksUserPrincipal actor, String caseTypeId) {
    if (actor == null || !evaluator.hasVerb(actor.authenticated(), caseTypeId, "transition")) {
      throw new AccessDeniedException(
          "Forbidden: missing verb 'transition' on case type " + caseTypeId);
    }
  }
}
