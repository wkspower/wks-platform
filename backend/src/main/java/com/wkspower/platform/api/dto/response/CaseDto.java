package com.wkspower.platform.api.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Wire shape for {@code GET /api/cases/{id}} and the result of {@code POST /api/cases} / {@code PUT
 * /api/cases/{id}} (Story 2.3 AC5–AC8). {@code documentCount} is always {@code 0} until Epic 3;
 * {@code caseType} is the embedded type view (sub-object). The field shape is frozen here so Story
 * 3.2 can fill {@code documentCount} without a DTO bump.
 */
public record CaseDto(
    UUID id,
    String caseTypeId,
    int caseTypeVersion,
    String status,
    UUID assignee,
    Map<String, Object> data,
    String processInstanceId,
    int documentCount,
    Instant createdAt,
    UUID createdBy,
    Instant updatedAt,
    long version,
    CaseTypeViewDto caseType) {}
