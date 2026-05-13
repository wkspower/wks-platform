package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.UserTaskMapping;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FormDefinition;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Story 6.3 AC-2 / AC-3 — pure helper that resolves "is this field owned by an open task's form?"
 * for the direct-edit precedence contract.
 *
 * <p>Given: the pinned {@link CaseTypeConfig}, its {@link MappingDefinition} (attachments), the
 * list of currently open {@link Task}s on the case, and the set of field ids the caller is
 * attempting to mutate.
 *
 * <p>Returns: per-field block reasons. A field is BLOCKED iff there exists an open task whose BPMN
 * {@code taskDefinitionKey} is mapped (via {@link AttachmentDefinition#userTaskMappings}) to a
 * {@link UserTaskMapping#form} whose {@link FormDefinition#fields} include that field.
 *
 * <p>Pure Java — no Spring, no port. Trivially unit-testable; consumed by {@code CaseService}.
 *
 * <p>Design choice (Open Question Q2): the gate is a stateless helper rather than a new {@code
 * EditContractService} bean. The gating logic is small (under 100 LoC), has no collaborators, and
 * its single caller is {@link CaseService#update}. A separate Spring bean would add lifecycle
 * surface without test-clarity gain.
 */
public final class EditContractGate {

  private EditContractGate() {}

  /**
   * Compute block reasons for the changed-field set. Returns one {@link BlockReason} per field that
   * is owned by at least one open task's form. A field with multiple matches returns the first
   * match (deterministic by attachment iteration order).
   *
   * <p>Backwards-compatible overload: no form exemption. Direct-edit path (PUT /api/cases/{id})
   * calls this entry point unchanged.
   */
  public static List<BlockReason> blockedFields(
      CaseTypeConfig caseType,
      MappingDefinition mapping,
      List<Task> openTasks,
      Set<String> changedFieldIds) {
    return blockedFields(caseType, mapping, openTasks, changedFieldIds, null);
  }

  /**
   * Story 6-3b AC1 — form-submit gate exemption. When {@code exemptFormId} is non-null, any
   * userTaskMapping whose {@code form} equals {@code exemptFormId} is skipped during the
   * ownership scan: fields owned by that form are NOT blocked. Sibling-form isolation: fields
   * owned by a *different* open task's form remain blocked — a form can only exempt its own
   * ownership, never its siblings'.
   *
   * <p>Carve choice = option (c) per story spec. Rationale: the gate is the single source of
   * truth for "what an open task owns"; threading the exempt through here keeps the rule
   * pure and unit-testable in isolation. Threading it through {@code CaseService.update}
   * (option b) would have required a public/private overload split with the same semantics
   * but no test surface gain.
   */
  public static List<BlockReason> blockedFields(
      CaseTypeConfig caseType,
      MappingDefinition mapping,
      List<Task> openTasks,
      Set<String> changedFieldIds,
      String exemptFormId) {
    Objects.requireNonNull(caseType, "caseType");
    Objects.requireNonNull(openTasks, "openTasks");
    Objects.requireNonNull(changedFieldIds, "changedFieldIds");

    if (changedFieldIds.isEmpty() || openTasks.isEmpty() || mapping == null) {
      return List.of();
    }

    List<BlockReason> blocked = new ArrayList<>();
    for (String fieldId : changedFieldIds) {
      findFirstMatch(caseType, mapping, openTasks, fieldId, exemptFormId).ifPresent(blocked::add);
    }
    return blocked;
  }

  private static Optional<BlockReason> findFirstMatch(
      CaseTypeConfig caseType,
      MappingDefinition mapping,
      List<Task> openTasks,
      String fieldId,
      String exemptFormId) {
    for (Task openTask : openTasks) {
      for (AttachmentDefinition attachment : mapping.attachments()) {
        UserTaskMapping userTaskMapping =
            attachment.userTaskMappings().get(openTask.taskDefinitionKey());
        if (userTaskMapping == null) {
          continue;
        }
        String formId = userTaskMapping.form();
        if (formId == null || formId.isBlank()) {
          continue;
        }
        if (exemptFormId != null && exemptFormId.equals(formId)) {
          // Story 6-3b — form-submit path is exempt from its own ownership. Sibling forms
          // (different formId on a different open task) still gate.
          continue;
        }
        Optional<FormDefinition> form =
            caseType.forms().stream().filter(f -> f.id().equals(formId)).findFirst();
        if (form.isEmpty()) {
          continue;
        }
        boolean ownsField =
            form.get().fields().stream().map(FieldDefinition::id).anyMatch(fieldId::equals);
        if (ownsField) {
          return Optional.of(new BlockReason(fieldId, openTask.id(), formId));
        }
      }
    }
    return Optional.empty();
  }

  /** Why a field is blocked from direct-edit: the open task and its form that own the field. */
  public record BlockReason(String fieldId, String openTaskId, String formId) {
    public BlockReason {
      Objects.requireNonNull(fieldId, "fieldId");
      Objects.requireNonNull(openTaskId, "openTaskId");
      Objects.requireNonNull(formId, "formId");
    }
  }
}
