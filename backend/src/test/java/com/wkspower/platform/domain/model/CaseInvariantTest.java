package com.wkspower.platform.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Story 4.4b AC4 — four-corner invariant test for {@link Case}'s compact constructor
 * {@code currentStageId ⇔ currentStageOrdinal} guard (review finding I11).
 *
 * <p>Covers: (1) both null — valid, (2) both non-null — valid, (3) only id set — throws, (4) only
 * ordinal set — throws.
 */
class CaseInvariantTest {

  private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final Instant NOW = Instant.parse("2026-05-06T12:00:00Z");
  private static final UUID ACTOR = UUID.fromString("00000000-0000-0000-0000-000000000002");

  // ---- corner 1: both null — valid (zero-stage shape) ----

  @Test
  void bothNull_isValid() {
    Case c =
        new Case(
            ID,
            "zero-zero",
            1,
            "open",
            null,
            Map.of(),
            null,
            NOW,
            ACTOR,
            NOW,
            0L,
            null,  // currentStageId
            null); // currentStageOrdinal
    assertThat(c.currentStageId()).isNull();
    assertThat(c.currentStageOrdinal()).isNull();
  }

  // ---- corner 2: both non-null — valid (staged case) ----

  @Test
  void bothNonNull_isValid() {
    Case c =
        new Case(
            ID,
            "loan-app",
            1,
            "open",
            null,
            Map.of(),
            null,
            NOW,
            ACTOR,
            NOW,
            0L,
            "stage1", // currentStageId
            0);       // currentStageOrdinal
    assertThat(c.currentStageId()).isEqualTo("stage1");
    assertThat(c.currentStageOrdinal()).isEqualTo(0);
  }

  // ---- corner 3: only id set, ordinal null — throws ----

  @Test
  void onlyIdSet_throws() {
    assertThatThrownBy(
            () ->
                new Case(
                    ID,
                    "loan-app",
                    1,
                    "open",
                    null,
                    Map.of(),
                    null,
                    NOW,
                    ACTOR,
                    NOW,
                    0L,
                    "stage1", // currentStageId set
                    null))    // currentStageOrdinal null — invalid
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("currentStageId")
        .hasMessageContaining("currentStageOrdinal");
  }

  // ---- corner 4: only ordinal set, id null — throws ----

  @Test
  void onlyOrdinalSet_throws() {
    assertThatThrownBy(
            () ->
                new Case(
                    ID,
                    "loan-app",
                    1,
                    "open",
                    null,
                    Map.of(),
                    null,
                    NOW,
                    ACTOR,
                    NOW,
                    0L,
                    null, // currentStageId null — invalid
                    0))   // currentStageOrdinal set
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("currentStageId")
        .hasMessageContaining("currentStageOrdinal");
  }
}
