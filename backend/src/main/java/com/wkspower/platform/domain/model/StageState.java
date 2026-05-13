package com.wkspower.platform.domain.model;

/**
 * Lifecycle state of a {@link Stage} on a case. Closed enum — Phase-1 deferral on extra states
 * (e.g. {@code CANCELLED}, {@code ROLLED_BACK}) per Story 3.1 AC2.
 *
 * <ul>
 *   <li>{@link #PENDING} — materialised on case creation, never yet active
 *   <li>{@link #ACTIVE} — currently the case's head stage
 *   <li>{@link #COMPLETED} — was active, has been advanced past
 *   <li>{@link #SKIPPED} — never active, jumped over by {@link
 *       com.wkspower.platform.domain.service.WksStageAdvancer#skipTo}
 *   <li>{@link #REMAPPED} — was active, remapped to a different stage id by the operator-supplied
 *       {@code stageRemap} during a CaseType version rebase (Story 3.9.1). The row is closed by the
 *       rebase apply path; the stage's data is preserved for audit. UI rendering is deferred.
 * </ul>
 */
public enum StageState {
  PENDING,
  ACTIVE,
  COMPLETED,
  SKIPPED,
  REMAPPED
}
