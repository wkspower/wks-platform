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
            workflows:
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
        workflows:
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
        workflows:
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

  // ---- Patch 2: blank displayName triggers WKS-CFG-001 ----

  @Test
  void wksCfg001_blankDisplayName() {
    var result =
        validate(GREEN_YAML.replace("displayName: Loan Application", "displayName: \"   \""));
    assertErrorOn(result.errors(), "WKS-CFG-001", "displayName");
  }

  // ---- Patch 16: version: 0 → WKS-CFG-002, missing version → WKS-CFG-001 ----

  @Test
  void wksCfg002_versionZero() {
    var result = validate(GREEN_YAML.replace("version: 1", "version: 0"));
    assertErrorOn(result.errors(), "WKS-CFG-002", "version");
  }

  @Test
  void wksCfg001_missingVersion() {
    var result = validate(GREEN_YAML.replace("version: 1\n", ""));
    assertErrorOn(result.errors(), "WKS-CFG-001", "version");
  }

  // ---- Story 3.2 follow-up: omitted `fields` is legal (zero-process case types) ----

  @Test
  void missingFieldsIsLegalAndSkipsListColumnsCrossRef() {
    // Original "Patch 17" test pinned WKS-CFG-001 on missing `fields`. Story 3.2 made
    // zero-process / "name + tracker" case types first-class, so `fields:` is now optional and
    // defaults to an empty list. The cross-ref skip when fields is absent is preserved.
    String yaml =
        """
        id: loan-application
        displayName: Loan Application
        version: 1
        workflows:
          bpmn: x.bpmn
        statuses:
          - id: open
            displayName: Open
        listColumns: [foo, bar]
        roles:
          - name: officer
            permissions: [view]
        """;
    var result = validate(yaml);
    long fieldErr =
        result.errors().stream()
            .filter(e -> "WKS-CFG-001".equals(e.code()) && "fields".equals(e.field()))
            .count();
    assertThat(fieldErr).as("`fields` is now optional — no WKS-CFG-001").isZero();
    long unknownRef = result.errors().stream().filter(e -> "WKS-CFG-005".equals(e.code())).count();
    assertThat(unknownRef).as("no unknown-field cascade when fields is absent").isZero();
  }

  @Test
  void smallestValidCaseType_noFieldsNoListColumnsNoStatusesNoWorkflow_isValid() {
    // The Story 3.2 headline shape: id + displayName + version + roles. Defaults applied
    // everywhere else. Was failing in seed.sh against j9-zero-zero with WKS-CFG-001 on both
    // `fields` and `listColumns`; this test locks the relaxation.
    String yaml =
        """
        id: j9-zero-zero
        displayName: "[J9] Zero"
        version: 1
        roles:
          - name: admin
            permissions: [view, create, edit, transition]
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid())
        .as("smallest valid case type must validate cleanly: errors = " + result.errors())
        .isFalse();
    var ct = result.config().orElseThrow();
    assertThat(ct.fields()).isEmpty();
    assertThat(ct.listColumns()).isEmpty();
  }

  // ---- Patch 23: description > 400 chars → WKS-CFG-007 ----

  @Test
  void wksCfg007_descriptionTooLong() {
    String huge = "x".repeat(401);
    var result =
        validate(GREEN_YAML.replace("description: Retail consumer loans", "description: " + huge));
    assertErrorOn(result.errors(), "WKS-CFG-007", "description");
  }

  // ---- Patch 7: duplicate SELECT option values ----

  @Test
  void wksCfg003_duplicateSelectOptionValues() {
    String yaml =
        """
        id: loan-application
        displayName: Loan Application
        version: 1
        workflows:
          bpmn: x.bpmn
        fields:
          - id: tier
            displayName: Tier
            type: select
            options:
              - label: Gold
                value: g
              - label: Gold2
                value: g
        statuses:
          - id: open
            displayName: Open
        listColumns: [tier]
        roles:
          - name: officer
            permissions: [view]
        """;
    var result = validate(yaml);
    assertErrorOn(result.errors(), "WKS-CFG-003", "fields[0].options[1].value");
  }

  // ---- Patch 8: blank option label/value ----

  @Test
  void wksCfg001_blankSelectOptionLabel() {
    String yaml =
        """
        id: loan-application
        displayName: Loan Application
        version: 1
        workflows:
          bpmn: x.bpmn
        fields:
          - id: tier
            displayName: Tier
            type: select
            options:
              - label: "  "
                value: g
        statuses:
          - id: open
            displayName: Open
        listColumns: [tier]
        roles:
          - name: officer
            permissions: [view]
        """;
    var result = validate(yaml);
    assertErrorOn(result.errors(), "WKS-CFG-001", "fields[0].options[0].label");
  }

  // ---- Patch 28: unknown permission message includes allow-list ----

  @Test
  void wksCfg008_unknownPermissionMessageListsAllowed() {
    var result = validate(GREEN_YAML.replace("permissions: [view, create]", "permissions: [fly]"));
    var e = assertErrorOn(result.errors(), "WKS-CFG-008", "roles[0].permissions[0]");
    assertThat(e.message()).contains("allowed:");
    assertThat(e.message()).contains("view");
  }

  // ---- Patch 21: line numbers stable for selected per-code tests ----

  @Test
  void wksCfg002_unknownFieldType_linesAt() {
    var result = validate(GREEN_YAML.replace("type: text", "type: banana"));
    var e = assertErrorOn(result.errors(), "WKS-CFG-002", "fields[0].type");
    assertThat(e.line()).isNotNull().isPositive();
  }

  @Test
  void wksCfg005_listColumnUnknown_lineAt() {
    var result =
        validate(GREEN_YAML.replace("listColumns: [applicant_name]", "listColumns: [nope]"));
    var e = assertErrorOn(result.errors(), "WKS-CFG-005", "listColumns[0]");
    assertThat(e.line()).isNotNull().isPositive();
  }

  // ---- Patch 3: oversize input → WKS-CFG-099 ----

  @Test
  void wksCfg099_oversizeYamlBytes() {
    byte[] huge = new byte[CaseTypeYamlLoader.MAX_YAML_BYTES + 1];
    java.util.Arrays.fill(huge, (byte) ' ');
    var r = loader.readBytes("huge", huge);
    assertThat(r.isParsed()).isFalse();
    assertThat(r.errors()).hasSize(1);
    assertThat(r.errors().get(0).code()).isEqualTo("WKS-CFG-099");
  }

  // ---- Patch 4: duplicate top-level key → WKS-CFG-099 ----

  @Test
  void wksCfg099_duplicateTopLevelKey() {
    String yaml = "id: a\nid: b\n";
    var r = loader.readBytes("dup", yaml.getBytes(StandardCharsets.UTF_8));
    assertThat(r.isParsed()).isFalse();
    assertThat(r.errors().get(0).code()).isEqualTo("WKS-CFG-099");
  }

  // ---- Patch 5: multi-document YAML → WKS-CFG-099 ----

  @Test
  void wksCfg099_multiDocumentYaml() {
    String yaml = "id: a\n---\nid: b\n";
    var r = loader.readBytes("multi", yaml.getBytes(StandardCharsets.UTF_8));
    assertThat(r.isParsed()).isFalse();
    assertThat(r.errors().get(0).code()).isEqualTo("WKS-CFG-099");
  }

  // ---- Patch 6: scalar coercion off — number → string ----

  @Test
  void wksCfg099_scalarCoercionOff() {
    String yaml =
        """
        id: loan-application
        displayName: Loan Application
        version: 1
        workflows:
          bpmn: x.bpmn
        fields:
          - id: tier
            displayName: Tier
            type: select
            options:
              - label: Gold
                value: 42
        statuses:
          - id: open
            displayName: Open
        listColumns: [tier]
        roles:
          - name: officer
            permissions: [view]
        """;
    var r = loader.readBytes("coerce", yaml.getBytes(StandardCharsets.UTF_8));
    assertThat(r.isParsed()).isFalse();
    assertThat(r.errors().get(0).code()).isEqualTo("WKS-CFG-099");
  }

  // ---- Patch 11: null bytes → WKS-CFG-099 ----

  @Test
  void wksCfg099_nullBytes() {
    var r = loader.readBytes("null", null);
    assertThat(r.isParsed()).isFalse();
    assertThat(r.errors().get(0).code()).isEqualTo("WKS-CFG-099");
  }

  // ---- Patch 13: anchors / aliases → WKS-CFG-099 ----

  @Test
  void wksCfg099_yamlAnchorsRejected() {
    String yaml =
        """
        defaults: &defaults
          displayName: Loan
        id: loan-application
        <<: *defaults
        version: 1
        workflows:
          bpmn: x.bpmn
        fields:
          - id: a
            displayName: A
            type: text
        statuses:
          - id: open
            displayName: Open
        listColumns: [a]
        roles:
          - name: officer
            permissions: [view]
        """;
    var r = loader.readBytes("anchor", yaml.getBytes(StandardCharsets.UTF_8));
    assertThat(r.isParsed()).isFalse();
    assertThat(r.errors().get(0).code()).isEqualTo("WKS-CFG-099");
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
        workflows:
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

  // ---- Story 3.2 AC1 — workflow: is now OPTIONAL ----

  @Test
  void story32_workflowOmitted_validates() {
    String yaml =
        """
        id: zero-zero
        displayName: Zero Zero
        version: 1
        fields:
          - id: subject
            displayName: Subject
            type: text
        statuses:
          - id: open
            displayName: Open
        listColumns: [subject]
        roles:
          - name: officer
            permissions: [view, create]
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid())
        .as("workflow-omitted YAML must NOT raise WKS-CFG-001 — got %s", result.errors())
        .isFalse();
    assertThat(result.config()).isPresent();
    assertThat(result.config().get().workflow()).isNull();
    assertThat(result.config().get().workflowOpt()).isEmpty();
  }

  @Test
  void story32_workflowPresentButBlankBpmn_validates() {
    String yaml =
        """
        id: zero-zero
        displayName: Zero Zero
        version: 1
        workflows:
          bpmn: ""
        fields:
          - id: subject
            displayName: Subject
            type: text
        statuses:
          - id: open
            displayName: Open
        listColumns: [subject]
        roles:
          - name: officer
            permissions: [view, create]
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid())
        .as("workflow.bpmn blank must be treated as omitted — got %s", result.errors())
        .isFalse();
    assertThat(result.config().get().workflowOpt()).isEmpty();
  }

  @Test
  void story32_workflowDeclared_stillValidatesAsToday() {
    var result = validate(GREEN_YAML);
    assertThat(result.isInvalid()).isFalse();
    assertThat(result.config().get().workflowOpt()).isPresent();
    assertThat(result.config().get().workflowOpt().get().bpmn()).isEqualTo("loan-application.bpmn");
  }

  // ---- Story 3.2 AC8 — default status set when YAML omits statuses ----

  @Test
  void story32_statusesOmitted_defaultsToOpenClosed() {
    String yaml =
        """
        id: zero-zero
        displayName: Zero Zero
        version: 1
        fields:
          - id: subject
            displayName: Subject
            type: text
        listColumns: [subject]
        roles:
          - name: officer
            permissions: [view, create]
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid())
        .as("statuses-omitted YAML must use the default — got %s", result.errors())
        .isFalse();
    var statuses = result.config().get().statuses();
    assertThat(statuses).hasSize(2);
    assertThat(statuses.get(0).id()).isEqualTo("open");
    assertThat(statuses.get(0).terminal()).isFalse();
    assertThat(statuses.get(1).id()).isEqualTo("closed");
    // Story 3.6 AC1 — closed defaults to terminal:true (deferred from Story 3.2).
    assertThat(statuses.get(1).terminal()).isTrue();
  }

  // ---- Story 3.6 — stage-scoped status sets (AC2, AC3, AC1) ----

  @Test
  void story36_stageScopedStatuses_parsesAndExposesTerminal() {
    String yaml =
        """
        id: ct-stage-status
        displayName: CT Stage Status
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
          - { id: closed, displayName: Closed, color: zinc }
        listColumns: []
        roles:
          - name: officer
            permissions: [view, create]
        stages:
          - id: underwriting
            displayName: Underwriting
            statuses:
              - { id: pending-docs, displayName: Pending, color: amber }
              - { id: in-review, displayName: In Review, color: blue }
              - { id: ready-for-decision, displayName: Ready, color: emerald, terminal: true }
            initialStatus: pending-docs
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).as("got %s", result.errors()).isFalse();
    var stage = result.config().get().stage("underwriting").orElseThrow();
    assertThat(stage.statusesOpt()).isPresent();
    assertThat(stage.statusesOpt().get()).hasSize(3);
    assertThat(stage.statusesOpt().get().get(2).terminal()).isTrue();
    assertThat(stage.initialStatus()).contains("pending-docs");
  }

  @Test
  void story36_stageScopedStatuses_initialStatusDefaultsToFirstDeclared() {
    String yaml =
        """
        id: ct-init-default
        displayName: CT Init
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
          - { id: closed, displayName: Closed, color: zinc }
        listColumns: []
        roles:
          - name: officer
            permissions: [view, create]
        stages:
          - id: intake
            displayName: Intake
            statuses:
              - { id: collecting, displayName: Collecting, color: blue }
              - { id: ready, displayName: Ready, color: emerald, terminal: true }
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).as("got %s", result.errors()).isFalse();
    var stage = result.config().get().stage("intake").orElseThrow();
    assertThat(stage.initialStatus()).contains("collecting");
  }

  @Test
  void story36_stageOmittingStatuses_fallsBackToFlatSet() {
    String yaml =
        """
        id: ct-flat-fallback
        displayName: Flat
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
          - { id: closed, displayName: Closed, color: zinc }
        listColumns: []
        roles:
          - name: officer
            permissions: [view]
        stages:
          - id: only-stage
            displayName: Only
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).as("got %s", result.errors()).isFalse();
    var ct = result.config().get();
    var stage = ct.stage("only-stage").orElseThrow();
    assertThat(stage.statusesOpt()).isEmpty();
    // Resolver returns flat fallback.
    assertThat(ct.statusesFor("only-stage")).hasSize(2);
    assertThat(ct.statusesFor("only-stage").get(0).id()).isEqualTo("open");
  }

  @Test
  void story36_duplicateStatusIdInStage_emitsWksStg005() {
    String yaml =
        """
        id: ct-dup
        displayName: Dup
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
          - { id: closed, displayName: Closed, color: zinc }
        listColumns: []
        roles:
          - name: officer
            permissions: [view]
        stages:
          - id: stage-a
            displayName: A
            statuses:
              - { id: dup, displayName: One, color: blue }
              - { id: dup, displayName: Two, color: amber }
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).isTrue();
    assertThat(result.errors().stream().anyMatch(e -> e.code().equals("WKS-STG-005"))).isTrue();
  }

  @Test
  void story36_initialStatusMissingTarget_emitsWksStg006() {
    String yaml =
        """
        id: ct-missing-init
        displayName: Mi
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
          - { id: closed, displayName: Closed, color: zinc }
        listColumns: []
        roles:
          - name: officer
            permissions: [view]
        stages:
          - id: stage-a
            displayName: A
            statuses:
              - { id: real, displayName: Real, color: blue }
            initialStatus: ghost
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).isTrue();
    assertThat(result.errors().stream().anyMatch(e -> e.code().equals("WKS-STG-006"))).isTrue();
  }

  @Test
  void story36_emptyStageStatusesList_emitsWksStg008() {
    String yaml =
        """
        id: ct-empty
        displayName: E
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
          - { id: closed, displayName: Closed, color: zinc }
        listColumns: []
        roles:
          - name: officer
            permissions: [view]
        stages:
          - id: stage-a
            displayName: A
            statuses: []
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).isTrue();
    assertThat(result.errors().stream().anyMatch(e -> e.code().equals("WKS-STG-008"))).isTrue();
  }

  @Test
  void story36_stageScopedStatusIdPatternViolation_emitsWksCfg032Reused() {
    // Q2 LOCKED — stage-scoped status id pattern violation reuses WKS-CFG-032; message
    // disambiguates ("Stage-scoped status id"). WKS-STG-007 is NOT introduced.
    String yaml =
        """
        id: ct-bad-id
        displayName: Bad
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
          - { id: closed, displayName: Closed, color: zinc }
        listColumns: []
        roles:
          - name: officer
            permissions: [view]
        stages:
          - id: stage-a
            displayName: A
            statuses:
              - { id: BAD_ID, displayName: Bad, color: blue }
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).isTrue();
    assertThat(result.errors().stream().anyMatch(e -> e.code().equals("WKS-CFG-032"))).isTrue();
  }

  // ---- helpers ----

  private ValidationResult validate(String yaml) {
    var r = loader.readBytes("test", yaml.getBytes(StandardCharsets.UTF_8));
    if (!r.isParsed()) {
      return ValidationResult.invalid(r.errors());
    }
    return validator.validate(r.raw(), r.lines());
  }

  // ---- Story 2.7 — requiredOnCreate grammar extension + WKS-CFG-013 ----

  @Test
  void requiredOnCreateDefaultsToRequiredWhenOmitted() {
    var result = validate(GREEN_YAML);
    assertThat(result.isInvalid()).isFalse();
    var fields = result.config().get().fields();
    var applicantName =
        fields.stream().filter(f -> f.id().equals("applicant_name")).findFirst().get();
    var loanAmount = fields.stream().filter(f -> f.id().equals("loan_amount")).findFirst().get();
    assertThat(applicantName.required()).isTrue();
    assertThat(applicantName.requiredOnCreate())
        .as("applicant_name has required:true and no requiredOnCreate slot — defaults to required")
        .isTrue();
    assertThat(loanAmount.required()).isFalse();
    assertThat(loanAmount.requiredOnCreate()).isFalse();
  }

  @Test
  void requiredOnCreateExplicitFalseOnRequiredFieldIsHonored() {
    var yaml =
        GREEN_YAML.replace(
            "    type: text\n    required: true",
            "    type: text\n    required: true\n    requiredOnCreate: false");
    var result = validate(yaml);
    assertThat(result.isInvalid()).isFalse();
    var f =
        result.config().get().fields().stream()
            .filter(x -> x.id().equals("applicant_name"))
            .findFirst()
            .get();
    assertThat(f.required()).isTrue();
    assertThat(f.requiredOnCreate()).isFalse();
  }

  @Test
  void wksCfg013_fileFieldRequiredOnCreateEmitsWarning() {
    var yaml =
        GREEN_YAML.replace(
            "  - id: loan_amount\n    displayName: Loan amount\n    type: number",
            "  - id: id_proof\n    displayName: ID proof\n    type: file\n"
                + "    requiredOnCreate: true");
    var result = validate(yaml);
    assertThat(result.isInvalid()).as("WKS-CFG-013 is a warning, not a blocking error").isFalse();
    assertThat(result.warnings()).hasSize(1);
    assertThat(result.warnings().get(0).code()).isEqualTo("WKS-CFG-013");
    assertThat(result.warnings().get(0).field()).contains("requiredOnCreate");
    assertThat(result.config()).isPresent();
  }

  private ErrorDetail assertErrorOn(List<ErrorDetail> errors, String code, String field) {
    var match =
        errors.stream().filter(e -> code.equals(e.code()) && field.equals(e.field())).findFirst();
    assertThat(match).as("expected error %s on field %s — got %s", code, field, errors).isPresent();
    return match.get();
  }

  // ---- Story 6.1 — YAML archetype validation (AC1) ----

  @Test
  void story61_formArchetypeUnknown_emitsWksArch001() {
    String yaml =
        """
        id: ct-arch
        displayName: Arch Test
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
        listColumns: []
        roles:
          - name: officer
            permissions: [view]
        forms:
          definitions:
            - id: intake-form
              topology: single
              dataModel: monolithic
              rendering: single-page
              archetype: unknown_archetype
              fields: []
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).isTrue();
    var e = assertErrorOn(result.errors(), "WKS-ARCH-001", "/forms/0/archetype");
    assertThat(e.message()).contains("unknown_archetype");
    assertThat(e.message()).contains("draft_section");
  }

  @Test
  void story61_formArchetypeDraftSection_passes() {
    String yaml =
        """
        id: ct-arch
        displayName: Arch Test
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
        listColumns: []
        roles:
          - name: officer
            permissions: [view]
        forms:
          definitions:
            - id: intake-form
              topology: single
              dataModel: monolithic
              rendering: single-page
              archetype: draft_section
              fields: []
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).as("draft_section is a valid archetype").isFalse();
  }

  @Test
  void story61_formArchetypeSubmitForProcessing_passes() {
    String yaml =
        """
        id: ct-arch
        displayName: Arch Test
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
        listColumns: []
        roles:
          - name: officer
            permissions: [view]
        forms:
          definitions:
            - id: intake-form
              topology: single
              dataModel: monolithic
              rendering: single-page
              archetype: submit_for_processing
              fields: []
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).as("submit_for_processing is a valid archetype").isFalse();
  }

  @Test
  void story61_formArchetypeBusinessFinal_passes() {
    String yaml =
        """
        id: ct-arch
        displayName: Arch Test
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
        listColumns: []
        roles:
          - name: officer
            permissions: [view]
        forms:
          definitions:
            - id: intake-form
              topology: single
              dataModel: monolithic
              rendering: single-page
              archetype: business_final
              fields: []
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).as("business_final is a valid archetype").isFalse();
  }

  @Test
  void story61_formArchetypeOmitted_passes() {
    String yaml =
        """
        id: ct-arch
        displayName: Arch Test
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
        listColumns: []
        roles:
          - name: officer
            permissions: [view]
        forms:
          definitions:
            - id: intake-form
              topology: single
              dataModel: monolithic
              rendering: single-page
              fields: []
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).as("omitted archetype should not produce errors").isFalse();
  }

  @Test
  void story61_stageArchetypeUnknown_emitsWksArch001() {
    String yaml =
        """
        id: ct-stage-arch
        displayName: Stage Arch Test
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
          - { id: closed, displayName: Closed, color: zinc }
        listColumns: []
        roles:
          - name: officer
            permissions: [view]
        stages:
          - id: intake
            displayName: Intake
          - id: review
            displayName: Review
          - id: final
            displayName: Final
            archetype: not_a_real_archetype
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).isTrue();
    var e = assertErrorOn(result.errors(), "WKS-ARCH-001", "/stages/2/archetype");
    assertThat(e.message()).contains("not_a_real_archetype");
  }

  @Test
  void story61_stageArchetypeOmitted_passes() {
    String yaml =
        """
        id: ct-stage-arch
        displayName: Stage Arch Test
        version: 1
        statuses:
          - { id: open, displayName: Open, color: blue }
          - { id: closed, displayName: Closed, color: zinc }
        listColumns: []
        roles:
          - name: officer
            permissions: [view]
        stages:
          - id: intake
            displayName: Intake
          - id: final
            displayName: Final
        """;
    var result = validate(yaml);
    assertThat(result.isInvalid()).as("omitted stage archetype should not produce errors").isFalse();
  }

  private static final String GREEN_YAML =
      """
      id: loan-application
      displayName: Loan Application
      version: 1
      description: Retail consumer loans
      workflows:
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
