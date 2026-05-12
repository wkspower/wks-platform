package com.wkspower.platform.api.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.EndEventMapping;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.PropertyEmissionRule;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.SignalMapping;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.UserTaskMapping;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Story 4.6 AC1 — projection unit tests for {@link MappingInspectorDto#from}. */
class MappingInspectorDtoProjectionTest {

  @Test
  void fullMappingProjectionEmitsRowPerElement() {
    AttachmentDefinition a =
        new AttachmentDefinition(
            "bpmn",
            "auto-loan.bpmn",
            "case",
            Optional.empty(),
            Map.of("userTask_review", new UserTaskMapping("review-task", "form-1")),
            Optional.of(new EndEventMapping("draft -> underwriting")),
            Map.of("escalate", new SignalMapping("review -> escalated")),
            List.of(
                new PropertyEmissionRule(
                    "userTask:complete",
                    "wksTaskCompleted",
                    ExecutionSignalKind.TASK_COMPLETED,
                    "case")),
            Map.of());
    MappingDefinition mapping = new MappingDefinition(List.of(a));

    MappingInspectorDto dto = MappingInspectorDto.from("auto-loan", "1.4", mapping);

    assertThat(dto.caseTypeId()).isEqualTo("auto-loan");
    assertThat(dto.version()).isEqualTo("1.4");
    assertThat(dto.emptyMapping()).isFalse();
    assertThat(dto.attachments()).hasSize(1);
    MappingInspectorDto.AttachmentView v = dto.attachments().get(0);
    assertThat(v.bpmnSource()).isEqualTo("auto-loan.bpmn");
    // 4 rows: endEvent + signal + userTask + propertyEmission
    assertThat(v.elements()).hasSize(4);
    assertThat(v.elements())
        .extracting(MappingInspectorDto.ElementMappingRow::bpmnElement)
        .containsExactlyInAnyOrder(
            "endEvent", "signal:escalate", "userTask:userTask_review", "userTask:complete");
  }

  @Test
  void emptyMappingProjectsEmptyTrueAndZeroAttachments() {
    MappingInspectorDto dto = MappingInspectorDto.from("simple", "1", MappingDefinition.empty());
    assertThat(dto.emptyMapping()).isTrue();
    assertThat(dto.attachments()).isEmpty();
  }

  @Test
  void perAttachmentIterationOrderPreserved() {
    AttachmentDefinition a1 =
        new AttachmentDefinition(
            "bpmn",
            "first.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.of(new EndEventMapping("a -> b")),
            Map.of(),
            List.of(),
            Map.of());
    AttachmentDefinition a2 =
        new AttachmentDefinition(
            "bpmn",
            "second.bpmn",
            "case",
            Optional.empty(),
            Map.of(),
            Optional.of(new EndEventMapping("c -> d")),
            Map.of(),
            List.of(),
            Map.of());
    MappingDefinition mapping = new MappingDefinition(List.of(a1, a2));

    MappingInspectorDto dto = MappingInspectorDto.from("multi", "2", mapping);
    assertThat(dto.attachments()).hasSize(2);
    assertThat(dto.attachments().get(0).bpmnSource()).isEqualTo("first.bpmn");
    assertThat(dto.attachments().get(1).bpmnSource()).isEqualTo("second.bpmn");
  }
}
