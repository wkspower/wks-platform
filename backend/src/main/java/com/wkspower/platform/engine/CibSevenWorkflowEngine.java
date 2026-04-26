package com.wkspower.platform.engine;

import com.wkspower.platform.domain.exception.WksWorkflowEngineException;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.workflow.DeploymentInfo;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.cibseven.bpm.engine.RepositoryService;
import org.cibseven.bpm.engine.repository.Deployment;
import org.cibseven.bpm.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CIB seven adapter for the {@link WorkflowEngine} port. Sole class outside the test tree allowed
 * to import {@code org.cibseven.*} (enforced by {@code
 * ArchitectureTest.onlyEngineImportsCibSeven}).
 *
 * <p>{@code enableDuplicateFiltering(true)} makes redeploy of an unchanged BPMN a no-op — CIB seven
 * hashes the resource bytes. This is the engine-side counterpart to the case-type registry's
 * idempotent same-version path.
 */
@Component
public class CibSevenWorkflowEngine implements WorkflowEngine {

  private static final Logger log = LoggerFactory.getLogger(CibSevenWorkflowEngine.class);

  /** Soft SLA — Story 2.2 AC3 sets the test ceiling at 3000 ms; 2000 ms warns early. */
  private static final long DEPLOY_WARN_THRESHOLD_MS = 2000L;

  private final RepositoryService repositoryService;

  public CibSevenWorkflowEngine(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }

  @Override
  public DeploymentResult deploy(DeploymentRequest request) {
    Instant start = Instant.now();
    String resourceName = request.processDefinitionKey() + ".bpmn";
    Deployment deployment;
    try {
      deployment =
          repositoryService
              .createDeployment()
              .name(request.name())
              .addInputStream(resourceName, new ByteArrayInputStream(request.bpmnXml()))
              .enableDuplicateFiltering(true)
              .deploy();
    } catch (RuntimeException ex) {
      throw new WksWorkflowEngineException(
          "CIB seven deploy failed for processDefinitionKey="
              + request.processDefinitionKey()
              + " (caseType="
              + request.caseTypeId()
              + " v"
              + request.caseTypeVersion()
              + ")",
          ex);
    }

    ProcessDefinition definition;
    try {
      definition =
          repositoryService
              .createProcessDefinitionQuery()
              .deploymentId(deployment.getId())
              .processDefinitionKey(request.processDefinitionKey())
              .singleResult();
    } catch (RuntimeException ex) {
      throw new WksWorkflowEngineException(
          "CIB seven post-deploy lookup failed for deploymentId=" + deployment.getId(), ex);
    }
    if (definition == null) {
      throw new WksWorkflowEngineException(
          "CIB seven returned no ProcessDefinition for deploymentId="
              + deployment.getId()
              + " key="
              + request.processDefinitionKey());
    }

    Duration elapsed = Duration.between(start, Instant.now());
    if (elapsed.toMillis() > DEPLOY_WARN_THRESHOLD_MS) {
      log.warn(
          "WKS engine deploy slow: key={} elapsedMs={} (threshold {} ms — Story 2.2 AC3)",
          request.processDefinitionKey(),
          elapsed.toMillis(),
          DEPLOY_WARN_THRESHOLD_MS);
    }

    if (deployment.getDeploymentTime() == null) {
      throw new WksWorkflowEngineException(
          "CIB seven deployment row has null deploymentTime for deploymentId="
              + deployment.getId()
              + " key="
              + request.processDefinitionKey());
    }
    return new DeploymentResult(
        deployment.getId(),
        definition.getKey(),
        definition.getId(),
        definition.getVersion(),
        deployment.getDeploymentTime().toInstant());
  }

  @Override
  public Optional<DeploymentInfo> latestDeployment(String processDefinitionKey) {
    ProcessDefinition definition;
    try {
      definition =
          repositoryService
              .createProcessDefinitionQuery()
              .processDefinitionKey(processDefinitionKey)
              .latestVersion()
              .singleResult();
    } catch (RuntimeException ex) {
      throw new WksWorkflowEngineException(
          "CIB seven query failed for processDefinitionKey=" + processDefinitionKey, ex);
    }
    if (definition == null) {
      return Optional.empty();
    }

    Deployment deployment;
    try {
      deployment =
          repositoryService
              .createDeploymentQuery()
              .deploymentId(definition.getDeploymentId())
              .singleResult();
    } catch (RuntimeException ex) {
      throw new WksWorkflowEngineException(
          "CIB seven deployment lookup failed for deploymentId=" + definition.getDeploymentId(),
          ex);
    }

    if (deployment == null || deployment.getDeploymentTime() == null) {
      // CIB seven returned a process-definition row but no matching deployment row (or one with
      // no deployment time). Treat as "not yet observable" rather than synthesise a sentinel.
      return Optional.empty();
    }
    return Optional.of(
        new DeploymentInfo(
            definition.getDeploymentId(),
            definition.getId(),
            definition.getVersion(),
            deployment.getDeploymentTime().toInstant()));
  }
}
