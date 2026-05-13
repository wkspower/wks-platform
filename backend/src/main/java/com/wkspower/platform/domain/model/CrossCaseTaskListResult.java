package com.wkspower.platform.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Story 13-1 AC1 — domain result of {@code WorkflowEngine.listPendingTasks(...)}. Carries the
 * ordered task list together with a {@code truncated} flag indicating whether the requested cap
 * was reached. The API layer projects this into {@code CrossCaseTaskListDto} without
 * re-interpreting the flag.
 *
 * @param tasks pending tasks ordered by {@code createdAt ASC, caseId ASC}; size never exceeds the
 *     cap passed to {@code listPendingTasks}.
 * @param truncated {@code true} when more pending tasks exist than were returned (the engine
 *     produced strictly more than {@code limit} matching rows). Always {@code false} when {@code
 *     tasks.size() < limit}.
 */
public record CrossCaseTaskListResult(List<Task> tasks, boolean truncated) {
  public CrossCaseTaskListResult {
    Objects.requireNonNull(tasks, "tasks");
    tasks = List.copyOf(tasks);
  }

  public static CrossCaseTaskListResult empty() {
    return new CrossCaseTaskListResult(List.of(), false);
  }
}
