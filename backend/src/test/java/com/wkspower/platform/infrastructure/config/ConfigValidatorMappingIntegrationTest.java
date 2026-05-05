package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.engine.BpmnParser;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Story 4.2 AC10 task 4 — cross-validator integration. Proves {@link ConfigValidator} composes
 * {@link MappingValidator} after stage validation without short-circuiting: a YAML with both a
 * stage error (Story 3.1 — {@code WKS-CFG-031}) and a mapping error ({@code WKS-CFG-027}) produces
 * BOTH error details in one {@code ValidationResult}.
 */
class ConfigValidatorMappingIntegrationTest {

  private static final String BPMN =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                        targetNamespace="http://wkspower.test">
        <bpmn:process id="claim" isExecutable="true">
          <bpmn:startEvent id="start"/>
          <bpmn:userTask id="real-task"/>
          <bpmn:endEvent id="done"/>
          <bpmn:sequenceFlow id="f1" sourceRef="start" targetRef="real-task"/>
          <bpmn:sequenceFlow id="f2" sourceRef="real-task" targetRef="done"/>
        </bpmn:process>
      </bpmn:definitions>
      """;

  private final CaseTypeYamlLoader loader = new CaseTypeYamlLoader();
  private final MappingValidator mappingValidator = new MappingValidator(new BpmnParser());
  private final ConfigValidator validator = new ConfigValidator(mappingValidator);

  @Test
  void duplicateStageAndMissingUserTaskBothSurfaceInOnePass() {
    String yaml =
        """
        id: claim
        displayName: Claim
        version: 1
        workflow:
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
            permissions: [view]
        stages: [intake, intake]
        attachments:
          - type: bpmn
            file: x.bpmn
            scope: case
            map:
              userTasks:
                ghost-task: { wksTask: "Ghost" }
        """;
    var read = loader.readBytes("test", yaml.getBytes(StandardCharsets.UTF_8));
    assertThat(read.isParsed()).isTrue();

    var result =
        validator.validate(
            read.raw(), read.lines(), Map.of("x.bpmn", BPMN.getBytes(StandardCharsets.UTF_8)));

    var codes = result.errors().stream().map(ErrorDetail::code).distinct().toList();
    assertThat(codes).contains("WKS-CFG-031", "WKS-CFG-027");
  }
}
