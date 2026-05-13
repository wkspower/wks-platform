package com.wkspower.platform.api.dto.response;

import java.util.List;

/**
 * Story 13-1 AC1 — wire shape for {@code GET /api/tasks} (cross-case task list). Wraps the
 * filtered, RBAC-bounded list of pending BPMN user tasks across every case-type the caller can
 * view, with a {@code truncated} flag set when the server cap was reached.
 *
 * <p>Reuses the existing {@link TaskDto} projection (Story 2.8) so wire shape per row stays in
 * lockstep with {@code GET /api/cases/{id}/tasks}. The wrapper is the smallest contract that
 * carries the cap signal without minting a new error code or HTTP header — {@code truncated = true}
 * means "the server returned the oldest {@code items.size()} entries and there are more" (server
 * cap = 500 in Sprint 12; refined by Story 13-4 filters).
 *
 * @param items pending user-task projections, ordered by {@code createdAt ASC} with {@code caseId
 *     ASC} as tiebreak. Empty when the user has zero permitted tasks.
 * @param truncated {@code true} when the server cap was reached (more pending tasks exist than were
 *     returned); always {@code false} when {@code items.size() < cap}.
 */
public record CrossCaseTaskListDto(List<TaskDto> items, boolean truncated) {}
