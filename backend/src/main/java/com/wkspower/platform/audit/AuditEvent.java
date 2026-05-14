package com.wkspower.platform.audit;

import com.wkspower.platform.domain.event.CaseCreated;
import com.wkspower.platform.domain.event.CaseDataEdited;
import com.wkspower.platform.domain.event.CaseDocumentUploaded;
import com.wkspower.platform.domain.event.CaseStatusChanged;
import com.wkspower.platform.domain.model.AuditSource;
import java.time.Instant;
import java.util.UUID;

/**
 * Story 9-3 — in-memory representation of an {@code audit_events} row. Append-only by construction:
 * the type is an immutable record and the {@link AuditEventRepository} surface exposes only {@code
 * insert} + read. This is the persistence-side counterpart of the existing {@link CaseDataEdited}
 * domain event.
 *
 * <p>{@link #source} preserves the four sealed {@link AuditSource} variants verbatim — round-trip
 * fidelity is the AC4 contract. Mapping to/from the two database columns ({@code source_type},
 * {@code source_payload}) lives in {@link AuditEventMapper}.
 *
 * <p>{@code eventType} is the discriminator the table will use when future audit surfaces (rebase,
 * lifecycle, admin-action) fold into this table per {@code feedback_fold_debt_into_stories}. For
 * Sprint 12 the only persisted event type is {@code "case.data.edit"}.
 *
 * <p>{@code result} is a free-form String at this layer to keep the persistence type independent of
 * the {@link CaseDataEdited.Result} enum — future event types may have different result
 * vocabularies (e.g. lifecycle: {@code "ENTERED"} / {@code "EXITED"}). Callers persisting a {@code
 * CaseDataEdited} use {@code event.result().name()} verbatim.
 *
 * @param id append-only row id (UUID v4 — caller-generated for test determinism)
 * @param caseId case the event pertains to (FK to {@code cases.id})
 * @param eventType discriminator (currently always {@code "case.data.edit"})
 * @param source typed source attribution (round-trips through AuditEventMapper)
 * @param result result string (e.g. {@code "APPLIED"} / {@code "BLOCKED"} / {@code "REJECTED"}; for
 *     {@code case.status.changed} rows, the new status id)
 * @param previousResult prior value for delta-typed events ({@code case.status.changed} stores the
 *     old status id here). Null for non-delta event types (edit, created).
 * @param fieldId field the edit targeted (may be null for non-edit event types)
 * @param openTaskId open task that owned the field on BLOCKED (null on APPLIED)
 * @param formId form binding the field on BLOCKED (null on APPLIED)
 * @param occurredAt domain timestamp (when the underlying action happened — caller-provided)
 * @param createdAt insert timestamp (DB-stamped via DEFAULT CURRENT_TIMESTAMP; null on the
 *     domain-side instance pre-insert, populated on read-back)
 */
public record AuditEvent(
    UUID id,
    UUID caseId,
    String eventType,
    AuditSource source,
    String result,
    String previousResult,
    String fieldId,
    String openTaskId,
    String formId,
    Instant occurredAt,
    Instant createdAt) {

  /** Discriminator string for {@link CaseDataEdited}-backed rows. Stable wire string. */
  public static final String EVENT_TYPE_CASE_DATA_EDIT = "case.data.edit";

  /** Discriminator string for {@link CaseCreated}-backed rows. Stable wire string. */
  public static final String EVENT_TYPE_CASE_CREATED = "case.created";

  /** Discriminator string for {@link CaseStatusChanged}-backed rows. Stable wire string. */
  public static final String EVENT_TYPE_CASE_STATUS_CHANGED = "case.status.changed";

  /** Discriminator string for {@link CaseDocumentUploaded}-backed rows. Stable wire string. */
  public static final String EVENT_TYPE_CASE_DOCUMENT_UPLOADED = "case.document.uploaded";

  /**
   * Build an {@code AuditEvent} from a {@link CaseDataEdited} domain event, generating a fresh row
   * id. The {@code createdAt} stays null — Postgres / H2 stamp it via the column DEFAULT.
   */
  public static AuditEvent fromCaseDataEdited(CaseDataEdited event) {
    return new AuditEvent(
        UUID.randomUUID(),
        event.caseId(),
        EVENT_TYPE_CASE_DATA_EDIT,
        event.source(),
        event.result().name(),
        null,
        event.fieldId(),
        event.openTaskId(),
        event.formId(),
        event.timestamp(),
        null);
  }

  /**
   * Build an {@code AuditEvent} from a {@link CaseCreated} domain event. The acting user is wrapped
   * in {@link AuditSource.User}. Result is the literal {@code "CREATED"}; field/task/form slots are
   * null (creation is not field-scoped).
   */
  public static AuditEvent fromCaseCreated(CaseCreated event) {
    return new AuditEvent(
        UUID.randomUUID(),
        event.caseId(),
        EVENT_TYPE_CASE_CREATED,
        new AuditSource.User(event.actorId()),
        "CREATED",
        null,
        null,
        null,
        null,
        event.timestamp(),
        null);
  }

  /**
   * Build an {@code AuditEvent} from a {@link CaseStatusChanged} domain event. {@code result}
   * carries the new status id verbatim; {@code previousResult} carries the old status id (may be
   * null on the very first transition). Source is taken verbatim — {@code User} on manual
   * transitions, {@code Backend} on BPMN.
   */
  public static AuditEvent fromCaseStatusChanged(CaseStatusChanged event) {
    return new AuditEvent(
        UUID.randomUUID(),
        event.caseId(),
        EVENT_TYPE_CASE_STATUS_CHANGED,
        event.source(),
        event.newStatus(),
        event.oldStatus(),
        null,
        null,
        null,
        event.timestamp(),
        null);
  }

  /**
   * Build an {@code AuditEvent} from a {@link CaseDocumentUploaded} domain event. {@code result}
   * carries the sanitized filename verbatim (this is the user-facing label the activity feed
   * renders). The document id rides on the slf4j wire line for SI-runbook grep; persisting it as a
   * separate column is deferred to the next story that needs to filter/index by document id.
   */
  public static AuditEvent fromCaseDocumentUploaded(CaseDocumentUploaded event) {
    return new AuditEvent(
        UUID.randomUUID(),
        event.caseId(),
        EVENT_TYPE_CASE_DOCUMENT_UPLOADED,
        event.source(),
        event.fileName(),
        null,
        null,
        null,
        null,
        event.timestamp(),
        null);
  }
}
