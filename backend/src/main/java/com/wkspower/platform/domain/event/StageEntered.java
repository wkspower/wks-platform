package com.wkspower.platform.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted when a stage transitions to {@link com.wkspower.platform.domain.model.StageState#ACTIVE}.
 * Story 3.1 AC4–AC6 fire this from two triggers:
 *
 * <ul>
 *   <li>{@code WksStageAdvancer.bootstrap} — on case creation, for stage 0 only (Sprint 1 triage Q3
 *       — stage-0-only events; future {@code StagePending} events are deferred to Epic 9 if a
 *       consumer needs them).
 *   <li>{@code WksStageAdvancer.advance} / {@code skipTo} — on the new active stage.
 * </ul>
 *
 * <p>Publication discipline: {@code afterCommit} via the {@link
 * com.wkspower.platform.domain.port.EventPublisher#publishAfterCommit publishAfterCommit} port
 * method. Subscribers must never observe an event whose underlying transaction was rolled back.
 * Mirrors Story 2.4's {@code CaseStatusListener} discipline.
 *
 * <p>Future consumers (named, not subscribed in this story):
 *
 * <ul>
 *   <li>Epic 4 audit-feed listener — writes the stage transition into the audit table.
 *   <li>Story 3.3 stage-timeline SSE pump — pushes timeline updates to the case-detail surface.
 * </ul>
 *
 * @param caseId owning case id
 * @param stageId YAML-declared stage id (e.g. {@code "intake"})
 * @param ordinal zero-based ordinal of the stage that just went active
 * @param source one of {@code wks-auto-rule} / {@code manual} / {@code backend-signal}
 * @param sourceRef free-form correlation string ({@code null} when not applicable)
 * @param timestamp commit timestamp from {@link com.wkspower.platform.domain.port.Clock#now}
 */
public record StageEntered(
    UUID caseId, String stageId, int ordinal, String source, String sourceRef, Instant timestamp) {}
