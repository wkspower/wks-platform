package com.wkspower.platform.api.controller;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.request.CompleteTaskRequest;
import com.wkspower.platform.api.dto.response.CrossCaseTaskListDto;
import com.wkspower.platform.api.dto.response.TaskActionResponse;
import com.wkspower.platform.api.dto.response.TaskDto;
import com.wkspower.platform.api.mapper.TaskDtoMapper;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.model.CrossCaseTaskListResult;
import com.wkspower.platform.domain.model.Task;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.service.TaskService;
import com.wkspower.platform.security.CaseTypePermissionEvaluator;
import com.wkspower.platform.security.WksUserPrincipal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

  /** Story 13-1 — server cap for {@code GET /api/tasks}. Refined by Story 13-4 filters. */
  static final int CROSS_CASE_TASK_LIMIT = 500;

  private final TaskService taskService;
  private final CaseTypePermissionEvaluator evaluator;
  private final CaseTypeReader caseTypeReader;
  private final Clock clock;

  public TaskController(
      TaskService taskService,
      CaseTypePermissionEvaluator evaluator,
      CaseTypeReader caseTypeReader,
      Clock clock) {
    this.taskService = taskService;
    this.evaluator = evaluator;
    this.caseTypeReader = caseTypeReader;
    this.clock = clock;
  }

  /**
   * Story 13-1 AC1 / AC4 — list pending BPMN user tasks across every case-type the caller can view.
   * Permission gate: the caller must hold the {@code view} verb on the case-type for any of its
   * tasks to appear; case-types with no view permission contribute zero rows regardless of the
   * engine state. Order is {@code createdAt ASC} (oldest first, with stable {@code caseId ASC}
   * tiebreak), capped at {@link #CROSS_CASE_TASK_LIMIT}. {@code truncated = true} signals the cap
   * was reached.
   */
  @GetMapping
  public ApiResponse<CrossCaseTaskListDto> listAcrossCases(
      @AuthenticationPrincipal WksUserPrincipal actor) {
    Set<String> permittedCaseTypeIds =
        caseTypeReader.all().stream()
            .map(CaseTypeConfig::id)
            .filter(id -> actor != null && evaluator.hasVerb(actor.authenticated(), id, "view"))
            .collect(Collectors.toUnmodifiableSet());
    CrossCaseTaskListResult result =
        taskService.listAcrossCases(permittedCaseTypeIds, CROSS_CASE_TASK_LIMIT);
    // Cross-case task list (Story 13-1) does not yet surface the open-form affordance — the
    // null formIdLookup yields formId=null per task, which the frontend renders as "no open-form
    // button". Whether 13-1 adopts formId is a deferred UX decision; until then this path stays
    // explicitly null rather than pretending no mapping exists.
    List<TaskDto> items =
        TaskDtoMapper.toDtos(result.tasks(), taskService::readActionLabel, (pdId, tdKey) -> null);
    return ApiResponse.success(new CrossCaseTaskListDto(items, result.truncated()));
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
