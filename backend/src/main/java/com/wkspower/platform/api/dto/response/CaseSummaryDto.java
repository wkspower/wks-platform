package com.wkspower.platform.api.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Wire shape for one row of {@code GET /api/cases} (Story 2.3 AC7). System columns are top-level;
 * YAML-declared list columns appear under {@code fields}. The full {@code data} JSON is never
 * surfaced on the list path.
 */
public record CaseSummaryDto(
    UUID id,
    String caseTypeId,
    String status,
    UUID assignee,
    Instant createdAt,
    Instant updatedAt,
    Map<String, Object> fields) {}
