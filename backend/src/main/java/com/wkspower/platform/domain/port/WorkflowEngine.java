package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.workflow.DeploymentInfo;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.util.Optional;

/**
 * Outbound port for the embedded BPMN engine. Story 2.2 owns the deploy + lookup surface; process
 * instance lifecycle (start, complete, transition) is intentionally absent and lands with Stories
 * 2.3 / 2.4 / 2.8. Implementations live in {@code engine/} only.
 */
public interface WorkflowEngine {

  /**
   * Deploy a single BPMN definition. Returns the engine-assigned deployment id. Implementations
   * MUST be idempotent on identical content (deploying the same bytes twice returns the original
   * deployment id rather than producing a new version).
   */
  DeploymentResult deploy(DeploymentRequest request);

  /**
   * Snapshot of the latest deployment for a process definition key, or {@link Optional#empty()} if
   * the key has never been deployed.
   */
  Optional<DeploymentInfo> latestDeployment(String processDefinitionKey);
}
