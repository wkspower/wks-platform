package com.wkspower.platform.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.diff.MappingDiff;
import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.EndEventMapping;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.PropertyEmissionRule;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.SignalMapping;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.UserTaskMapping;
import com.wkspower.platform.domain.config.model.MappingChangeClass;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Story 4.2 AC8 / AC10 — branch coverage for the unwired {@link MappingDiff#classify} helper. Story
 * 3.8 will wire this into deploy gating; here we lock the classification semantics.
 */
class MappingDiffTest {

  @Test
  void emptyToEmptyIsAppendClass() {
    assertThat(MappingDiff.classify(MappingDefinition.empty(), MappingDefinition.empty()))
        .isEqualTo(MappingChangeClass.APPEND_CLASS);
  }

  @Test
  void appendClass_newAttachmentAdded() {
    MappingDefinition prev = MappingDefinition.empty();
    MappingDefinition next =
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    "x.bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.empty(),
                    Map.of(),
                    List.of())));
    assertThat(MappingDiff.classify(prev, next)).isEqualTo(MappingChangeClass.APPEND_CLASS);
  }

  @Test
  void appendClass_newUserTaskMappingAdded() {
    AttachmentDefinition before = baseAttachment(Map.of(), List.of());
    AttachmentDefinition after =
        baseAttachment(Map.of("t1", new UserTaskMapping("Review", null)), List.of());
    assertThat(
            MappingDiff.classify(
                new MappingDefinition(List.of(before)), new MappingDefinition(List.of(after))))
        .isEqualTo(MappingChangeClass.APPEND_CLASS);
  }

  @Test
  void appendClass_newPropertyRuleAdded() {
    PropertyEmissionRule rule =
        new PropertyEmissionRule(
            "userTask:t1", "status", ExecutionSignalKind.TASK_STATUS_CHANGED, "case");
    AttachmentDefinition before = baseAttachment(Map.of(), List.of());
    AttachmentDefinition after = baseAttachment(Map.of(), List.of(rule));
    assertThat(
            MappingDiff.classify(
                new MappingDefinition(List.of(before)), new MappingDefinition(List.of(after))))
        .isEqualTo(MappingChangeClass.APPEND_CLASS);
  }

  @Test
  void mutateClass_attachmentRemoved() {
    AttachmentDefinition a = baseAttachment(Map.of(), List.of());
    assertThat(MappingDiff.classify(new MappingDefinition(List.of(a)), MappingDefinition.empty()))
        .isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  @Test
  void mutateClass_wksTaskValueChanged() {
    AttachmentDefinition before =
        baseAttachment(Map.of("t1", new UserTaskMapping("Old", null)), List.of());
    AttachmentDefinition after =
        baseAttachment(Map.of("t1", new UserTaskMapping("New", null)), List.of());
    assertThat(
            MappingDiff.classify(
                new MappingDefinition(List.of(before)), new MappingDefinition(List.of(after))))
        .isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  @Test
  void mutateClass_stageTransitionPayloadChanged() {
    AttachmentDefinition before =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.of(new EndEventMapping("a -> b")),
            Map.of(),
            List.of());
    AttachmentDefinition after =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.of(new EndEventMapping("a -> c")),
            Map.of(),
            List.of());
    assertThat(
            MappingDiff.classify(
                new MappingDefinition(List.of(before)), new MappingDefinition(List.of(after))))
        .isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  @Test
  void mutateClass_signalMappingChanged() {
    AttachmentDefinition before =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.empty(),
            Map.of("escalated", new SignalMapping("a -> b")),
            List.of());
    AttachmentDefinition after =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.empty(),
            Map.of("escalated", new SignalMapping("a -> c")),
            List.of());
    assertThat(
            MappingDiff.classify(
                new MappingDefinition(List.of(before)), new MappingDefinition(List.of(after))))
        .isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  @Test
  void mutateClass_fileReferenceChanged() {
    AttachmentDefinition before =
        new AttachmentDefinition(
            "bpmn",
            "old.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.empty(),
            Map.of(),
            List.of());
    AttachmentDefinition after =
        new AttachmentDefinition(
            "bpmn",
            "new.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.empty(),
            Map.of(),
            List.of());
    assertThat(
            MappingDiff.classify(
                new MappingDefinition(List.of(before)), new MappingDefinition(List.of(after))))
        .isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  @Test
  void mutateClass_scopeChangedRegistersAsRemoveAndAdd() {
    AttachmentDefinition before =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.empty(),
            Map.of(),
            List.of());
    AttachmentDefinition after =
        new AttachmentDefinition(
            "bpmn",
            "x.bpmn",
            "stage:intake",
            Optional.of("intake"),
            Map.of(),
            Optional.empty(),
            Map.of(),
            List.of());
    // Scope is part of the identity key — so the diff sees a removal + an addition. Removal is
    // mutate-class, by definition.
    assertThat(
            MappingDiff.classify(
                new MappingDefinition(List.of(before)), new MappingDefinition(List.of(after))))
        .isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  @Test
  void mutateClass_userTaskMappingRemoved() {
    AttachmentDefinition before =
        baseAttachment(Map.of("t1", new UserTaskMapping("Review", null)), List.of());
    AttachmentDefinition after = baseAttachment(Map.of(), List.of());
    assertThat(
            MappingDiff.classify(
                new MappingDefinition(List.of(before)), new MappingDefinition(List.of(after))))
        .isEqualTo(MappingChangeClass.MUTATE_CLASS);
  }

  private AttachmentDefinition baseAttachment(
      Map<String, UserTaskMapping> userTasks, List<PropertyEmissionRule> properties) {
    return new AttachmentDefinition(
        "bpmn",
        "x.bpmn",
        "case",
        Optional.empty(),
        userTasks,
        Optional.empty(),
        Map.of(),
        properties);
  }
}
