package com.wkspower.platform.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Story 5.4 AC4 — emitted by the {@code FormDraftExpirationJob} for each draft row deleted because
 * its {@code updated_at} crossed the {@code wks.form.draft.ttl-days} threshold. The existing audit
 * pipeline ({@code @EventListener} reflection) picks this up without additional wiring.
 *
 * @param caseId the draft's case id
 * @param formId the draft's form id
 * @param userId the user who owned the draft
 * @param ageDays how many full days the draft sat untouched before expiration
 * @param occurredAt when the expiration deletion happened
 */
public record FormDraftExpired(
    UUID caseId, String formId, UUID userId, long ageDays, Instant occurredAt) {}
