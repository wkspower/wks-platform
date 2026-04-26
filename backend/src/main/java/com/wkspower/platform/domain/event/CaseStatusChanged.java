package com.wkspower.platform.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted when a BPMN execution crosses a status boundary — i.e. when the engine commits the
 * transition out of a user task or end event whose id (or {@code camunda:property name="status"})
 * maps to a YAML-declared status id. Story 2.4's {@code CaseStatusListener} fires it on the
 * engine's transaction-commit pathway so subscribers see the new status atomically with the engine
 * state and the {@code cases.status} update.
 *
 * <p>Schema follows {@code architecture.md §Decision 11} — entity ids, transition metadata,
 * timestamp. Activity-feed / audit listeners attach in Story 4.1; SSE transport in Story 4.3.
 *
 * @param caseId case whose status changed
 * @param oldStatus previous status id (may be {@code null} on the very first transition out of the
 *     start event if no prior user-task fired)
 * @param newStatus new status id read from the next active activity (or the end event's status
 *     property)
 * @param processInstanceId BPMN process instance id — useful for correlating with engine logs
 * @param timestamp commit timestamp from the engine (taken via {@code Clock.now()} inside the
 *     listener)
 */
public record CaseStatusChanged(
    UUID caseId, String oldStatus, String newStatus, String processInstanceId, Instant timestamp) {}
