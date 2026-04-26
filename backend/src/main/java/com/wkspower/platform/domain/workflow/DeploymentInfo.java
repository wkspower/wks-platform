package com.wkspower.platform.domain.workflow;

import java.time.Instant;

/** Snapshot of the latest deployment for a process definition key. */
public record DeploymentInfo(
    String deploymentId, String processDefinitionId, int version, Instant deployedAt) {}
