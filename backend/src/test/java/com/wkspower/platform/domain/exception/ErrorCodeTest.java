package com.wkspower.platform.domain.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Structural guardrail for the public error-code taxonomy.
 *
 * <p>The wire string ({@link ErrorCode#wire()}) is the public contract — customer runbooks and
 * frontend snapshot tests depend on exact strings. A duplicate wire string across two enum
 * constants would compile and ship silently, quietly breaking the "one code per meaning" invariant.
 * This test fails the build as soon as that happens.
 */
class ErrorCodeTest {

  @Test
  void wireStringsAreUniqueAcrossAllEnumValues() {
    var duplicates =
        Arrays.stream(ErrorCode.values())
            .collect(Collectors.groupingBy(ErrorCode::wire, Collectors.counting()))
            .entrySet()
            .stream()
            .filter(e -> e.getValue() > 1)
            .map(e -> e.getKey() + " (x" + e.getValue() + ")")
            .toList();

    assertThat(duplicates)
        .as("ErrorCode.wire() must be unique across all enum constants")
        .isEmpty();
  }

  @Test
  void caseTypeValidationBandIsPopulated() {
    // Story 2.1 must add the 001..009 + 099 band. Guard so future edits can't silently
    // drop a code and pass the uniqueness check.
    var cfgCodes =
        Arrays.stream(ErrorCode.values())
            .map(ErrorCode::wire)
            .filter(w -> w.startsWith("WKS-CFG-"))
            .collect(Collectors.toSet());
    assertThat(cfgCodes)
        .as("Story 2.1 introduces 001..009 and 099 in the WKS-CFG band")
        .contains(
            "WKS-CFG-001",
            "WKS-CFG-002",
            "WKS-CFG-003",
            "WKS-CFG-004",
            "WKS-CFG-005",
            "WKS-CFG-006",
            "WKS-CFG-007",
            "WKS-CFG-008",
            "WKS-CFG-009",
            "WKS-CFG-011",
            "WKS-CFG-099");
  }

  @Test
  void bpmnValidationBandIsPopulated() {
    // Story 2.2 introduces 010, 012, 020, 021. Codes 013..019 and 022..099 are reserved.
    var cfgCodes =
        Arrays.stream(ErrorCode.values())
            .map(ErrorCode::wire)
            .filter(w -> w.startsWith("WKS-CFG-"))
            .collect(Collectors.toSet());
    assertThat(cfgCodes)
        .as("Story 2.2 introduces 010, 012, 020, 021 in the WKS-CFG band")
        .contains("WKS-CFG-010", "WKS-CFG-012", "WKS-CFG-020", "WKS-CFG-021");
  }

  @Test
  void optimisticLockCodeIsExposed() {
    // Story 2.3 introduces WKS-RTM-409 for optimistic-lock conflicts on PUT /api/cases/{id}
    // and any future entity update via BaseJpaEntity.@Version.
    assertThat(ErrorCode.WKS_RTM_409.wire()).isEqualTo("WKS-RTM-409");
  }

  @Test
  void multipartUploadCodeIsExposed() {
    // Story 2.2 admin deploy endpoint maps Spring's MaxUploadSizeExceededException to this code.
    assertThat(ErrorCode.WKS_API_413.wire()).isEqualTo("WKS-API-413");
  }

  @Test
  void wireStringsFollowWksHyphenFormat() {
    // Pattern: WKS-<PREFIX>-<NNN> where prefix is 3–4 uppercase letters and NNN is 3 digits.
    // Story 5.1 introduced WKS-FORM-001 — the first 4-letter prefix band. Prior bands (CFG, MAP,
    // STG, VER, RTM, API) all use 3-letter prefixes; the pattern is relaxed to [A-Z]{3,4} to
    // accommodate the FORM band while retaining the guardrail against arbitrary-length prefixes.
    for (ErrorCode code : ErrorCode.values()) {
      assertThat(code.wire())
          .as("ErrorCode.%s wire string must match WKS-<PREFIX>-NNN format", code.name())
          .matches("^WKS-[A-Z]{3,4}-[0-9]{3}$");
    }
  }
}
