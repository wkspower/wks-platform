package com.wkspower.platform.api.mapper;

import com.wkspower.platform.api.dto.response.CaseDto;
import com.wkspower.platform.api.dto.response.CaseSummaryDto;
import com.wkspower.platform.api.dto.response.CaseTypeViewDto;
import com.wkspower.platform.api.dto.response.CaseTypeViewDto.FieldView;
import com.wkspower.platform.api.dto.response.CaseTypeViewDto.OptionView;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseSummary;
import java.util.List;

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
        caseType.fields().stream().map(CaseDtoMapper::toFieldView).toList(),
        caseType.statuses(),
        caseType.listColumns());
  }

  static FieldView toFieldView(FieldDefinition f) {
    FieldDefinition.TypeSlots s = f.slots();
    List<OptionView> options =
        f.options().stream().map(o -> new OptionView(o.label(), o.value())).toList();
    return new FieldView(
        f.id(),
        f.displayName(),
        f.type().wire(),
        f.required(),
        f.requiredOnCreate(),
        f.order(),
        options,
        s == null ? null : s.minLength(),
        s == null ? null : s.maxLength(),
        s == null ? null : s.min(),
        s == null ? null : s.max(),
        s == null ? null : s.step(),
        s == null ? null : s.dateMin(),
        s == null ? null : s.dateMax(),
        s == null ? null : s.maxBytes(),
        s == null || s.allowedMimeTypes() == null ? List.of() : s.allowedMimeTypes());
  }
}
