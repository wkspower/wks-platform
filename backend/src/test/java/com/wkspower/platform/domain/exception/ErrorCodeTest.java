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
  void wireStringsFollowWksTripleHyphenFormat() {
    for (ErrorCode code : ErrorCode.values()) {
      assertThat(code.wire())
          .as("ErrorCode.%s wire string must match WKS-XXX-NNN format", code.name())
          .matches("^WKS-[A-Z]{3}-[0-9]{3}$");
    }
  }
}
