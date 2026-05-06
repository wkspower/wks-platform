package com.wkspower.platform.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite-key class for {@link CaseTypeVersionEntity} (Story 3.4 / Decision 20). Natural key
 * {@code (case_type_id, version)} surfaces in the operator-facing schema; we use {@code @IdClass}
 * rather than a synthetic surrogate id to keep the operator key visible.
 *
 * <p>Field names + types must match the {@code @Id} fields on the entity.
 */
public class CaseTypeVersionId implements Serializable {

  private String caseTypeId;
  private int version;

  public CaseTypeVersionId() {
    // JPA
  }

  public CaseTypeVersionId(String caseTypeId, int version) {
    this.caseTypeId = caseTypeId;
    this.version = version;
  }

  public String getCaseTypeId() {
    return caseTypeId;
  }

  public int getVersion() {
    return version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CaseTypeVersionId other)) {
      return false;
    }
    return version == other.version && Objects.equals(caseTypeId, other.caseTypeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(caseTypeId, version);
  }
}
