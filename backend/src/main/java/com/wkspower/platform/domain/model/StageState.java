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
 * </ul>
 */
public enum StageState {
  PENDING,
  ACTIVE,
  COMPLETED,
  SKIPPED
}
