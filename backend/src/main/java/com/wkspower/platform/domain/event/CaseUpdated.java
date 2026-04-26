package com.wkspower.platform.domain.event;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Emitted after a {@code Case} is updated by {@code CaseService.update}. Carries the set of field
 * ids whose value changed between the old and new {@code data} maps so Story 4.1's listener can
 * write activity-feed entries like "Meera updated Applicant Name and Loan Amount".
 *
 * <p>{@code changedFieldIds} is computed by {@code CaseService.update} as the union of field ids
 * whose values are not {@link java.util.Objects#equals} between old and new {@code data}.
 *
 * <p>This event is additive vs the {@code architecture.md §Decision 11} table; the canonical
 * status-change event ({@code CaseStatusChanged}) lands with Story 2.4.
 */
public record CaseUpdated(
    UUID caseId, UUID actorId, Instant timestamp, Set<String> changedFieldIds) {

  public CaseUpdated {
    changedFieldIds = Set.copyOf(changedFieldIds);
  }
}
