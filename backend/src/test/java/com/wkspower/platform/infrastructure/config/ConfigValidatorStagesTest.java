package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.model.StageDefinition;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Story 3.1 — extends {@link ConfigValidatorTest} surface with stage-grammar tests. The original
 * file has the GREEN_YAML constant; this class stays self-contained to avoid editing the existing
 * test class structure.
 */
class ConfigValidatorStagesTest {

  private final CaseTypeYamlLoader loader = new CaseTypeYamlLoader();
  private final ConfigValidator validator = new ConfigValidator();

  // ---- legal forms ----

  @Test
  void omittedStagesProducesEmptyList() {
    var r = validate(GREEN);
    assertThat(r.isInvalid()).isFalse();
    assertThat(r.config().get().stages()).isEmpty();
  }

  @Test
  void emptyStagesArrayIsLegal() {
    var r =
        validate(
            GREEN.replace(
                "listColumns: [applicant_name]", "stages: []\nlistColumns: [applicant_name]"));
    assertThat(r.isInvalid()).isFalse();
    assertThat(r.config().get().stages()).isEmpty();
  }

  @Test
  void stringListFormParsesWithDefaultDisplayName() {
    var r =
        validate(
            GREEN.replace(
                "listColumns: [applicant_name]",
                "stages: [intake, loan-decision]\nlistColumns: [applicant_name]"));
    assertThat(r.isInvalid()).isFalse();
    List<StageDefinition> stages = r.config().get().stages();
    assertThat(stages).hasSize(2);
    assertThat(stages.get(0).id()).isEqualTo("intake");
    assertThat(stages.get(0).ordinal()).isEqualTo(0);
    assertThat(stages.get(0).displayName()).isEqualTo("Intake");
    assertThat(stages.get(1).id()).isEqualTo("loan-decision");
    assertThat(stages.get(1).ordinal()).isEqualTo(1);
    assertThat(stages.get(1).displayName()).isEqualTo("Loan Decision");
  }

  @Test
  void richObjectFormPreservesExplicitDisplayName() {
    String yaml =
        GREEN.replace(
            "listColumns: [applicant_name]",
            "stages:\n"
                + "  - id: intake\n"
                + "    displayName: \"Custom Intake\"\n"
                + "  - id: review\n"
                + "listColumns: [applicant_name]");
    var r = validate(yaml);
    assertThat(r.isInvalid()).isFalse();
    var stages = r.config().get().stages();
    assertThat(stages).hasSize(2);
    assertThat(stages.get(0).displayName()).isEqualTo("Custom Intake");
    assertThat(stages.get(1).displayName()).isEqualTo("Review");
  }

  // ---- WKS-CFG-031 duplicate stage id ----

  @Test
  void wksCfg031_duplicateStageId() {
    var r =
        validate(
            GREEN.replace(
                "listColumns: [applicant_name]",
                "stages: [intake, intake]\nlistColumns: [applicant_name]"));
    assertThat(r.isInvalid()).isTrue();
    assertThat(r.errors().stream().map(e -> e.code())).contains("WKS-CFG-031");
  }

  // ---- WKS-CFG-032 invalid pattern ----

  @Test
  void wksCfg032_invalidStageIdPattern() {
    var r =
        validate(
            GREEN.replace(
                "listColumns: [applicant_name]",
                "stages: [\"Intake!\"]\nlistColumns: [applicant_name]"));
    assertThat(r.isInvalid()).isTrue();
    assertThat(r.errors().stream().map(e -> e.code())).contains("WKS-CFG-032");
  }

  // ---- WKS-CFG-033 reserved word ----

  @Test
  void wksCfg033_reservedWordCase() {
    var r =
        validate(
            GREEN.replace(
                "listColumns: [applicant_name]", "stages: [case]\nlistColumns: [applicant_name]"));
    assertThat(r.isInvalid()).isTrue();
    assertThat(r.errors().stream().map(e -> e.code())).contains("WKS-CFG-033");
  }

  @Test
  void wksCfg033_reservedWordStage() {
    var r =
        validate(
            GREEN.replace(
                "listColumns: [applicant_name]", "stages: [stage]\nlistColumns: [applicant_name]"));
    assertThat(r.isInvalid()).isTrue();
    assertThat(r.errors().stream().map(e -> e.code())).contains("WKS-CFG-033");
  }

  // ---- WKS-CFG-007 stage displayName length (Story 3.1 code review S3) ----

  @Test
  void wksCfg007_stageDisplayNameOver40Chars() {
    String tooLong = "X".repeat(41);
    String yaml =
        GREEN.replace(
            "listColumns: [applicant_name]",
            "stages:\n"
                + "  - id: intake\n"
                + "    displayName: \""
                + tooLong
                + "\"\n"
                + "listColumns: [applicant_name]");
    var r = validate(yaml);
    assertThat(r.isInvalid()).isTrue();
    assertThat(r.errors().stream().map(e -> e.code())).contains("WKS-CFG-007");
    assertThat(r.errors().stream().map(e -> e.field()))
        .anyMatch(f -> f != null && f.endsWith(".displayName"));
  }

  @Test
  void wksCfg007_stageDisplayNameAt40CharsAccepted() {
    String exact = "Y".repeat(40);
    String yaml =
        GREEN.replace(
            "listColumns: [applicant_name]",
            "stages:\n"
                + "  - id: intake\n"
                + "    displayName: \""
                + exact
                + "\"\n"
                + "listColumns: [applicant_name]");
    var r = validate(yaml);
    assertThat(r.isInvalid()).isFalse();
    assertThat(r.config().get().stages().get(0).displayName()).isEqualTo(exact);
  }

  // ---- AC12 §9 — collect-all combo ----

  @Test
  void collectsAllThreeStageErrorsInOnePass() {
    var r =
        validate(
            GREEN.replace(
                "listColumns: [applicant_name]",
                "stages:\n"
                    + "  - id: intake\n"
                    + "  - id: intake\n" // duplicate -> WKS-CFG-031
                    + "  - id: \"Bad Stage\"\n" // pattern -> WKS-CFG-032
                    + "  - id: case\n" // reserved -> WKS-CFG-033
                    + "listColumns: [applicant_name]"));
    assertThat(r.isInvalid()).isTrue();
    var codes = r.errors().stream().map(e -> e.code()).distinct().toList();
    assertThat(codes).contains("WKS-CFG-031", "WKS-CFG-032", "WKS-CFG-033");
  }

  // ---- helpers ----

  private ValidationResult validate(String yaml) {
    var read = loader.readBytes("test", yaml.getBytes(StandardCharsets.UTF_8));
    if (!read.isParsed()) {
      return ValidationResult.invalid(read.errors());
    }
    return validator.validate(read.raw(), read.lines());
  }

  private static final String GREEN =
      """
      id: loan-application
      displayName: Loan Application
      version: 1
      workflows:
        bpmn: loan-application.bpmn
      fields:
        - id: applicant_name
          displayName: Applicant name
          type: text
      statuses:
        - id: open
          displayName: Open
      listColumns: [applicant_name]
      roles:
        - name: officer
          permissions: [view, create]
      """;
}
