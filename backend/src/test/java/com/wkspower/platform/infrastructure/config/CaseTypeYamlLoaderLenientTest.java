package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.infrastructure.config.CaseTypeYamlLoader.RawReadResult;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Story 3.11 AC1 — unit tests for {@link CaseTypeYamlLoader#readBytesLenient}.
 *
 * <p>Verifies: lenient mapper tolerates unknown top-level / nested keys; truly malformed YAML still
 * fails on both paths; valid drift-free YAML produces equal {@link RawCaseTypeConfig} on both paths
 * (sanity — no false-positive divergence).
 */
class CaseTypeYamlLoaderLenientTest {

  private final CaseTypeYamlLoader loader = new CaseTypeYamlLoader();

  /** Minimal valid case-type YAML used as the structural base for drift scenarios. */
  private static byte[] base() {
    return ("""
        id: lenient-test
        displayName: "Lenient Test"
        version: 1
        statuses:
          - id: open
            displayName: Open
            color: blue
        roles:
          - name: admin
            permissions: [view, create, edit, transition]
        """)
        .getBytes(StandardCharsets.UTF_8);
  }

  // ---- Case 1: unknown top-level key — RawCaseTypeConfig carries
  // @JsonIgnoreProperties(ignoreUnknown = true) so BOTH paths tolerate top-level drift. The
  // lenient path is for nested mapping-subtree drift (case 2). Top-level forward-compat is
  // already in place per Story 4.3.1 AC8 commentary in CaseTypeYamlLoader.

  @Test
  void unknownTopLevelKey_bothPathsTolerate() {
    byte[] yaml =
        ("""
            id: lenient-test
            displayName: "Lenient Test"
            version: 1
            legacyField: removed-since
            statuses:
              - id: open
                displayName: Open
                color: blue
            roles:
              - name: admin
                permissions: [view, create, edit, transition]
            """)
            .getBytes(StandardCharsets.UTF_8);

    RawReadResult strict = loader.readBytes("strict.yaml", yaml);
    RawReadResult lenient = loader.readBytesLenient("lenient.yaml", yaml);

    // Both paths tolerate top-level unknown — RawCaseTypeConfig has @JsonIgnoreProperties.
    assertThat(strict.isParsed()).isTrue();
    assertThat(lenient.isParsed()).isTrue();
    assertThat(strict.raw().id()).isEqualTo("lenient-test");
    assertThat(lenient.raw().id()).isEqualTo("lenient-test");
  }

  // ---- Case 2: unknown nested key under a record that dropped @JsonIgnoreProperties ----
  // RawAttachment in the Mapping subtree (Story 4.3.1 AC8) is the cleanest example.

  @Test
  void unknownNestedKeyInAttachment_strictFails_lenientSucceeds() {
    byte[] yaml =
        ("""
            id: lenient-nested
            displayName: "Lenient Nested"
            version: 1
            statuses:
              - id: open
                displayName: Open
                color: blue
            roles:
              - name: admin
                permissions: [view, create, edit, transition]
            attachments:
              - type: bpmn
                file: process.bpmn
                scope: case
                legacyAttachmentSlot: gone
            """)
            .getBytes(StandardCharsets.UTF_8);

    RawReadResult strict = loader.readBytes("strict.yaml", yaml);
    assertThat(strict.isParsed())
        .as("strict mapper rejects unknown attachment-nested key")
        .isFalse();

    RawReadResult lenient = loader.readBytesLenient("lenient.yaml", yaml);
    assertThat(lenient.isParsed())
        .as("lenient mapper tolerates unknown attachment-nested key")
        .isTrue();
  }

  // ---- Case 3: malformed YAML (unterminated quoted string) — both fail ----

  @Test
  void malformedYaml_bothFail() {
    byte[] yaml =
        ("""
            id: malformed
            displayName: "Unterminated string
            version: 1
            """)
            .getBytes(StandardCharsets.UTF_8);

    RawReadResult strict = loader.readBytes("strict.yaml", yaml);
    RawReadResult lenient = loader.readBytesLenient("lenient.yaml", yaml);

    assertThat(strict.isParsed()).isFalse();
    assertThat(lenient.isParsed())
        .as("genuinely malformed YAML must fail lenient too — AC2 fail-closed regression guard")
        .isFalse();
    assertThat(lenient.errors().get(0).code()).isEqualTo(ErrorCode.WKS_CFG_099.wire());
  }

  // ---- Case 4: valid drift-free YAML — strict and lenient produce equal raw ----

  @Test
  void noDrift_bothPathsAgree() {
    byte[] yaml = base();
    RawReadResult strict = loader.readBytes("strict.yaml", yaml);
    RawReadResult lenient = loader.readBytesLenient("lenient.yaml", yaml);

    assertThat(strict.isParsed()).isTrue();
    assertThat(lenient.isParsed()).isTrue();
    assertThat(strict.raw())
        .as("no schema drift → strict and lenient projections must be equal")
        .isEqualTo(lenient.raw());
  }
}
