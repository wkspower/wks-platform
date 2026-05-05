package com.wkspower.platform.infrastructure.persistence.mapper;

import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.infrastructure.persistence.entity.CaseEntity;

/**
 * Manual mapper between the {@link CaseEntity} JPA row and the {@link Case} domain record. Mirrors
 * Story 1.4's no-MapStruct precedent — keeps the mapping explicit and easy to grep.
 */
public final class CaseMapper {

  private CaseMapper() {
    // utility
  }

  public static Case toDomain(CaseEntity entity) {
    return new Case(
        entity.getId(),
        entity.getCaseTypeId(),
        entity.getCaseTypeVersion(),
        entity.getStatus(),
        entity.getAssignee(),
        entity.getData(),
        entity.getProcessInstanceId(),
        entity.getCreatedAt(),
        entity.getCreatedBy(),
        entity.getUpdatedAt(),
        entity.getVersion(),
        // Story 3.2 AC5 — propagate the Story 3.1 stage-cache columns into the domain model so the
        // CaseDto and the (Story 3.3) timeline UI can read them. Zero-stage CaseTypes ⇒ null/null.
        entity.getCurrentStageId(),
        entity.getCurrentStageOrdinal());
  }

  public static CaseEntity toEntity(Case domain) {
    return new CaseEntity(
        domain.id(),
        domain.caseTypeId(),
        domain.caseTypeVersion(),
        domain.status(),
        domain.assignee(),
        domain.data(),
        domain.processInstanceId(),
        domain.createdBy(),
        domain.createdAt(),
        domain.updatedAt());
  }
}
