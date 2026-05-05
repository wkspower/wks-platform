package com.wkspower.platform.api.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Wire shape for {@code GET /api/cases/{id}} and the result of {@code POST /api/cases} / {@code PUT
 * /api/cases/{id}} (Story 2.3 AC5–AC8). {@code documentCount} is always {@code 0} until Epic 3;
 * {@code caseType} is the embedded type view (sub-object).
 *
 * <p>Story 3.2 AC5 — adds two NULLABLE scalar stage-cache fields:
 *
 * <ul>
 *   <li>{@code currentStageId} — the latest ACTIVE stage id; {@code null} on zero-stage CaseTypes
 *       and after the final stage is completed.
 *   <li>{@code currentStageOrdinal} — companion ordinal; {@code null} iff {@code currentStageId} is
 *       {@code null}.
 * </ul>
 *
 * Future consumer: Story 3.3 (StageTimeline frontend component) reads both fields to highlight the
 * active stage. Story 3.3 ALSO ships a {@code stages: List<StageView>} array field on this DTO (the
 * timeline payload); the two scalars and the array are additive, not redundant — scalars give O(1)
 * "which stage am I on?" without iterating the array.
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
    CaseTypeViewDto caseType,
    String currentStageId,
    Integer currentStageOrdinal) {}
