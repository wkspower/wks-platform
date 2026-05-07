package com.wkspower.platform.domain.port;

/**
 * The four kinds of {@link ExecutionSignal} a {@link WorkflowAdapter} may emit. Architecture §662 +
 * §810 (Decision 22).
 *
 * <p><b>Precedence ordering — highest to lowest:</b>
 *
 * <ol>
 *   <li>{@link #STAGE_TRANSITION} — terminal; supersedes all other signals for the same instance.
 *   <li>{@link #NAMED_SIGNAL} — explicit signal/message correlation emitted by the backend.
 *   <li>{@link #TASK_STATUS_CHANGED} — user-task property change that drives a status transition.
 *   <li>{@link #TASK_COMPLETED} — user-task completion signal (e.g. BPMN {@code <camunda:property>}
 *       fired on task complete that maps to a stage advance / case-level outcome).
 *   <li>{@link #OUTCOME} — backend-internal outcome event.
 * </ol>
 *
 * <p>This generalises the "endEvent wins" rule documented at architecture §810 to all signal kinds.
 * Story 4.3 ({@code ExecutionSignalRouter}) is the enforcer; this enum only documents the ordering
 * so adapters and tests share one source of truth.
 *
 * <p>Story 4.3.1 AC10 / Option (a) — the legacy {@code USER_TASK_PROPERTY} value collapsed two
 * author-distinguishable shapes ({@code emits.type: status} vs {@code emits.type: task-complete})
 * onto the same enum constant. {@link com.wkspower.platform.infrastructure.config.MappingDiff}
 * silently misclassified mutations as APPEND because {@code .equals()} treated them as identical.
 * The split into {@link #TASK_STATUS_CHANGED} and {@link #TASK_COMPLETED} makes the type system the
 * enforcer — every callsite that branches on the user-task family must opt into a single value,
 * surfacing any future collapse to reviewers.
 */
public enum ExecutionSignalKind {

  /** Terminal end event — wins over all other signals for the same instance. Highest precedence. */
  STAGE_TRANSITION,

  /** Named signal/message correlation emitted by the backend. */
  NAMED_SIGNAL,

  /**
   * User-task-scoped status property change (YAML {@code emits.type: status}). Drives a status
   * transition within the active stage; never advances stages.
   */
  TASK_STATUS_CHANGED,

  /**
   * User-task-completion signal (YAML {@code emits.type: task-complete}). Fires on task complete
   * and may map to a stage advance via the surrounding rule. Distinct from {@link
   * #TASK_STATUS_CHANGED} so {@code MappingDiff} does not collapse the two shapes (Story 4.3.1
   * AC10).
   */
  TASK_COMPLETED,

  /** Backend-internal outcome event. Lowest precedence. */
  OUTCOME
}
