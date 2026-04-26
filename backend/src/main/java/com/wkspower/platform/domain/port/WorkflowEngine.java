package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.workflow.DeploymentInfo;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.util.Map;
import java.util.Optional;

/**
 * Outbound port for the embedded BPMN engine.
 *
 * <ul>
 *   <li>Story 2.2 surface — {@link #deploy(DeploymentRequest)} and {@link
 *       #latestDeployment(String)}.
 *   <li>Story 2.3 surface — {@link #startProcessInstance(String, Map)}.
 *   <li>Process completion / transition lifecycle remains absent until Stories 2.4 / 2.8.
 * </ul>
 *
 * <p>Implementations live in {@code engine/} only.
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

  /**
   * Start a process instance for the given deployed process definition. {@code variables} are
   * passed to the engine as initial process variables; pass simple scalars only ({@code String},
   * {@code UUID.toString()}, {@code Long}). Returns the engine-assigned process instance id.
   *
   * <p>Implementations MUST translate engine-side failures (unknown key, JUEL evaluation errors)
   * into {@link com.wkspower.platform.domain.exception.WksWorkflowEngineException}.
   */
  String startProcessInstance(String processDefinitionKey, Map<String, Object> variables);
}
