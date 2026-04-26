package com.wkspower.platform.domain.workflow;

import java.time.Instant;

/** Outcome of a successful deploy. Engine-assigned ids surface here for the API + audit. */
public record DeploymentResult(
    String deploymentId,
    String processDefinitionKey,
    String processDefinitionId,
    int version,
    Instant deployedAt) {}
