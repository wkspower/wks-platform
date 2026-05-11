package com.wkspower.platform.domain.event;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Domain event emitted by {@link com.wkspower.platform.domain.service.CaseService#submitForm} after
 * a successful form submission and case-data write. The event carries the minimal audit surface
 * required by Story 5.2 AC3.
 *
 * <p>{@code source: "form"} is implicit in the event type — {@code FormSubmitted} is the
 * form-sourced audit entry; no separate {@code source} field is needed.
 *
 * <p>Published via {@link com.wkspower.platform.domain.port.EventPublisher#publishAfterCommit} so
 * subscribers never observe state that the underlying transaction subsequently rolls back.
 *
 * <p>Story 5.2 — Phase 0. No persistence; in-memory routing via Spring ApplicationEventPublisher.
 *
 * <p>Story 5.5 AC-7 — {@code caseTypeVersion} records the pinned CaseTypeVersion the form was
 * resolved against (Decision D20 frozen-on-version). This is the version the case was bound to at
 * create time, NOT the latest deployed CaseType version. The audit row consumer MUST use this field
 * to surface "caseTypeVersion" in the audit payload JSON — never re-read the live registry.
 */
public record FormSubmitted(
    UUID caseId,
    String formId,
    UUID actorId,
    Instant submittedAt,
    Set<String> updatedFieldIds,
    /** Story 5.5 AC-7 — the pinned CaseTypeVersion the form was resolved against (D20). */
    int caseTypeVersion) {

  /**
   * Backward-compat factory for callers (e.g. test helpers) that pre-date Story 5.5's {@code
   * caseTypeVersion} slot. Defaults {@code caseTypeVersion} to {@code 0} as a sentinel — real
   * production paths must always supply the version.
   */
  public static FormSubmitted of(
      UUID caseId, String formId, UUID actorId, Instant submittedAt, Set<String> updatedFieldIds) {
    return new FormSubmitted(caseId, formId, actorId, submittedAt, updatedFieldIds, 0);
  }
}
