package com.wkspower.platform.api.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Story 2.8 AC1 — wire shape for {@code GET /api/cases/{id}/tasks}. One entry per pending (active,
 * uncompleted) BPMN user task in engine create-time order.
 *
 * <p>{@code actionLabel} is the user-facing CTA copy shown on the {@code TaskLifecycleButton}. It
 * is read from the BPMN {@code userTask} {@code camunda:property name="actionLabel"} when present,
 * falling back to the {@code userTask.name} attribute. The mapper resolves it via {@code
 * WorkflowEngine.readActionLabel} so the domain {@link com.wkspower.platform.domain.model.Task}
 * stays lean (UI-string concerns belong at the API edge).
 */
public record TaskDto(
    String id,
    String processInstanceId,
    UUID caseId,
    String caseTypeId,
    String taskDefinitionKey,
    String name,
    UUID assignee,
    String archetype,
    String actionLabel,
    Instant createdAt,
    Instant dueAt) {}
