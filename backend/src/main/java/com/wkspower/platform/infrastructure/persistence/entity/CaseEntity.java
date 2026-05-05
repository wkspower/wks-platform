package com.wkspower.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * JPA entity for {@code cases}. Inherits id / version / audit timestamps from {@link
 * BaseJpaEntity}; owns the case-specific columns plus the JSON {@code data} column that carries
 * dynamic case-type fields.
 *
 * <p>Hibernate 6 ({@link JdbcTypeCode} + {@link SqlTypes#JSON}) maps the {@code Map<String,
 * Object>} natively without {@code com.vladmihalcea:hibernate-types} — verified at story-creation
 * time (Spring Boot 3.5.4 → Hibernate 6.6.x). Resolved version recorded in dev record.
 */
@Entity
@Table(name = "cases")
public class CaseEntity extends BaseJpaEntity {

  @Column(name = "case_type_id", length = 64, nullable = false)
  private String caseTypeId;

  @Column(name = "case_type_version", nullable = false)
  private int caseTypeVersion;

  @Column(name = "status", length = 64, nullable = false)
  private String status;

  @Column(name = "assignee")
  private UUID assignee;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "data", nullable = false)
  private Map<String, Object> data = new HashMap<>();

  @Column(name = "process_instance_id", length = 64)
  private String processInstanceId;

  /**
   * Story 3.1 — denormalised cache of the latest ACTIVE stage. {@code null} on zero-stage cases
   * (CaseType declares no stages) and after the last stage is completed (Story 3.1 AC7).
   * Authoritative state lives in {@code case_stage_history}; this column is rebuildable.
   */
  @Column(name = "current_stage_id", length = 64)
  private String currentStageId;

  /** Story 3.1 — convenience companion to {@link #currentStageId} for ordering / fast lookup. */
  @Column(name = "current_stage_ordinal")
  private Integer currentStageOrdinal;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  protected CaseEntity() {
    // JPA
  }

  public CaseEntity(
      UUID id,
      String caseTypeId,
      int caseTypeVersion,
      String status,
      UUID assignee,
      Map<String, Object> data,
      String processInstanceId,
      UUID createdBy,
      Instant createdAt,
      Instant updatedAt) {
    super(id);
    this.caseTypeId = caseTypeId;
    this.caseTypeVersion = caseTypeVersion;
    this.status = status;
    this.assignee = assignee;
    this.data = data == null ? new HashMap<>() : new HashMap<>(data);
    this.processInstanceId = processInstanceId;
    this.createdBy = createdBy;
    setCreatedAt(createdAt);
    setUpdatedAt(updatedAt);
  }

  public String getCaseTypeId() {
    return caseTypeId;
  }

  public void setCaseTypeId(String caseTypeId) {
    this.caseTypeId = caseTypeId;
  }

  public int getCaseTypeVersion() {
    return caseTypeVersion;
  }

  public void setCaseTypeVersion(int caseTypeVersion) {
    this.caseTypeVersion = caseTypeVersion;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public UUID getAssignee() {
    return assignee;
  }

  public void setAssignee(UUID assignee) {
    this.assignee = assignee;
  }

  public Map<String, Object> getData() {
    return Collections.unmodifiableMap(data);
  }

  public void setData(Map<String, Object> data) {
    this.data = data == null ? new HashMap<>() : new HashMap<>(data);
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getCurrentStageId() {
    return currentStageId;
  }

  public void setCurrentStageId(String currentStageId) {
    this.currentStageId = currentStageId;
  }

  public Integer getCurrentStageOrdinal() {
    return currentStageOrdinal;
  }

  public void setCurrentStageOrdinal(Integer currentStageOrdinal) {
    this.currentStageOrdinal = currentStageOrdinal;
  }

  public UUID getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(UUID createdBy) {
    this.createdBy = createdBy;
  }

  @Override
  public void setUpdatedAt(Instant updatedAt) {
    super.setUpdatedAt(updatedAt);
  }

  /** Exposed so the adapter can pin the optimistic-lock version to the caller's expected value. */
  public void setExpectedVersion(long version) {
    setVersion(version);
  }
}
