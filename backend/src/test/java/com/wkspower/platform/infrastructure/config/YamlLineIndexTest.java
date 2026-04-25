package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class YamlLineIndexTest {

  @Test
  void capturesTopLevelScalarLines() throws Exception {
    String yaml =
        """
        id: loan-application
        displayName: Loan Application
        version: 1
        """;
    var idx = YamlLineIndex.of(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

    assertThat(idx.lineOf("id")).contains(1);
    assertThat(idx.lineOf("displayName")).contains(2);
    assertThat(idx.lineOf("version")).contains(3);
  }

  @Test
  void capturesNestedArrayElementLines() throws Exception {
    String yaml =
        """
        fields:
          - id: applicant_name
            type: text
          - id: loan_amount
            type: number
        """;
    var idx = YamlLineIndex.of(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

    assertThat(idx.lineOf("fields[0].id")).contains(2);
    assertThat(idx.lineOf("fields[0].type")).contains(3);
    assertThat(idx.lineOf("fields[1].id")).contains(4);
    assertThat(idx.lineOf("fields[1].type")).contains(5);
  }

  @Test
  void nearestAncestorFallback() throws Exception {
    String yaml =
        """
        fields:
          - id: x
            type: text
        """;
    var idx = YamlLineIndex.of(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)));

    // No such leaf — fallback to nearest known ancestor.
    assertThat(idx.lineOfOrNearest("fields[0].displayName")).isPresent();
  }
}
