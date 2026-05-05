package com.wkspower.platform.domain.config.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.model.AttachmentDefinition.PropertyEmissionRule;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.UserTaskMapping;
import com.wkspower.platform.domain.port.BackendSignalKind;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Story 4.2 AC10 — value-object surface tests for {@link MappingDefinition}. */
class MappingDefinitionTest {

  @Test
  void emptyFactoryReturnsZeroAttachmentInstance() {
    MappingDefinition empty = MappingDefinition.empty();
    assertThat(empty.attachments()).isEmpty();
  }

  @Test
  void emptyFactoryIsSingleton() {
    assertThat(MappingDefinition.empty()).isSameAs(MappingDefinition.empty());
  }

  @Test
  void equalityHoldsForIdenticalContent() {
    AttachmentDefinition a =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "case",
            Optional.empty(),
            Map.of("t1", new UserTaskMapping("Review", null)),
            Optional.empty(),
            Map.of(),
            List.of());
    AttachmentDefinition b =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "case",
            Optional.empty(),
            Map.of("t1", new UserTaskMapping("Review", null)),
            Optional.empty(),
            Map.of(),
            List.of());
    assertThat(new MappingDefinition(List.of(a))).isEqualTo(new MappingDefinition(List.of(b)));
  }

  @Test
  void backendSignalKindIsReusedForPropertyEmission() {
    PropertyEmissionRule rule =
        new PropertyEmissionRule(
            "userTask:t1", "status", BackendSignalKind.USER_TASK_PROPERTY, "stage:underwriting");
    assertThat(rule.emits()).isEqualTo(BackendSignalKind.USER_TASK_PROPERTY);
    assertThat(rule.emitScope()).isEqualTo("stage:underwriting");
  }

  @Test
  void scopeStageIdIsExposedViaOptional() {
    AttachmentDefinition stageScoped =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "stage:underwriting",
            Optional.of("underwriting"),
            Map.of(),
            Optional.empty(),
            Map.of(),
            List.of());
    assertThat(stageScoped.stageScopeId()).contains("underwriting");

    AttachmentDefinition caseScoped =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.empty(),
            Map.of(),
            List.of());
    assertThat(caseScoped.stageScopeId()).isEmpty();
  }
}
