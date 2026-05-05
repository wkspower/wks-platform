package com.wkspower.platform.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted when a stage transitions out of {@link
 * com.wkspower.platform.domain.model.StageState#ACTIVE} — i.e. becomes {@link
 * com.wkspower.platform.domain.model.StageState#COMPLETED}. Story 3.1 AC5–AC7 fire this from {@code
 * WksStageAdvancer.advance} and {@code WksStageAdvancer.skipTo}, including the last-stage
 * completion path (AC7).
 *
 * <p>Skipped intermediates do <em>not</em> emit this event in Story 3.1 (AC6) — the audit row is
 * the audit record; activity-feed handling for skips is Epic 4 work.
 *
 * <p>Publication discipline: {@code afterCommit} via the {@link
 * com.wkspower.platform.domain.port.EventPublisher#publishAfterCommit publishAfterCommit} port
 * method.
 *
 * <p>Future consumers (named, not subscribed in this story):
 *
 * <ul>
 *   <li>Epic 4 audit-feed listener — writes the stage transition into the audit table.
 *   <li>Story 3.3 stage-timeline SSE pump.
 * </ul>
 *
 * @param caseId owning case id
 * @param stageId YAML-declared stage id of the stage that just exited active
 * @param ordinal zero-based ordinal of the exited stage
 * @param source one of {@code wks-auto-rule} / {@code manual} / {@code backend-signal}
 * @param sourceRef free-form correlation string ({@code null} when not applicable)
 * @param timestamp commit timestamp from {@link com.wkspower.platform.domain.port.Clock#now}
 */
public record StageExited(
    UUID caseId, String stageId, int ordinal, String source, String sourceRef, Instant timestamp) {}
