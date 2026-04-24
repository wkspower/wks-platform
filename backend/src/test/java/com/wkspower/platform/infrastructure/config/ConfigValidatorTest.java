package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.exception.ErrorDetail;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * One test per WKS-CFG code on the Story 2.1 surface. Each test asserts the EXACT wire string, the
 * dotted field path, and (where applicable) a non-null line number — per AC4/AC8.
 */
class ConfigValidatorTest {

  private final CaseTypeYamlLoader loader = new CaseTypeYamlLoader();
  private final ConfigValidator validator = new ConfigValidator();

  // ---- happy path ----

  @Test
  void happyPathProducesValidConfig() {
    var result = validate(GREEN_YAML);
    assertThat(result.isInvalid()).as("green YAML should produce no errors").isFalse();
    assertThat(result.config()).isPresent();
    assertThat(result.config().get().id()).isEqualTo("loan-application");
    assertThat(result.config().get().fields()).hasSize(2);
  }

  // ---- WKS-CFG-001 required key missing ----

  @Test
  void wksCfg001_missingTopLevelId() {
    var result = validate(GREEN_YAML.replace("id: loan-application\n", ""));
    assertErrorOn(result.errors(), "WKS-CFG-001", "id");
  }

  @Test
  void wksCfg001_missingFieldDisplayName() {
    var result =
        validate(
            """
            id: loan-application
            displayName: Loan Application
            version: 1
            workflow:
              bpmn: x.bpmn
            fields:
              - id: applicant_name
                type: text
            statuses:
              - id: open
                displayName: Open
            listColumns: [applicant_name]
            roles:
              - name: officer
                permissions: [view]
            """);
    assertErrorOn(result.errors(), "WKS-CFG-001", "fields[0].displayName");
  }

  // ---- WKS-CFG-002 invalid field type ----

  @Test
  void wksCfg002_unknownFieldType() {
    var result = validate(GREEN_YAML.replace("type: text", "type: banana"));
    var e = assertErrorOn(result.errors(), "WKS-CFG-002", "fields[0].type");
    assertThat(e.line()).isNotNull();
  }

  // ---- WKS-CFG-003 duplicate id ----

  @Test
  void wksCfg003_duplicateFieldId() {
    var result =
        validate(
            GREEN_YAML.replace(
                "  - id: loan_amount\n    displayName: Loan amount\n    type: number",
                "  - id: applicant_name\n    displayName: Dup\n    type: number"));
    assertErrorOn(result.errors(), "WKS-CFG-003", "fields[1].id");
  }

  // ---- WKS-CFG-004 fields over limit ----

  @Test
  void wksCfg004_tooManyFields() {
    StringBuilder fields = new StringBuilder();
    for (int i = 0; i < 51; i++) {
      fields.append("  - id: f").append(i).append("\n");
      fields.append("    displayName: F").append(i).append("\n");
      fields.append("    type: text\n");
    }
    String yaml =
        """
        id: loan-application
        displayName: Loan Application
        version: 1
        workflow:
          bpmn: x.bpmn
        fields:
        """
            + fields
            + """
            statuses:
              - id: open
                displayName: Open
            listColumns: [f0]
            roles:
              - name: officer
                permissions: [view]
            """;
    var result = validate(yaml);
    assertErrorOn(result.errors(), "WKS-CFG-004", "fields");
  }

  // ---- WKS-CFG-005 listColumns over limit / unknown reference ----

  @Test
  void wksCfg005_listColumnReferencesUnknownField() {
    var result =
        validate(GREEN_YAML.replace("listColumns: [applicant_name]", "listColumns: [nope]"));
    assertErrorOn(result.errors(), "WKS-CFG-005", "listColumns[0]");
  }

  // ---- WKS-CFG-006 statuses over limit ----

  @Test
  void wksCfg006_tooManyStatuses() {
    StringBuilder statuses = new StringBuilder();
    for (int i = 0; i < 11; i++) {
      statuses.append("  - id: s").append(i).append("\n");
      statuses.append("    displayName: S").append(i).append("\n");
    }
    String yaml =
        """
        id: loan-application
        displayName: Loan Application
        version: 1
        workflow:
          bpmn: x.bpmn
        fields:
          - id: a
            displayName: A
            type: text
        statuses:
        """
            + statuses
            + """
            listColumns: [a]
            roles:
              - name: officer
                permissions: [view]
            """;
    var result = validate(yaml);
    assertErrorOn(result.errors(), "WKS-CFG-006", "statuses");
  }

