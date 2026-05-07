package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.engine.BpmnParser;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Story 4.2 AC10 — collect-all wire-code coverage for {@link MappingValidator}. Mirrors the {@code
 * ConfigValidatorStagesTest} idiom: every "wrong YAML" test asserts the error list
 * <em>contains</em> the expected wire code, never asserts list size unless explicitly testing
 * collect-all.
 */
class MappingValidatorTest {

  private static final String BPMN_WITH_REVIEW_AND_END =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                        targetNamespace="http://wkspower.test">
        <bpmn:signal id="claim-escalated" name="ClaimEscalated"/>
        <bpmn:process id="claim" isExecutable="true">
          <bpmn:startEvent id="start"/>
          <bpmn:userTask id="review-claim" name="Review Claim"/>
          <bpmn:endEvent id="done"/>
          <bpmn:sequenceFlow id="f1" sourceRef="start" targetRef="review-claim"/>
          <bpmn:sequenceFlow id="f2" sourceRef="review-claim" targetRef="done"/>
        </bpmn:process>
      </bpmn:definitions>
      """;

  private static final String BPMN_NO_END_EVENT =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                        targetNamespace="http://wkspower.test">
        <bpmn:process id="claim" isExecutable="true">
          <bpmn:startEvent id="start"/>
          <bpmn:userTask id="review-claim" name="Review"/>
        </bpmn:process>
      </bpmn:definitions>
      """;

  private final BpmnParser parser = new BpmnParser();
  private final MappingValidator validator = new MappingValidator(parser);

  // ---- AC1 zero-attachments / null attachments are legal ----

  @Test
  void emptyAttachmentsListIsLegal() {
    var raw = parseYaml(GREEN_HEAD + "\nattachments: []\n");
    var result = validator.validate(raw, Set.of("intake"), Map.of());
    assertThat(result.errors()).isEmpty();
    assertThat(result.definition().attachments()).isEmpty();
  }

  @Test
  void omittedAttachmentsKeyIsLegal() {
    var raw = parseYaml(GREEN_HEAD);
    var result = validator.validate(raw, Set.of("intake"), Map.of());
    assertThat(result.errors()).isEmpty();
    assertThat(result.definition().attachments()).isEmpty();
  }

  // ---- WKS-MAP-004 — type allow-list ----

  @Test
  void wksMap004_unknownAttachmentType() {
    var raw = parseYaml(GREEN_HEAD + attach("state-machine", "x.bpmn", "case", null));
    var result = validator.validate(raw, Set.of("intake"), Map.of());
    assertCodes(result.errors()).contains("WKS-MAP-004");
  }

  // ---- WKS-MAP-005 — file missing/unprovided ----

  @Test
  void wksMap005_fileNotProvided() {
    var raw = parseYaml(GREEN_HEAD + attach("bpmn", "missing.bpmn", "case", null));
    var result = validator.validate(raw, Set.of("intake"), Map.of());
    assertCodes(result.errors()).contains("WKS-MAP-005");
  }

  @Test
  void wksMap005_bpmnParseFails() {
    var raw = parseYaml(GREEN_HEAD + attach("bpmn", "garbage.bpmn", "case", null));
    Map<String, byte[]> files =
        Map.of("garbage.bpmn", "this is not bpmn".getBytes(StandardCharsets.UTF_8));
    var result = validator.validate(raw, Set.of("intake"), files);
    assertCodes(result.errors()).contains("WKS-MAP-005");
  }

  // ---- WKS-MAP-003 — unknown stage in scope or emits.scope ----

  @Test
  void wksMap003_unknownStageInScope() {
    var raw = parseYaml(GREEN_HEAD + attach("bpmn", "x.bpmn", "stage:nonexistent", null));
    var result =
        validator.validate(
            raw, Set.of("intake"), Map.of("x.bpmn", BPMN_WITH_REVIEW_AND_END.getBytes()));
    assertCodes(result.errors()).contains("WKS-MAP-003");
  }

  // ---- WKS-MAP-006 — duplicate scope across attachments ----

  @Test
  void wksMap006_duplicateScopeAcrossAttachments() {
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: a.bpmn\n    scope: case\n"
            + "  - type: bpmn\n    file: b.bpmn\n    scope: case\n";
    var raw = parseYaml(yaml);
    var result =
        validator.validate(
            raw,
            Set.of("intake"),
            Map.of(
                "a.bpmn",
                BPMN_WITH_REVIEW_AND_END.getBytes(),
                "b.bpmn",
                BPMN_WITH_REVIEW_AND_END.getBytes()));
    assertCodes(result.errors()).contains("WKS-MAP-006");
  }

