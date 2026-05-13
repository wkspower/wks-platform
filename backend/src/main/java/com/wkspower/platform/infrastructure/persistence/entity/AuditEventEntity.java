package com.wkspower.platform.infrastructure.persistence.entity;

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
 * Story 9-3 — JPA entity mirroring the {@code audit_events} table. Lives in {@code
 * infrastructure.persistence.entity} per the ArchUnit rule {@code
 * jpaEntitiesLiveOnlyInPersistenceEntityPackage} (Story 1.4 AC12).
 *
 * <p>Does NOT extend {@code BaseJpaEntity} because:
 *
 * <ul>
 *   <li>The table has no {@code @Version} column — rows are never updated (append-only).
 *   <li>The table has no {@code @PreUpdate} callback — same reason.
 *   <li>{@code created_at} is DB-stamped via the column {@code DEFAULT CURRENT_TIMESTAMP}, not by a
 *       {@code @PrePersist} hook.
 * </ul>
 *
 * <p>Append-only invariant: every column except {@code created_at} is {@code updatable=false}.
 * External code must NEVER instantiate this entity directly; {@code
 * com.wkspower.platform.audit.AuditEventRepository} is the only authorised writer and exposes only
 * {@code insert} + read at its public surface. {@link AuditEventJpaRepository}'s {@code
 * JpaRepository}-derived mutation methods ({@code save}, {@code delete}, {@code deleteAll}, ...)
 * are off-contract — code review + the AC2 surface-guard test on {@code AuditEventRepository}
 * enforce that no other call site reaches the JPA repository directly.
 */
@Entity
@Table(name = "audit_events")
public class AuditEventEntity {

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
  // INSERT and the DB default applies. Field stays null on the freshly-persisted in-memory entity
  // (Hibernate does not auto-refresh); production reads re-load through the repository's
  // findByCaseId path which sees the DB value.
  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;

  protected AuditEventEntity() {
    // JPA
  }

  public AuditEventEntity(
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

  public UUID getId() {
    return id;
  }

  public UUID getCaseId() {
    return caseId;
  }

  public String getEventType() {
    return eventType;
  }

  public String getSourceType() {
    return sourceType;
  }

  public Map<String, Object> getSourcePayload() {
    return sourcePayload;
  }

  public String getResult() {
    return result;
  }

  public String getFieldId() {
    return fieldId;
  }

  public String getOpenTaskId() {
    return openTaskId;
  }

  public String getFormId() {
    return formId;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
