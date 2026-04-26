package com.wkspower.platform.api.mapper;

import com.wkspower.platform.api.dto.response.CaseDto;
import com.wkspower.platform.api.dto.response.CaseSummaryDto;
import com.wkspower.platform.api.dto.response.CaseTypeViewDto;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseSummary;

/**
 * Domain-model → wire-DTO mapping for the case CRUD endpoints. Manual mapping (no MapStruct) per
 * Story 1.4 precedent.
 */
public final class CaseDtoMapper {

  private CaseDtoMapper() {
    // utility
  }

  public static CaseDto toDto(Case domain, CaseTypeConfig caseType) {
    return new CaseDto(
        domain.id(),
        domain.caseTypeId(),
        domain.caseTypeVersion(),
        domain.status(),
        domain.assignee(),
        domain.data(),
        domain.processInstanceId(),
        0, // documentCount — Epic 3 fills this; AC5 freezes the field at 0 in Phase 0.
        domain.createdAt(),
        domain.createdBy(),
        domain.updatedAt(),
        domain.version(),
        toCaseTypeView(caseType));
  }

  public static CaseSummaryDto toSummaryDto(CaseSummary summary) {
    return new CaseSummaryDto(
        summary.id(),
        summary.caseTypeId(),
        summary.status(),
        summary.assignee(),
        summary.createdAt(),
        summary.updatedAt(),
        summary.fields());
  }

  public static CaseTypeViewDto toCaseTypeView(CaseTypeConfig caseType) {
    return new CaseTypeViewDto(
        caseType.id(),
        caseType.displayName(),
        caseType.version(),
        caseType.fields(),
        caseType.statuses(),
        caseType.listColumns());
  }
}
