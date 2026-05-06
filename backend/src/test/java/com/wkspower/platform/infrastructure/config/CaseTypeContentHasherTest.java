package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CaseTypeContentHasher} (Story 3.4 / Decision 20). Pins the canonical-hash
 * contract: byte-identical equals; whitespace / comment / key-reorder same; semantic edits differ;
 * empty bytes throw.
 */
class CaseTypeContentHasherTest {

  private final CaseTypeContentHasher hasher = new CaseTypeContentHasher();

  private static final byte[] BASELINE =
      ("id: j9-zero-zero\n"
              + "displayName: \"[J9] Zero Stages\"\n"
              + "version: 1\n"
              + "roles:\n"
              + "  - name: admin\n"
              + "    permissions: [view, create]\n")
          .getBytes();

  @Test
  void byteIdenticalProducesSameHash() {
    String h1 = hasher.hash(BASELINE);
    String h2 = hasher.hash(BASELINE.clone());
    assertThat(h1).isEqualTo(h2);
    assertThat(h1).hasSize(64).matches("[0-9a-f]{64}");
  }

  @Test
  void whitespaceOnlyEditProducesSameHash() {
    byte[] padded =
        ("id: j9-zero-zero\n"
                + "\n"
                + "displayName: \"[J9] Zero Stages\"\n"
                + "version:    1\n"
                + "\n"
                + "roles:\n"
                + "  - name: admin\n"
                + "    permissions: [view, create]\n\n")
            .getBytes();
    assertThat(hasher.hash(padded)).isEqualTo(hasher.hash(BASELINE));
  }

  @Test
  void commentOnlyEditProducesSameHash() {
    byte[] commented =
        ("# story 3.4 fixture\n"
                + "id: j9-zero-zero\n"
                + "displayName: \"[J9] Zero Stages\" # smallest valid\n"
                + "version: 1\n"
                + "roles:\n"
                + "  - name: admin\n"
                + "    permissions: [view, create]\n")
            .getBytes();
    assertThat(hasher.hash(commented)).isEqualTo(hasher.hash(BASELINE));
  }

  @Test
  void keyReorderProducesSameHash() {
    byte[] reordered =
        ("displayName: \"[J9] Zero Stages\"\n"
                + "version: 1\n"
                + "id: j9-zero-zero\n"
                + "roles:\n"
                + "  - permissions: [view, create]\n"
                + "    name: admin\n")
            .getBytes();
    assertThat(hasher.hash(reordered)).isEqualTo(hasher.hash(BASELINE));
  }

  @Test
  void semanticEditProducesDifferentHash() {
    byte[] semantic =
        ("id: j9-zero-zero\n"
                + "displayName: \"[J9] Zero Stages\"\n"
                + "version: 1\n"
                + "roles:\n"
                + "  - name: admin\n"
                + "    permissions: [view, create, edit]\n")
            .getBytes();
    assertThat(hasher.hash(semantic)).isNotEqualTo(hasher.hash(BASELINE));
  }

  @Test
  void emptyBytesThrow() {
    assertThatThrownBy(() -> hasher.hash(new byte[0]))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("non-empty");
  }

  @Test
  void nullBytesThrow() {
    assertThatThrownBy(() -> hasher.hash(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("non-null");
  }

  @Test
  void unparseableYamlThrows() {
    assertThatThrownBy(() -> hasher.hash("a: [\nbroken".getBytes()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
