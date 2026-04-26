package com.wkspower.platform.engine;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.workflow.BpmnValidationResult;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class BpmnValidatorTest {

  private final BpmnValidator validator = new BpmnValidator(new BpmnParser());

  // -- WKS-CFG-010 -----------------------------------------------------------

  @Test
  void emptyBytesReturnWksCfg010() {
    BpmnValidationResult result =
        validator.validate(new byte[0], caseType("application", List.of()));

    assertThat(result.isInvalid()).isTrue();
    assertThat(result.errors()).hasSize(1);
    ErrorDetail error = result.errors().get(0);
    assertThat(error.code()).isEqualTo("WKS-CFG-010");
    assertThat(error.message()).isEqualTo("BPMN bytes empty");
  }

  @Test
  void nonBpmnXmlReturnsWksCfg010() {
    String notBpmn = "<?xml version=\"1.0\"?><foo/>";

    BpmnValidationResult result =
        validator.validate(
            notBpmn.getBytes(StandardCharsets.UTF_8), caseType("application", List.of()));

    assertThat(result.isInvalid()).isTrue();
    assertThat(result.errors()).hasSize(1);
    assertThat(result.errors().get(0).code()).isEqualTo("WKS-CFG-010");
  }

  // -- WKS-CFG-020 -----------------------------------------------------------

  @Test
  void userTaskWithoutArchetypeReturnsWksCfg020() {
    String bpmn = bpmn("<bpmn:userTask id=\"reviewTask\" name=\"Review\"/>");

    BpmnValidationResult result =
        validator.validate(
            bpmn.getBytes(StandardCharsets.UTF_8), caseType("application", List.of()));

    assertThat(result.isInvalid()).isTrue();
    ErrorDetail error =
        result.errors().stream()
            .filter(e -> "WKS-CFG-020".equals(e.code()))
            .findFirst()
            .orElseThrow();
    assertThat(error.message())
        .isEqualTo(
            "User task 'reviewTask' is missing the required 'archetype' camunda:property "
                + "(expected one of: draft_section, submit_for_processing, business_final)");
    assertThat(error.field()).isEqualTo("userTasks[reviewTask]");
  }

  @Test
  void userTaskWithUnknownArchetypeReturnsWksCfg020() {
    String bpmn =
        bpmn(
            "<bpmn:userTask id=\"reviewTask\" name=\"Review\">"
                + "<bpmn:extensionElements>"
                + "<camunda:properties>"
                + "<camunda:property name=\"archetype\" value=\"speculative\"/>"
                + "</camunda:properties>"
                + "</bpmn:extensionElements>"
                + "</bpmn:userTask>");

    BpmnValidationResult result =
        validator.validate(
            bpmn.getBytes(StandardCharsets.UTF_8), caseType("application", List.of()));

    ErrorDetail error =
        result.errors().stream()
            .filter(e -> "WKS-CFG-020".equals(e.code()))
            .findFirst()
            .orElseThrow();
    assertThat(error.message())
        .isEqualTo(
            "User task 'reviewTask' declares unknown archetype 'speculative' "
                + "(allowed: draft_section, submit_for_processing, business_final)");
  }

  // -- WKS-CFG-021 -----------------------------------------------------------

  @Test
  void businessFinalWithAsyncAfterReturnsWksCfg021() {
    String bpmn =
        bpmn(
            "<bpmn:userTask id=\"finalise\" name=\"Finalise\" camunda:asyncAfter=\"true\">"
                + "<bpmn:extensionElements>"
                + "<camunda:properties>"
                + "<camunda:property name=\"archetype\" value=\"business_final\"/>"
                + "</camunda:properties>"
                + "</bpmn:extensionElements>"
                + "</bpmn:userTask>");

    BpmnValidationResult result =
        validator.validate(
            bpmn.getBytes(StandardCharsets.UTF_8), caseType("application", List.of()));

    ErrorDetail error =
        result.errors().stream()
            .filter(e -> "WKS-CFG-021".equals(e.code()))
            .findFirst()
            .orElseThrow();
    assertThat(error.message())
        .isEqualTo(
            "User task 'finalise' archetype 'business_final' must not declare "
                + "camunda:asyncAfter=true (rule: business_final tasks are terminal and synchronous)");
    assertThat(error.field()).isEqualTo("userTasks[finalise].archetype");
  }

  @Test
  void draftSectionWithDownstreamUserTaskReturnsWksCfg021() {
    String bpmn =
        bpmn(
            "<bpmn:startEvent id=\"start\"/>"
                + "<bpmn:userTask id=\"draft\" name=\"Draft\">"
                + "<bpmn:extensionElements>"
                + "<camunda:properties>"
                + "<camunda:property name=\"archetype\" value=\"draft_section\"/>"
                + "</camunda:properties>"
                + "</bpmn:extensionElements>"
                + "</bpmn:userTask>"
                + "<bpmn:userTask id=\"review\" name=\"Review\">"
                + "<bpmn:extensionElements>"
                + "<camunda:properties>"
                + "<camunda:property name=\"archetype\" value=\"submit_for_processing\"/>"
                + "</camunda:properties>"
                + "</bpmn:extensionElements>"
                + "</bpmn:userTask>"
                + "<bpmn:endEvent id=\"end\"/>"
                + "<bpmn:sequenceFlow id=\"startToDraft\" sourceRef=\"start\" targetRef=\"draft\"/>"
                + "<bpmn:sequenceFlow id=\"draftToReview\" sourceRef=\"draft\" targetRef=\"review\"/>"
                + "<bpmn:sequenceFlow id=\"reviewToEnd\" sourceRef=\"review\" targetRef=\"end\"/>");

    BpmnValidationResult result =
        validator.validate(
            bpmn.getBytes(StandardCharsets.UTF_8), caseType("application", List.of()));

    ErrorDetail error =
        result.errors().stream()
            .filter(e -> "WKS-CFG-021".equals(e.code()))
            .findFirst()
            .orElseThrow();
    assertThat(error.message())
        .isEqualTo(
            "User task 'draft' archetype 'draft_section' must not have outgoing sequence flows "
                + "targeting another task (rule: draft_section is terminal-only)");
  }

  // -- WKS-CFG-012 -----------------------------------------------------------

  @Test
  void unknownVariableReturnsWksCfg012() {
    String bpmn =
        bpmn(
            "<bpmn:userTask id=\"draft\">"
                + "<bpmn:extensionElements>"
                + "<camunda:properties>"
                + "<camunda:property name=\"archetype\" value=\"draft_section\"/>"
                + "</camunda:properties>"
                + "</bpmn:extensionElements>"
                + "</bpmn:userTask>"
                + "<bpmn:endEvent id=\"end\"/>"
                + "<bpmn:sequenceFlow id=\"flow1\" sourceRef=\"draft\" targetRef=\"end\">"
                + "<bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">${amount &gt; 1000}</bpmn:conditionExpression>"
                + "</bpmn:sequenceFlow>");

    BpmnValidationResult result =
        validator.validate(
            bpmn.getBytes(StandardCharsets.UTF_8), caseType("application", List.of()));

    ErrorDetail error =
        result.errors().stream()
            .filter(e -> "WKS-CFG-012".equals(e.code()))
            .findFirst()
            .orElseThrow();
    assertThat(error.message())
        .isEqualTo("Variable 'amount' not found in case context for case type 'application'");
    assertThat(error.field()).startsWith("process.testProcess.expression[");
  }

  @Test
  void declaredVariableIsAccepted() {
    String bpmn =
        bpmn(
            "<bpmn:userTask id=\"draft\">"
                + "<bpmn:extensionElements>"
                + "<camunda:properties>"
                + "<camunda:property name=\"archetype\" value=\"draft_section\"/>"
                + "</camunda:properties>"
                + "</bpmn:extensionElements>"
                + "</bpmn:userTask>"
                + "<bpmn:endEvent id=\"end\"/>"
                + "<bpmn:sequenceFlow id=\"flow1\" sourceRef=\"draft\" targetRef=\"end\">"
                + "<bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">${amount &gt; 1000}</bpmn:conditionExpression>"
                + "</bpmn:sequenceFlow>");

    FieldDefinition amount =
        new FieldDefinition(
            "amount",
            "Amount",
            FieldType.NUMBER,
            true,
            1,
            List.of(),
            FieldDefinition.TypeSlots.empty());
    BpmnValidationResult result =
        validator.validate(
            bpmn.getBytes(StandardCharsets.UTF_8), caseType("application", List.of(amount)));

    assertThat(result.errors().stream().filter(e -> "WKS-CFG-012".equals(e.code())))
        .as("declared field id 'amount' must satisfy the variable-binding check")
        .isEmpty();
  }

  @Test
  void wellKnownVariableIsAccepted() {
    String bpmn =
        bpmn(
            "<bpmn:userTask id=\"draft\">"
                + "<bpmn:extensionElements>"
                + "<camunda:properties>"
                + "<camunda:property name=\"archetype\" value=\"draft_section\"/>"
                + "</camunda:properties>"
                + "</bpmn:extensionElements>"
                + "</bpmn:userTask>"
                + "<bpmn:endEvent id=\"end\"/>"
                + "<bpmn:sequenceFlow id=\"flow1\" sourceRef=\"draft\" targetRef=\"end\">"
                + "<bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">${caseStatus == 'open'}</bpmn:conditionExpression>"
                + "</bpmn:sequenceFlow>");

    BpmnValidationResult result =
        validator.validate(
            bpmn.getBytes(StandardCharsets.UTF_8), caseType("application", List.of()));

    assertThat(result.errors().stream().filter(e -> "WKS-CFG-012".equals(e.code())))
        .as("well-known variable 'caseStatus' must satisfy the variable-binding check")
        .isEmpty();
  }

  // -- collect-all -----------------------------------------------------------

  @Test
  void manyErrorsAreCollectedNotShortCircuited() {
    String bpmn =
        bpmn(
            "<bpmn:userTask id=\"first\"/>" // missing archetype → WKS-CFG-020
                + "<bpmn:userTask id=\"second\">" // unknown archetype → WKS-CFG-020
                + "<bpmn:extensionElements>"
                + "<camunda:properties>"
                + "<camunda:property name=\"archetype\" value=\"made_up\"/>"
                + "</camunda:properties>"
                + "</bpmn:extensionElements>"
                + "</bpmn:userTask>"
                + "<bpmn:userTask id=\"third\" camunda:asyncAfter=\"true\">" // contradiction →
                // WKS-CFG-021
                + "<bpmn:extensionElements>"
                + "<camunda:properties>"
                + "<camunda:property name=\"archetype\" value=\"business_final\"/>"
                + "</camunda:properties>"
                + "</bpmn:extensionElements>"
                + "</bpmn:userTask>");

    BpmnValidationResult result =
        validator.validate(
            bpmn.getBytes(StandardCharsets.UTF_8), caseType("application", List.of()));

    List<String> codes = result.errors().stream().map(ErrorDetail::code).toList();
    assertThat(codes)
        .as("all violations must surface in a single pass — never fail-on-first")
        .contains("WKS-CFG-020", "WKS-CFG-021")
        .hasSizeGreaterThanOrEqualTo(3);
  }

  // -- happy path ------------------------------------------------------------

  @Test
  void cleanBpmnReturnsOkWithProcessKey() {
    String bpmn =
        bpmn(
            "<bpmn:userTask id=\"draft\">"
                + "<bpmn:extensionElements>"
                + "<camunda:properties>"
                + "<camunda:property name=\"archetype\" value=\"submit_for_processing\"/>"
                + "</camunda:properties>"
                + "</bpmn:extensionElements>"
                + "</bpmn:userTask>");

    BpmnValidationResult result =
        validator.validate(
            bpmn.getBytes(StandardCharsets.UTF_8), caseType("application", List.of()));

    assertThat(result.isInvalid()).isFalse();
    assertThat(result.errors())
        .as("clean BPMN must produce zero error details (would catch a no-op validator regression)")
        .isEmpty();
    assertThat(result.processDefinitionKey()).contains("testProcess");
  }

  // -- helpers ---------------------------------------------------------------

  private static String bpmn(String body) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\""
        + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
        + " xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\""
        + " targetNamespace=\"http://wkspower.com/bpmn/test\">"
        + "<bpmn:process id=\"testProcess\" isExecutable=\"true\">"
        + body
        + "</bpmn:process>"
        + "</bpmn:definitions>";
  }

  private static CaseTypeConfig caseType(String id, List<FieldDefinition> fields) {
    return new CaseTypeConfig(
        id,
        "Application",
        1,
        null,
        new WorkflowRef(id + ".bpmn"),
        fields,
        List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
        List.of(),
        List.of(new RoleDefinition("admin", List.of())));
  }
}
