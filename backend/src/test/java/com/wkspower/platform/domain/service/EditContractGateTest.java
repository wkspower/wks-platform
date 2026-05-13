package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.UserTaskMapping;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.FormDefinition;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.model.Task;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Story 6.3 AC-2 / AC-3 — pure unit coverage for {@link EditContractGate}. Covers the four
 * reachable shapes:
 *
 * <ol>
 *   <li>Field owned by an open task's form -> BLOCKED with the matching openTaskId / formId.
 *   <li>No open tasks on the case -> ALLOWED (AC-3 happy path).
 *   <li>Open tasks exist but none reach the changed field -> ALLOWED (form does not own field).
 *   <li>Open task references a userTaskMapping whose form id is unknown -> ALLOWED (defensive
 *       skip; the deploy-time validator catches form-id typos but we never NPE at runtime).
 * </ol>
 */
class EditContractGateTest {

  @Test
  void blocks_when_open_task_form_owns_changed_field() {
    CaseTypeConfig caseType = caseType(form("intake-form", "applicant"));
    MappingDefinition mapping = mapping(attachment("intake-task", "intake-form"));
    Task openTask = task("task-1", "intake-task");

    List<EditContractGate.BlockReason> blocked =
        EditContractGate.blockedFields(caseType, mapping, List.of(openTask), Set.of("applicant"));

    assertThat(blocked).hasSize(1);
    assertThat(blocked.get(0).fieldId()).isEqualTo("applicant");
    assertThat(blocked.get(0).openTaskId()).isEqualTo("task-1");
    assertThat(blocked.get(0).formId()).isEqualTo("intake-form");
  }

  @Test
  void allows_when_no_open_tasks_on_case() {
    CaseTypeConfig caseType = caseType(form("intake-form", "applicant"));
    MappingDefinition mapping = mapping(attachment("intake-task", "intake-form"));

    List<EditContractGate.BlockReason> blocked =
        EditContractGate.blockedFields(caseType, mapping, List.of(), Set.of("applicant"));

    assertThat(blocked).isEmpty();
  }

  @Test
  void allows_when_open_task_form_does_not_own_field() {
    CaseTypeConfig caseType = caseType(form("intake-form", "applicant"));
    MappingDefinition mapping = mapping(attachment("intake-task", "intake-form"));
    Task openTask = task("task-1", "intake-task");

    List<EditContractGate.BlockReason> blocked =
        EditContractGate.blockedFields(
            caseType, mapping, List.of(openTask), Set.of("loan-amount"));

    assertThat(blocked).isEmpty();
  }

  @Test
  void allows_when_userTaskMapping_references_unknown_form_id() {
    CaseTypeConfig caseType = caseType(form("intake-form", "applicant"));
    MappingDefinition mapping = mapping(attachment("intake-task", "missing-form"));
    Task openTask = task("task-1", "intake-task");

    List<EditContractGate.BlockReason> blocked =
        EditContractGate.blockedFields(caseType, mapping, List.of(openTask), Set.of("applicant"));

    assertThat(blocked).isEmpty();
  }

  // ---- helpers ----

  private static CaseTypeConfig caseType(FormDefinition... forms) {
    return new CaseTypeConfig(
        "loan",
        "Loan",
        1,
        "test",
        new WorkflowRef("loan.bpmn"),
        List.of(
            new FieldDefinition("applicant", "Applicant", FieldType.TEXT, true, 0, List.of(), null),
            new FieldDefinition(
                "loan-amount", "Loan Amount", FieldType.NUMBER, false, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("applicant"),
        List.of(new RoleDefinition("admin", List.of(Permission.VIEW))),
        List.of(),
        List.of(forms));
  }

  private static FormDefinition form(String id, String fieldId) {
    return new FormDefinition(
        id,
        "single",
        "monolithic",
        "single-page",
        List.of(
            new FieldDefinition(fieldId, fieldId, FieldType.TEXT, false, 0, List.of(), null)),
        List.of(),
        "submit_for_processing");
  }

  private static AttachmentDefinition attachment(String bpmnTaskId, String formId) {
    return new AttachmentDefinition(
        "bpmn",
        "loan.bpmn",
        "case",
        Optional.empty(),
        Map.of(bpmnTaskId, new UserTaskMapping(bpmnTaskId, formId)),
        Optional.empty(),
        Map.of(),
        List.of(),
        Map.of());
  }

  private static MappingDefinition mapping(AttachmentDefinition... attachments) {
    return new MappingDefinition(List.of(attachments));
  }

  private static Task task(String id, String taskDefinitionKey) {
    return new Task(
        id,
        "pi-1",
        "pd-1",
        UUID.randomUUID(),
        "loan",
        taskDefinitionKey,
        "Intake",
        null,
        "submit_for_processing",
        Instant.parse("2026-05-13T10:00:00Z"),
        null);
  }
}
