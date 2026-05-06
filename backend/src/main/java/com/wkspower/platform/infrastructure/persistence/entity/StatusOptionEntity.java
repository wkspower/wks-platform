package com.wkspower.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * JPA entity for {@code status_options} (Story 3.7). Stores append-class status edits made via the
 * admin REST API after a CaseType is deployed; bound to the row in {@code case_type_versions} via
 * the composite key {@code (case_type_id, version, stage_id, status_id)}.
 *
 * <p>Decision 21 — append-class writes target the CURRENT bound version per {@code
 * CaseTypeVersionRegistry}; never bumping it. Mutate-class writes (Story 3.8) will land here under
 * a freshly-bumped version envelope.
 *
 * <p>{@code stage_id == "__flat__"} represents case-type-level (non-stage-scoped) statuses. Using a
 * sentinel keeps the composite PK non-nullable and lets the index hit uniformly regardless of
 * whether the stage is declared. See {@code StatusOptionsStore.FLAT_SENTINEL}.
 */
@Entity
@Table(name = "status_options")
@IdClass(StatusOptionId.class)
public class StatusOptionEntity {

  @Id
  @Column(name = "case_type_id", length = 64, nullable = false, updatable = false)
  private String caseTypeId;

  @Id
  @Column(name = "version", nullable = false, updatable = false)
  private int version;

  @Id
  @Column(name = "stage_id", length = 64, nullable = false, updatable = false)
  private String stageId;

  @Id
  @Column(name = "status_id", length = 64, nullable = false, updatable = false)
  private String statusId;

  @Column(name = "display_name", length = 128, nullable = false)
  private String displayName;

  @Column(name = "color", length = 32, nullable = false)
  private String color;

  @Column(name = "terminal", nullable = false)
  private boolean terminal;

  @Column(name = "ordinal", nullable = false)
  private int ordinal;

  protected StatusOptionEntity() {
    // JPA
  }

  public StatusOptionEntity(
      String caseTypeId,
      int version,
      String stageId,
      String statusId,
      String displayName,
      String color,
      boolean terminal,
      int ordinal) {
    this.caseTypeId = caseTypeId;
    this.version = version;
    this.stageId = stageId;
    this.statusId = statusId;
    this.displayName = displayName;
    this.color = color;
    this.terminal = terminal;
    this.ordinal = ordinal;
  }

  public String getCaseTypeId() {
    return caseTypeId;
  }

  public int getVersion() {
    return version;
  }

  public String getStageId() {
    return stageId;
  }

  public String getStatusId() {
    return statusId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public boolean isTerminal() {
    return terminal;
  }

  public int getOrdinal() {
    return ordinal;
  }
}
