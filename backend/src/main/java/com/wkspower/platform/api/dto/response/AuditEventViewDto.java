package com.wkspower.platform.api.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Story 9-2 AC2 — wire shape for {@code GET /api/cases/{caseId}/audit-events}. One entry per
 * persisted {@code audit_events} row.
 *
 * <p>{@link #source} preserves the 4-variant sealed-interface discriminator from {@link
 * com.wkspower.platform.domain.model.AuditSource}: {@code type} is the persistence discriminator
 * string ({@code USER} / {@code AUTO_RULE} / {@code BACKEND} / {@code EXECUTION_UNMAPPED}) and
 * {@code payload} mirrors the {@code audit_events.source_payload} JSON column 1:1 (matching the
 * variant-shape pinned by {@link com.wkspower.platform.audit.AuditEventMapper}). The frontend
 * discriminates on {@code type} and renders the variant per Story 9-2 §Design Decision 3.
 *
 * <p>{@code fieldId} / {@code openTaskId} / {@code formId} are nullable to mirror the persistence
 * shape (only populated on {@code case.data.edit} rows; future audit surfaces fold into the same
 * table with these fields null).
 *
 * @param id append-only row id
 * @param eventType discriminator (e.g. {@code "case.data.edit"})
 * @param source {@link AuditSourceView} — {@code { type, payload }} variant shape
 * @param result result string ({@code "APPLIED"} / {@code "BLOCKED"} / {@code "REJECTED"}; for
 *     {@code case.status.changed} rows, the new status id)
 * @param previousResult prior value for delta-typed events ({@code case.status.changed} carries the
 *     old status id here). Null for non-delta event types (edit, created).
 * @param fieldId field id (null for non-edit event types)
 * @param openTaskId open task that owned the field on BLOCKED (null on APPLIED)
 * @param formId form binding the field on BLOCKED (null on APPLIED)
 * @param occurredAt ISO-8601 instant
 */
public record AuditEventViewDto(
    UUID id,
    String eventType,
    AuditSourceView source,
    String result,
    String previousResult,
    String fieldId,
    String openTaskId,
    String formId,
    Instant occurredAt) {

  /**
   * Story 9-2 AC2 — sealed-interface bridge to the wire. {@code type} is the persistence
   * discriminator string from {@link com.wkspower.platform.audit.AuditEventMapper}; {@code payload}
   * is the variant-shape map.
   */
  public record AuditSourceView(String type, Map<String, Object> payload) {}
}
