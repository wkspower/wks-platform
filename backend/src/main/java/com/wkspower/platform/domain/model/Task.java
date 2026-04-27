package com.wkspower.platform.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain record for a single BPMN user task. Plain Java — no Spring, no JPA, no engine SDK on the
 * import list. The ArchUnit rule {@code caseDomainHasNoSpringOrJpaImports} (Story 2.3 AC9) covers
 * this; the explicit AC8 rule for Story 2.4 reasserts it.
 *
 * <p>The {@code archetype} field carries one of {@code draft_section}, {@code
 * submit_for_processing}, {@code business_final} as declared in the BPMN user task's {@code
 * camunda:properties}. Story 2.2's {@code BpmnValidator} rejects deploys with missing or
 * contradictory archetypes ({@code WKS-CFG-020} / {@code WKS-CFG-021}); Story 2.4's runtime path
 * therefore reads the property without re-validating.
 *
 * @param id engine-assigned task id (CIB seven uses string ids)
 * @param processInstanceId BPMN process instance id (correlates with engine logs); used to populate
 *     {@code TaskActionResponse.processInstanceId} on the {@code /complete} and {@code /claim}
 *     responses since the snapshot is taken before completion (Story 2.4 review).
 * @param processDefinitionId BPMN process definition id (engine-assigned). Story 2.8 surfaces it so
 *     the API mapper can read user-task BPMN extension properties (e.g. {@code actionLabel}) via
 *     {@code WorkflowEngine.readActionLabel} without an extra engine round-trip. Nullable for
 *     backward compatibility with pre-2.8 producers; populate from the engine in new paths.
 * @param caseId case the task belongs to (read from the {@code caseId} process variable set by
 *     {@code CaseService.create} in Story 2.3)
 * @param caseTypeId case type id from the case row — needed by the API layer's permission gate
 * @param taskDefinitionKey BPMN element id of the {@code bpmn:userTask}
 * @param name human-readable task name from the BPMN
 * @param assignee user id assigned to the task, or {@code null} when unassigned (claim required)
 * @param archetype one of {@code draft_section}, {@code submit_for_processing}, {@code
 *     business_final}; {@code null} only if the deploy-time gate was somehow bypassed
 * @param createdAt task creation timestamp from the engine
 * @param dueAt due date from the BPMN, or {@code null} when no due date is configured
 */
public record Task(
    String id,
    String processInstanceId,
    String processDefinitionId,
    UUID caseId,
    String caseTypeId,
    String taskDefinitionKey,
    String name,
    UUID assignee,
    String archetype,
    Instant createdAt,
    Instant dueAt) {

  public Task {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(processInstanceId, "processInstanceId");
    Objects.requireNonNull(caseId, "caseId");
    Objects.requireNonNull(caseTypeId, "caseTypeId");
    Objects.requireNonNull(taskDefinitionKey, "taskDefinitionKey");
    Objects.requireNonNull(createdAt, "createdAt");
  }
}
