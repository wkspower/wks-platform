package com.wkspower.platform.domain.config.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.model.AttachmentDefinition.OutcomeMapping;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.PropertyEmissionRule;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.UserTaskMapping;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
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
            List.of(),
            Map.of());
    AttachmentDefinition b =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "case",
            Optional.empty(),
            Map.of("t1", new UserTaskMapping("Review", null)),
            Optional.empty(),
            Map.of(),
            List.of(),
            Map.of());
    assertThat(new MappingDefinition(List.of(a))).isEqualTo(new MappingDefinition(List.of(b)));
  }

  @Test
  void backendSignalKindIsReusedForPropertyEmission() {
    PropertyEmissionRule rule =
        new PropertyEmissionRule(
            "userTask:t1", "status", ExecutionSignalKind.TASK_STATUS_CHANGED, "stage:underwriting");
    assertThat(rule.emits()).isEqualTo(ExecutionSignalKind.TASK_STATUS_CHANGED);
    assertThat(rule.emitScope()).isEqualTo("stage:underwriting");
  }

  /** Story 6.2 — outcomeMappings is included in the canonical hash, sorted by key. */
  @Test
  void hashIncludesOutcomeMappingsInSortedOrder() {
    // Build two definitions with the same outcome entries in DIFFERENT insertion order.
    AttachmentDefinition withOutcomes1 =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.empty(),
            Map.of(),
            List.of(),
            Map.of(
                "approve", new OutcomeMapping("intake -> decision"),
                "reject", new OutcomeMapping("intake -> closed")));
    AttachmentDefinition withOutcomes2 =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.empty(),
            Map.of(),
            List.of(),
            Map.of(
                "reject", new OutcomeMapping("intake -> closed"),
                "approve", new OutcomeMapping("intake -> decision")));
    String hash1 = new MappingDefinition(List.of(withOutcomes1)).computeHash();
    String hash2 = new MappingDefinition(List.of(withOutcomes2)).computeHash();
    assertThat(hash1).isEqualTo(hash2);
  }

  /** Story 6.2 — hash differs when outcomeMappings differ. */
  @Test
  void hashDiffersWhenOutcomeMappingsDiffer() {
    AttachmentDefinition withOutcomes =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.empty(),
            Map.of(),
            List.of(),
            Map.of("approve", new OutcomeMapping("intake -> decision")));
    AttachmentDefinition withoutOutcomes =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.empty(),
            Map.of(),
            List.of(),
            Map.of());
    String h1 = new MappingDefinition(List.of(withOutcomes)).computeHash();
    String h2 = new MappingDefinition(List.of(withoutOutcomes)).computeHash();
    assertThat(h1).isNotEqualTo(h2);
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
            List.of(),
            Map.of());
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
            List.of(),
            Map.of());
    assertThat(caseScoped.stageScopeId()).isEmpty();
  }
}
