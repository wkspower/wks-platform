package com.wkspower.platform.api.dto.response;

import java.time.Instant;
import java.util.List;
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
 * <p>Story 3.3 — appended {@code stages: List<StageView>} (always non-null; empty for zero-stage
 * CaseTypes). The two scalar denormalised cache fields above are owned by Story 3.2; the array is
 * owned by Story 3.3 per the locked Sprint 2 coordination split. Scalars give O(1) "which stage am
 * I on?" without iterating the array.
 *
 * <p>Story 3.6 AC8 — appends {@code availableStatuses: List<StatusView>} (always non-null; empty
 * only when both stage-scoped and flat status sets are empty — should be impossible after Story
 * 3.2's default). Resolved at mapping time using {@code caseType.statusesFor(currentStageId)} per
 * Decision 19's unbranched-paths invariant.
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
    Integer currentStageOrdinal,
    List<StageView> stages,
    List<StatusView> availableStatuses) {

  /**
   * Compact constructor — defensive-copy {@code stages} and {@code availableStatuses} so the wire
   * shape is immutable end-to-end (mirrors {@code CaseTypeConfig}'s {@code List.copyOf}
   * discipline).
   */
  public CaseDto {
    stages = stages == null ? List.of() : List.copyOf(stages);
    availableStatuses = availableStatuses == null ? List.of() : List.copyOf(availableStatuses);
  }

  /**
   * Backward-compat constructor for Story 3.2 / 3.3 callers (and tests) authored before Story 3.6
   * appended the {@code availableStatuses} slot. Defaults the new slot to the empty list — the
   * mapper should always populate via the canonical constructor.
   */
  public CaseDto(
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
      Integer currentStageOrdinal,
      List<StageView> stages) {
    this(
        id,
        caseTypeId,
        caseTypeVersion,
        status,
        assignee,
        data,
        processInstanceId,
        documentCount,
        createdAt,
        createdBy,
        updatedAt,
        version,
        caseType,
        currentStageId,
        currentStageOrdinal,
        stages,
        List.of());
  }
}
