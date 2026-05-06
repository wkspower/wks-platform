package com.wkspower.platform.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite-key class for {@link StatusOptionEntity} (Story 3.7). Natural key {@code (case_type_id,
 * version, stage_id, status_id)} matches the {@code status_options} table primary key.
 *
 * <p>Field names + types must match the {@code @Id} fields on the entity.
 */
public class StatusOptionId implements Serializable {

  private String caseTypeId;
  private int version;
  private String stageId;
  private String statusId;

  public StatusOptionId() {
    // JPA
  }

  public StatusOptionId(String caseTypeId, int version, String stageId, String statusId) {
    this.caseTypeId = caseTypeId;
    this.version = version;
    this.stageId = stageId;
    this.statusId = statusId;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StatusOptionId other)) {
      return false;
    }
    return version == other.version
        && Objects.equals(caseTypeId, other.caseTypeId)
        && Objects.equals(stageId, other.stageId)
        && Objects.equals(statusId, other.statusId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(caseTypeId, version, stageId, statusId);
  }
}
