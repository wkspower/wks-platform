package com.wkspower.platform.domain.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for atomically updating {@code cases.status} from inside the engine-callback
 * pathway. Story 2.4's {@code CaseStatusListener} (CIB seven {@code ExecutionListener}) calls this
 * port instead of issuing JPA writes directly — see Story 2.4 Dev Notes §Engine-callback hexagonal
 * pattern.
 *
 * <p>The JPA adapter implements this interface and runs inside the engine's transaction, so a
 * listener failure rolls back both the engine state and the {@code cases.status} write atomically.
 *
 * <p>Returning the previous status lets the listener publish {@code CaseStatusChanged} with both
 * old + new values without an extra round trip.
 */
public interface CaseStatusUpdater {

  /**
   * Update the row's {@code status} column to {@code newStatus}. Returns the previous value (or
   * {@link Optional#empty()} if the row did not exist — engine state should never get this far if
   * the case row was created normally, so the empty branch is a defensive escape hatch). Returns
   * the previous status as {@link Optional} of the prior column value when the row exists.
   */
  Optional<String> updateStatus(UUID caseId, String newStatus);
}
