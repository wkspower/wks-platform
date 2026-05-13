package com.wkspower.platform.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Story 9-3 — JPA entity mirroring the {@code audit_events} table. Lives inside the {@code
 * com.wkspower.platform.audit} package (not the generic {@code infrastructure/persistence/entity}
 * tree) so the append-only invariant is co-located with the only repository allowed to touch it.
 *
 * <p>Does NOT extend {@code BaseJpaEntity} because:
 *
 * <ul>
 *   <li>The table has no {@code @Version} column — rows are never updated (append-only).
 *   <li>The table has no {@code @PreUpdate} callback — same reason.
 *   <li>{@code created_at} is DB-stamped via the column {@code DEFAULT CURRENT_TIMESTAMP}, not by
 *       a {@code @PrePersist} hook.
 * </ul>
 *
 * <p>Package-private. External code must go through {@link AuditEventRepository} which exposes only
 * insert + read — never the full {@code JpaRepository} mutation surface. The corresponding Spring
 * Data interface ({@link AuditEventJpaRepository}) is also package-private, so neither this entity
 * nor a {@code save/delete} call site can leak outside the package.
 */
@Entity
@Table(name = "audit_events")
class AuditEventEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "case_id", nullable = false, updatable = false)
  private UUID caseId;

  @Column(name = "event_type", nullable = false, updatable = false, length = 64)
  private String eventType;

  @Column(name = "source_type", nullable = false, updatable = false, length = 32)
  private String sourceType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "source_payload", nullable = false, updatable = false)
  private Map<String, Object> sourcePayload = new HashMap<>();

  @Column(name = "result", nullable = false, updatable = false, length = 16)
  private String result;

  @Column(name = "field_id", updatable = false, length = 128)
  private String fieldId;

  @Column(name = "open_task_id", updatable = false, length = 128)
  private String openTaskId;

  @Column(name = "form_id", updatable = false, length = 128)
  private String formId;

  @Column(name = "occurred_at", nullable = false, updatable = false)
  private Instant occurredAt;

  // DB-stamped via DEFAULT CURRENT_TIMESTAMP. insertable=false so Hibernate omits the column from
  // INSERT and the DB default applies; the value is read back on the entity after flush via
  // refresh() in the adapter (or left as null when only insert is needed).
  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;

  protected AuditEventEntity() {
    // JPA
  }

  AuditEventEntity(
      UUID id,
      UUID caseId,
      String eventType,
      String sourceType,
      Map<String, Object> sourcePayload,
      String result,
      String fieldId,
      String openTaskId,
      String formId,
      Instant occurredAt) {
    this.id = id;
    this.caseId = caseId;
    this.eventType = eventType;
    this.sourceType = sourceType;
    this.sourcePayload = sourcePayload;
    this.result = result;
    this.fieldId = fieldId;
    this.openTaskId = openTaskId;
    this.formId = formId;
    this.occurredAt = occurredAt;
  }

  UUID getId() {
    return id;
  }

  UUID getCaseId() {
    return caseId;
  }

  String getEventType() {
    return eventType;
  }

  String getSourceType() {
    return sourceType;
  }

  Map<String, Object> getSourcePayload() {
    return sourcePayload;
  }

  String getResult() {
    return result;
  }

  String getFieldId() {
    return fieldId;
  }

  String getOpenTaskId() {
    return openTaskId;
  }

  String getFormId() {
    return formId;
  }

  Instant getOccurredAt() {
    return occurredAt;
  }

  Instant getCreatedAt() {
    return createdAt;
  }
}
