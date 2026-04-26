package com.wkspower.platform.api.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload for {@code POST /api/tasks/{id}/complete} and {@code POST /api/tasks/{id}/claim}
 * (Story 2.4 AC2 / AC3 / AC4). {@code archetype} is read from the user task's {@code
 * camunda:property name="archetype"} so Story 2.8's {@code TaskLifecycleButton} can render the
 * right Layer-3 UI state without a follow-up round trip (architecture.md §Decision 9).
 *
 * @param taskId engine-assigned task id
 * @param processInstanceId BPMN process instance id (correlates with engine logs)
 * @param caseId case the task belongs to
 * @param archetype one of {@code draft_section}, {@code submit_for_processing}, {@code
 *     business_final}; may be {@code null} on the claim path if the task definition is malformed
 *     (deploy-time gate prevents this in practice)
 * @param assignee post-action assignee — populated on the {@code /claim} response, may be {@code
 *     null} on the {@code /complete} response (the engine has already removed the task)
 * @param at action timestamp (claim or complete)
 */
public record TaskActionResponse(
    String taskId,
    String processInstanceId,
    UUID caseId,
    String archetype,
    UUID assignee,
    Instant at) {}
