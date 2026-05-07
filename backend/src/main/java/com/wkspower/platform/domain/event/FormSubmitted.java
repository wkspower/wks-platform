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
 */
public record FormSubmitted(
    UUID caseId, String formId, UUID actorId, Instant submittedAt, Set<String> updatedFieldIds) {}
