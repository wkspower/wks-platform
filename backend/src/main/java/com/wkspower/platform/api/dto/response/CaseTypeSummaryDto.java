package com.wkspower.platform.api.dto.response;

/**
 * Summary projection of a case type for {@code GET /api/case-types}. The case-type filter dropdown
 * (frontend Story 2.5) only needs {@code id} + {@code displayName}, but {@code statusCount} and
 * {@code fieldCount} are essentially free to compute and surface useful chrome for a future
 * case-type selector. Heavy fields ({@code fields[]}, {@code statuses[]}, {@code listColumns}) live
 * on {@link CaseTypeViewDto} for {@code GET /api/case-types/{id}}.
 */
public record CaseTypeSummaryDto(
    String id, String displayName, int version, int statusCount, int fieldCount) {}
