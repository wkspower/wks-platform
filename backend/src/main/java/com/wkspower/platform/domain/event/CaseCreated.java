package com.wkspower.platform.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted after a {@code Case} is persisted by {@code CaseService.create}. Story 2.3 publishes via
 * the {@code EventPublisher} port; activity-feed and audit listeners attach in Story 4.1; SSE
 * transport in Story 4.3.
 *
 * <p>Schema follows {@code architecture.md §Decision 11} — entity ids, actor, timestamp.
 */
public record CaseCreated(
    UUID caseId, String caseTypeId, int caseTypeVersion, UUID actorId, Instant timestamp) {}
