package com.wkspower.platform.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain record for a single business case. Plain Java — no Spring, no JPA, no Jackson annotations.
 * The ArchUnit rule {@code caseDomainHasNoSpringOrJpaImports} (Story 2.3 AC9) enforces this.
 *
 * <p>The {@code data} map carries dynamic fields whose ids come from the case-type YAML schema
 * (Story 2.1). The map is owned by the case — callers are expected to treat it as immutable; the
 * record's compact constructor makes a defensive copy.
 *
 * @param id case identifier (UUID)
 * @param caseTypeId case type id from YAML
 * @param caseTypeVersion snapshot of the case type version when the case was created
 * @param status current status id (initial value comes from {@code statuses[0].id} of the case
 *     type; later mutated by Story 2.4's BPMN execution listener)
 * @param assignee user id assigned to the case, or {@code null} when unassigned (queue)
 * @param data dynamic case data — keys are field ids declared in the case type's schema
 * @param processInstanceId BPMN engine-assigned process instance id; populated after engine start
 * @param createdAt creation timestamp
 * @param createdBy user id of the actor who created the case
 * @param updatedAt last-update timestamp
 * @param version optimistic-locking version (from {@code @Version} on the JPA row)
 * @param currentStageId Story 3.1 / 3.2 — denormalised cache of the latest ACTIVE stage id; {@code
 *     null} on zero-stage CaseTypes and after the last stage is completed
 * @param currentStageOrdinal Story 3.1 / 3.2 — companion ordinal to {@code currentStageId}; {@code
 *     null} when {@code currentStageId} is {@code null}
 */
public record Case(
    UUID id,
    String caseTypeId,
    int caseTypeVersion,
    String status,
    UUID assignee,
    Map<String, Object> data,
    String processInstanceId,
    Instant createdAt,
    UUID createdBy,
    Instant updatedAt,
    long version,
    String currentStageId,
    Integer currentStageOrdinal) {

  /**
   * Story 3.2 — backward-compat constructor for callers (and tests) that predate the stage-cache
   * fields. Defaults both stage-cache slots to {@code null} (zero-stage shape).
   *
   * @deprecated Story 4.4b review finding I13: this constructor silently sets both {@code
   *     currentStageId} and {@code currentStageOrdinal} to {@code null}, which is safe only when
   *     the caller truly owns a zero-stage case. Any caller that subsequently populates one of the
   *     two cache fields via a direct field-set (rather than constructing a new {@code Case}) risks
   *     creating a paired-but-mismatched row that the compact-constructor invariant now catches.
   *     Prefer the 13-arg canonical constructor and pass both cache slots explicitly. Story 3.8
   *     (mutate-class) and Epic 5 (forms) will be the first callers to migrate.
   */
  @Deprecated
  public Case(
      UUID id,
      String caseTypeId,
      int caseTypeVersion,
      String status,
      UUID assignee,
      Map<String, Object> data,
      String processInstanceId,
      Instant createdAt,
      UUID createdBy,
      Instant updatedAt,
      long version) {
    this(
        id,
        caseTypeId,
        caseTypeVersion,
        status,
        assignee,
        data,
        processInstanceId,
        createdAt,
        createdBy,
        updatedAt,
        version,
        null,
        null);
  }

  public Case {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(caseTypeId, "caseTypeId");
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(createdAt, "createdAt");
    Objects.requireNonNull(createdBy, "createdBy");
    Objects.requireNonNull(updatedAt, "updatedAt");
    // Story 4.4b AC4 — enforce currentStageId ⇔ currentStageOrdinal invariant (review I11).
    // One null without the other creates a paired-but-mismatched cache row. Frontends
    // dereferencing currentStageOrdinal! per the documented contract will NPE on such a row.
    if ((currentStageId == null) != (currentStageOrdinal == null)) {
      throw new IllegalArgumentException(
          "currentStageId and currentStageOrdinal must both be null or both be non-null"
              + " (currentStageId="
              + currentStageId
              + ", currentStageOrdinal="
              + currentStageOrdinal
              + ")");
    }
    Map<String, Object> source = data == null ? Map.of() : data;
    data = Collections.unmodifiableMap(new HashMap<>(source));
  }
}