  // ---- WKS-CFG-027 — userTask mapping references unknown BPMN userTask ----

  @Test
  void wksCfg027_userTaskMappingReferencesUnknownTask() {
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      userTasks:\n        ghost-task: { wksTask: \"Ghost\" }\n";
    var raw = parseYaml(yaml);
    var result =
        validator.validate(
            raw, Set.of("intake"), Map.of("x.bpmn", BPMN_WITH_REVIEW_AND_END.getBytes()));
    assertCodes(result.errors()).contains("WKS-CFG-027");
  }

  @Test
  void wksCfg027_propertyOnUserTaskReferencesUnknownTask() {
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      properties:\n"
            + "        - on: userTask:ghost-task\n"
            + "          camunda:property: status\n"
            + "          emits: { type: status, scope: case }\n";
    var raw = parseYaml(yaml);
    var result =
        validator.validate(
            raw, Set.of("intake"), Map.of("x.bpmn", BPMN_WITH_REVIEW_AND_END.getBytes()));
    assertCodes(result.errors()).contains("WKS-CFG-027");
  }

  // ---- WKS-MAP-001 — non-userTask BPMN element refs ----

  @Test
  void wksMap001_signalReferencesUnknownBpmnSignal() {
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      events:\n"
            + "        signal:\n"
            + "          ghost-signal: { stageTransition: \"intake -> completed\" }\n";
    var raw = parseYaml(yaml);
    var result =
        validator.validate(
            raw, Set.of("intake"), Map.of("x.bpmn", BPMN_WITH_REVIEW_AND_END.getBytes()));
    assertCodes(result.errors()).contains("WKS-MAP-001");
  }

  @Test
  void wksMap001_endEventRuleOnBpmnWithNoEndEvents() {
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      events:\n"
            + "        endEvent: { stageTransition: \"intake -> completed\" }\n";
    var raw = parseYaml(yaml);
    var result =
        validator.validate(raw, Set.of("intake"), Map.of("x.bpmn", BPMN_NO_END_EVENT.getBytes()));
    assertCodes(result.errors()).contains("WKS-MAP-001");
  }

  // ---- WKS-CFG-028 — stageTransition adjacency invalid ----

  @Test
  void wksCfg028_endEventStageTransitionUnknownStage() {
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      events:\n"
            + "        endEvent: { stageTransition: \"intake -> nonexistent\" }\n";
    var raw = parseYaml(yaml);
    var result =
        validator.validate(
            raw, Set.of("intake"), Map.of("x.bpmn", BPMN_WITH_REVIEW_AND_END.getBytes()));
    assertCodes(result.errors()).contains("WKS-CFG-028");
  }

  // ---- AC7 — JSON-pointer field paths ----

  @Test
  void errorDetailsCarryJsonPointerFieldPaths() {
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      userTasks:\n        ghost: { wksTask: \"G\" }\n";
    var raw = parseYaml(yaml);
    var result =
        validator.validate(
            raw, Set.of("intake"), Map.of("x.bpmn", BPMN_WITH_REVIEW_AND_END.getBytes()));
    assertThat(result.errors())
        .anySatisfy(e -> assertThat(e.field()).isEqualTo("/attachments/0/map/userTasks/ghost"));
  }

  // ---- AC2 — collect-all (multiple errors of different kinds in one pass) ----

  @Test
  void collectsAllThreeMappingErrorsInOnePass() {
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: stage:nonexistent\n" // WKS-MAP-003
            + "    routing:\n      userTasks:\n        ghost: { wksTask: \"G\" }\n" // WKS-CFG-027
            + "      events:\n"
            + "        endEvent: { stageTransition: \"intake -> nonexistent\" }\n"; // WKS-CFG-028
    var raw = parseYaml(yaml);
    var result =
        validator.validate(
            raw, Set.of("intake"), Map.of("x.bpmn", BPMN_WITH_REVIEW_AND_END.getBytes()));
    var codes = result.errors().stream().map(ErrorDetail::code).distinct().toList();
    assertThat(codes).contains("WKS-MAP-003", "WKS-CFG-027", "WKS-CFG-028");
  }

  // ---- WKS-CFG-028 — signal stageTransition adjacency invalid ----

  @Test
  void wksCfg028_signalStageTransitionUnknownToStage() {
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      events:\n"
            + "        signal:\n"
            + "          claim-escalated: { stageTransition: \"intake -> ghost\" }\n";
    var raw = parseYaml(yaml);
    var result =
        validator.validate(
            raw, Set.of("intake"), Map.of("x.bpmn", BPMN_WITH_REVIEW_AND_END.getBytes()));
    assertCodes(result.errors()).contains("WKS-CFG-028");
  }

  // ---- WKS-MAP-003 — unknown stage in emits.scope ----

  @Test
  void wksMap003_unknownStageInEmitsScope() {
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      properties:\n"
            + "        - on: userTask:review-claim\n"
            + "          camunda:property: status\n"
            + "          emits: { type: status, scope: stage:nonexistent }\n";
    var raw = parseYaml(yaml);
    var result =
        validator.validate(
            raw, Set.of("intake"), Map.of("x.bpmn", BPMN_WITH_REVIEW_AND_END.getBytes()));
    assertCodes(result.errors()).contains("WKS-MAP-003");
  }

  // ---- WKS-CFG-008 — unknown emits.type ----

  @Test
  void wksCfg008_unknownEmitsType() {
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      properties:\n"
            + "        - on: userTask:review-claim\n"
            + "          camunda:property: status\n"
            + "          emits: { type: bogus, scope: case }\n";
    var raw = parseYaml(yaml);
    var result =
        validator.validate(
            raw, Set.of("intake"), Map.of("x.bpmn", BPMN_WITH_REVIEW_AND_END.getBytes()));
    assertCodes(result.errors()).contains("WKS-CFG-008");
  }

  // ---- AC6 — Phase-0 type allow-list contains exactly 'bpmn' ----

  @Test
  void phase0AllowListAcceptsBpmnOnly() {
    var rawTemporal = parseYaml(GREEN_HEAD + attach("temporal", "x.bpmn", "case", null));
    var resTemporal = validator.validate(rawTemporal, Set.of("intake"), Map.of());
    assertCodes(resTemporal.errors()).contains("WKS-MAP-004");

    var rawStateMachine = parseYaml(GREEN_HEAD + attach("state-machine", "x.bpmn", "case", null));
    var resStateMachine = validator.validate(rawStateMachine, Set.of("intake"), Map.of());
    assertCodes(resStateMachine.errors()).contains("WKS-MAP-004");
  }

  // ---- AC9 / AC10 — positive smoke: a fully valid attachment passes ----

  @Test
  void greenSeedPassesWithZeroErrors() {
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      userTasks:\n"
            + "        review-claim: { wksTask: \"Underwriting Review\" }\n"
            + "      events:\n"
            + "        endEvent: { stageTransition: \"intake -> completed\" }\n"
            + "      properties:\n"
            + "        - on: userTask:review-claim\n"
            + "          camunda:property: status\n"
            + "          emits: { type: status, scope: case }\n";
    var raw = parseYaml(yaml);
    var result =
        validator.validate(
            raw, Set.of("intake"), Map.of("x.bpmn", BPMN_WITH_REVIEW_AND_END.getBytes()));
    assertThat(result.errors()).isEmpty();
    assertThat(result.definition().attachments()).hasSize(1);
    var attachment = result.definition().attachments().get(0);
    assertThat(attachment.type()).isEqualTo("bpmn");
    assertThat(attachment.scope()).isEqualTo("case");
    assertThat(attachment.userTaskMappings()).containsKey("review-claim");
    assertThat(attachment.endEventMapping()).isPresent();
    assertThat(attachment.propertyEmissionRules()).hasSize(1);
  }

  // ---- Story 4.3.1 AC3 — WKS-MAP-002 precedence-collision detection ----

  @Test
  void wksMap002_endEventAndSignalTargetingSameStageTransitionCollide() {
    // endEvent and a signal both produce the SAME stageTransition tuple — Phase-0 disallows.
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      events:\n"
            + "        endEvent: { stageTransition: \"intake -> completed\" }\n"
            + "        signal:\n"
            + "          claim-escalated: { stageTransition: \"intake -> completed\" }\n";
    var raw = parseYaml(yaml);
    var result =
        validator.validate(
            raw, Set.of("intake"), Map.of("x.bpmn", BPMN_WITH_REVIEW_AND_END.getBytes()));
    assertCodes(result.errors()).contains("WKS-MAP-002");
  }

  @Test
  void wksMap002_endEventAndSignalTargetingDifferentStageTransitionsAreLegal() {
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      events:\n"
            + "        endEvent: { stageTransition: \"intake -> completed\" }\n"
            + "        signal:\n"
            + "          claim-escalated: { stageTransition: \"intake -> skipped\" }\n";
    var raw = parseYaml(yaml);
    var result =
        validator.validate(
            raw, Set.of("intake"), Map.of("x.bpmn", BPMN_WITH_REVIEW_AND_END.getBytes()));
    assertCodes(result.errors()).doesNotContain("WKS-MAP-002");
  }

  // ---- Story 4.3.1 AC8 — WKS-MAP-008 unknown YAML key ----

  @Test
  void wksMap008_unknownKeyInMappingSubtreeIsRejected() {
    // Typo: "signl" instead of "signal" inside events block.
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      events:\n"
            + "        signl:\n"
            + "          claim-escalated: { stageTransition: \"intake -> completed\" }\n";
    var loader = new CaseTypeYamlLoader();
    var read = loader.readBytes("test", yaml.getBytes(StandardCharsets.UTF_8));
    assertThat(read.isParsed()).as("loader must REJECT unknown mapping key").isFalse();
    assertThat(read.errors())
        .as("AC8: unknown mapping subtree key surfaces as WKS-MAP-008")
        .anyMatch(e -> "WKS-MAP-008".equals(e.code()));
  }

  @Test
  void wksMap008_unknownKeyInEmitsBlockIsRejected() {
    // Typo: "typ" instead of "type" inside emits block.
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      properties:\n"
            + "        - on: userTask:review-claim\n"
            + "          camunda:property: status\n"
            + "          emits: { typ: status, scope: case }\n";
    var loader = new CaseTypeYamlLoader();
    var read = loader.readBytes("test", yaml.getBytes(StandardCharsets.UTF_8));
    assertThat(read.isParsed()).isFalse();
    assertThat(read.errors()).anyMatch(e -> "WKS-MAP-008".equals(e.code()));
  }

  // ---- Story 4.3.1 AC9 — WKS-MAP-009 duplicate YAML key ----

  @Test
  void wksMap009_duplicateMapKeyIsRejected() {
    // userTasks declares the same key twice — silent last-wins is forbidden.
    String yaml =
        GREEN_HEAD
            + "\nattachments:\n"
            + "  - type: bpmn\n    file: x.bpmn\n    scope: case\n"
            + "    routing:\n      userTasks:\n"
            + "        review-claim: { wksTask: \"First\" }\n"
            + "        review-claim: { wksTask: \"Second\" }\n";
    var loader = new CaseTypeYamlLoader();
    var read = loader.readBytes("test", yaml.getBytes(StandardCharsets.UTF_8));
    assertThat(read.isParsed()).as("loader must REJECT duplicate keys").isFalse();
    assertThat(read.errors()).anyMatch(e -> "WKS-MAP-009".equals(e.code()));
  }

  // ---- helpers ----

  private static org.assertj.core.api.AbstractListAssert<?, List<? extends String>, String, ?>
      assertCodes(List<ErrorDetail> errors) {
    return assertThat(errors.stream().map(ErrorDetail::code).toList());
  }

  private static String attach(String type, String file, String scope, String mapBlock) {
    StringBuilder sb = new StringBuilder("\nattachments:\n  - type: ");
    sb.append(type).append("\n    file: ").append(file).append("\n    scope: ").append(scope);
    if (mapBlock != null) {
      sb.append("\n    ").append(mapBlock);
    }
    sb.append('\n');
    return sb.toString();
  }

  private RawCaseTypeConfig parseYaml(String yaml) {
    var loader = new CaseTypeYamlLoader();
    var read = loader.readBytes("test", yaml.getBytes(StandardCharsets.UTF_8));
    if (!read.isParsed()) {
      throw new AssertionError(
          "YAML did not parse: " + read.errors().stream().map(ErrorDetail::message).toList());
    }
    return read.raw();
  }

  private static final String GREEN_HEAD =
      """
      id: claim
      displayName: Claim
      version: 1
      workflows:
        bpmn: claim.bpmn
      fields:
        - id: applicant
          displayName: Applicant
          type: text
      statuses:
        - id: open
          displayName: Open
      listColumns: [applicant]
      roles:
        - name: officer
          permissions: [view, create]
      stages:
        - id: intake
          displayName: Intake
      """;
}
