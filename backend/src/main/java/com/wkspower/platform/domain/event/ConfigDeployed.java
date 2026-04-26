package com.wkspower.platform.domain.event;

import java.time.Instant;

/**
 * Emitted after a successful case-type + BPMN deploy. The {@code processDefinitionId} is whatever
 * the workflow engine assigned to the deployed BPMN — opaque to domain code. Story 2.2 publishes;
 * Story 4.3 transports via SSE. Audit and activity-feed listeners attach in Epic 4.
 *
 * <p>Schema follows {@code architecture.md §Decision 11} — timestamp, actor, entity ids. {@code
 * actorEmail} is {@code null} for startup-loader emissions (no authenticated principal).
 */
public record ConfigDeployed(
    String caseTypeId,
    int version,
    String deploymentId,
    String processDefinitionId,
    String actorEmail,
    Instant timestamp) {}
