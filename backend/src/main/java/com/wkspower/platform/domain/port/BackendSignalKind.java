package com.wkspower.platform.domain.port;

/**
 * The four kinds of {@link BackendSignal} a {@link BackendAdapter} may emit. Architecture §662 +
 * §810 (Decision 22).
 *
 * <p><b>Precedence ordering — highest to lowest:</b>
 *
 * <ol>
 *   <li>{@link #END_EVENT} — terminal; supersedes all other signals for the same instance.
 *   <li>{@link #NAMED_SIGNAL} — explicit signal/message correlation emitted by the backend.
 *   <li>{@link #USER_TASK_PROPERTY} — user-task-scoped property change (e.g. {@code
 *       <camunda:property>} on task complete).
 *   <li>{@link #OUTCOME} — backend-internal outcome event.
 * </ol>
 *
 * <p>This generalises the "endEvent wins" rule documented at architecture §810 to all signal kinds.
 * Story 4.3 ({@code BackendSignalRouter}) is the enforcer; this enum only documents the ordering so
 * adapters and tests share one source of truth.
 */
public enum BackendSignalKind {

  /** Terminal end event — wins over all other signals for the same instance. Highest precedence. */
  END_EVENT,

  /** Named signal/message correlation emitted by the backend. */
  NAMED_SIGNAL,

  /** User-task-scoped property change (e.g. status property set on task complete). */
  USER_TASK_PROPERTY,

  /** Backend-internal outcome event. Lowest precedence. */
  OUTCOME
}
