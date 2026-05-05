package com.wkspower.platform.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain record for a single stage row in a case's lifecycle history (Story 3.1 AC2). Plain Java —
 * no Spring, no JPA, no Jackson annotations. Domain-purity ArchUnit rules treat this package as a
 * framework-free zone (Decision 4 / NFR36).
 *
 * <p>Each {@link Stage} corresponds to one row of {@code case_stage_history}. The history table is
 * append-only-ish — a row is updated to flip {@link StageState} (e.g. {@code PENDING} → {@code
 * ACTIVE}, {@code ACTIVE} → {@code COMPLETED}, {@code PENDING} → {@code SKIPPED}) but never
 * deleted. The denormalised cache lives on {@code cases.current_stage_id} / {@code
 * current_stage_ordinal}; this record exists for reads and tests.
 *
 * @param id row identifier (UUID)
 * @param caseId owning case id
 * @param stageId YAML-declared stage id (e.g. {@code "intake"})
 * @param ordinal zero-based ordinal in the declared stage list
 * @param state {@link StageState} of this row
 * @param enteredAt timestamp the row entered {@link StageState#ACTIVE}; {@code null} for rows still
 *     {@link StageState#PENDING} or rows that were {@link StageState#SKIPPED} without ever going
 *     active
 * @param exitedAt timestamp the row left {@link StageState#ACTIVE} (i.e. became {@code COMPLETED}
 *     or {@code SKIPPED}); {@code null} while a row is still {@code PENDING} or {@code ACTIVE}
 * @param source one of {@code wks-auto-rule} / {@code manual} / {@code backend-signal} (Decision 1)
 *     — {@code null} on initial {@code PENDING} rows before the first transition
 * @param sourceRef free-form correlation string (user id, BPMN process-instance id, etc.); {@code
 *     null} when not applicable
 */
public record Stage(
    UUID id,
    UUID caseId,
    String stageId,
    int ordinal,
    StageState state,
    Instant enteredAt,
    Instant exitedAt,
    String source,
    String sourceRef) {

  public Stage {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(caseId, "caseId");
    Objects.requireNonNull(stageId, "stageId");
    Objects.requireNonNull(state, "state");
    if (ordinal < 0) {
      throw new IllegalArgumentException("ordinal must be >= 0");
    }
  }
}
