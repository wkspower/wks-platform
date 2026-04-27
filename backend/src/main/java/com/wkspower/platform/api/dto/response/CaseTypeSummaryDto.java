package com.wkspower.platform.api.dto.response;

import java.util.List;

/**
 * Summary projection of a case type for {@code GET /api/case-types}. The case-type filter dropdown
 * (frontend Story 2.5) only needs {@code id} + {@code displayName}, but {@code statusCount} and
 * {@code fieldCount} are essentially free to compute and surface useful chrome for a future
 * case-type selector. Heavy fields ({@code fields[]}, {@code statuses[]}, {@code listColumns}) live
 * on {@link CaseTypeViewDto} for {@code GET /api/case-types/{id}}.
 *
 * <p>Story 2.7 adds {@link #permissions()} — the verbs the caller holds on this case-type. The
 * frontend uses this to filter the Create-Case selector dropdown to only case-types where the user
 * holds {@code create}, without an extra round-trip per case-type.
 */
public record CaseTypeSummaryDto(
    String id,
    String displayName,
    int version,
    int statusCount,
    int fieldCount,
    List<String> permissions) {

  public CaseTypeSummaryDto {
    permissions = permissions == null ? List.of() : List.copyOf(permissions);
  }
}