  // ---- WKS-CFG-007 displayName too long ----

  @Test
  void wksCfg007_displayNameOverLimit() {
    String longName = "x".repeat(41);
    var result = validate(GREEN_YAML.replace("Loan Application", longName));
    assertErrorOn(result.errors(), "WKS-CFG-007", "displayName");
  }

  // ---- WKS-CFG-008 unknown enum literal ----

  @Test
  void wksCfg008_unknownStatusColor() {
    var result = validate(GREEN_YAML.replace("color: amber", "color: fuchsia"));
    assertErrorOn(result.errors(), "WKS-CFG-008", "statuses[0].color");
  }

  @Test
  void wksCfg008_unknownPermission() {
    var result = validate(GREEN_YAML.replace("permissions: [view, create]", "permissions: [fly]"));
    assertErrorOn(result.errors(), "WKS-CFG-008", "roles[0].permissions[0]");
  }

  // ---- WKS-CFG-009 malformed id ----

  @Test
  void wksCfg009_malformedTopLevelId() {
    var result = validate(GREEN_YAML.replace("id: loan-application", "id: LoanApp"));
    assertErrorOn(result.errors(), "WKS-CFG-009", "id");
  }

  // ---- WKS-CFG-099 catastrophic parse ----

  @Test
  void wksCfg099_yamlParseError() {
    var readResult = loader.readBytes("test", "id: [unclosed".getBytes(StandardCharsets.UTF_8));
    assertThat(readResult.isParsed()).isFalse();
    assertThat(readResult.errors()).hasSize(1);
    assertThat(readResult.errors().get(0).code()).isEqualTo("WKS-CFG-099");
  }

  // ---- Collect-all: multiple errors at once ----

  @Test
  void collectAllCollectsEveryErrorInOnePass() {
    String yaml =
        """
        id: BadId
        displayName: ThisIsDefinitelyWayTooLongForAFortyCharDisplayNameYes
        version: 0
        workflow:
          bpmn: x.bpmn
        fields:
          - id: a
            displayName: A
            type: banana
        statuses:
          - id: open
            displayName: Open
            color: fuchsia
        listColumns: [nope]
        roles:
          - name: BadRole
            permissions: [fly]
        """;
    var result = validate(yaml);
    // Do not assert exact count — assert the distinct codes present. This makes the test
    // resilient to minor error-message additions yet still proves collect-all works.
    List<String> codes = result.errors().stream().map(ErrorDetail::code).distinct().toList();
    assertThat(codes)
        .contains(
            "WKS-CFG-002", // bad field type
            "WKS-CFG-005", // listColumns unknown ref
            "WKS-CFG-007", // displayName too long
            "WKS-CFG-008", // bad enum
            "WKS-CFG-009"); // bad id
    assertThat(result.errors().size()).isGreaterThanOrEqualTo(5);
  }

  // ---- helpers ----

  private ValidationResult validate(String yaml) {
    var r = loader.readBytes("test", yaml.getBytes(StandardCharsets.UTF_8));
    if (!r.isParsed()) {
      return ValidationResult.invalid(r.errors());
    }
    return validator.validate(r.raw(), r.lines());
  }

  private ErrorDetail assertErrorOn(List<ErrorDetail> errors, String code, String field) {
    var match =
        errors.stream().filter(e -> code.equals(e.code()) && field.equals(e.field())).findFirst();
    assertThat(match).as("expected error %s on field %s — got %s", code, field, errors).isPresent();
    return match.get();
  }

  private static final String GREEN_YAML =
      """
      id: loan-application
      displayName: Loan Application
      version: 1
      description: Retail consumer loans
      workflow:
        bpmn: loan-application.bpmn
      fields:
        - id: applicant_name
          displayName: Applicant name
          type: text
          required: true
        - id: loan_amount
          displayName: Loan amount
          type: number
      statuses:
        - id: open
          displayName: Open
          color: amber
      listColumns: [applicant_name]
      roles:
        - name: loan-officer
          permissions: [view, create]
      """;
}
