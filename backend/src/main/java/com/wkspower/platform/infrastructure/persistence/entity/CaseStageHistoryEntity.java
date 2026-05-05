package com.wkspower.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the append-only stage history table (Story 3.1 AC3). Inherits id / version / audit
 * timestamps from {@link BaseJpaEntity}; owns the case-stage-specific columns.
 *
 * <p>The {@link com.wkspower.platform.domain.model.StageState} enum maps via {@link
 * Enumerated.STRING} so the wire string ({@code PENDING}/{@code ACTIVE}/{@code COMPLETED}/{@code
 * SKIPPED}) lands as readable VARCHAR rather than an ordinal — keeps post-mortem SQL queries
 * grep-friendly.
 */
@Entity
@Table(name = "case_stage_history")
public class CaseStageHistoryEntity extends BaseJpaEntity {

  @Column(name = "case_id", nullable = false)
  private UUID caseId;

  @Column(name = "stage_id", length = 64, nullable = false)
  private String stageId;

  @Column(name = "ordinal", nullable = false)
  private int ordinal;

  @Enumerated(EnumType.STRING)
  @Column(name = "state", length = 16, nullable = false)
  private com.wkspower.platform.domain.model.StageState state;

  @Column(name = "entered_at")
  private Instant enteredAt;

  @Column(name = "exited_at")
  private Instant exitedAt;

  @Column(name = "source", length = 32)
  private String source;

  @Column(name = "source_ref", length = 128)
  private String sourceRef;

  protected CaseStageHistoryEntity() {
    // JPA
  }

  public CaseStageHistoryEntity(
      UUID id,
      UUID caseId,
      String stageId,
      int ordinal,
      com.wkspower.platform.domain.model.StageState state,
      Instant enteredAt,
      Instant exitedAt,
      String source,
      String sourceRef,
      Instant createdAt) {
    super(id);
    this.caseId = caseId;
    this.stageId = stageId;
    this.ordinal = ordinal;
    this.state = state;
    this.enteredAt = enteredAt;
    this.exitedAt = exitedAt;
    this.source = source;
    this.sourceRef = sourceRef;
    setCreatedAt(createdAt);
    setUpdatedAt(createdAt);
  }

  public UUID getCaseId() {
    return caseId;
  }

  public String getStageId() {
    return stageId;
  }

  public int getOrdinal() {
    return ordinal;
  }

  public com.wkspower.platform.domain.model.StageState getState() {
    return state;
  }

  public void setState(com.wkspower.platform.domain.model.StageState state) {
    this.state = state;
  }

  public Instant getEnteredAt() {
    return enteredAt;
  }

  public void setEnteredAt(Instant enteredAt) {
    this.enteredAt = enteredAt;
  }

  public Instant getExitedAt() {
    return exitedAt;
  }

  public void setExitedAt(Instant exitedAt) {
    this.exitedAt = exitedAt;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getSourceRef() {
    return sourceRef;
  }

  public void setSourceRef(String sourceRef) {
    this.sourceRef = sourceRef;
  }
}
