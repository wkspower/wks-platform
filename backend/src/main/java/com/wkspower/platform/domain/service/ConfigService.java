package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.DeployResult;
import com.wkspower.platform.domain.config.RegistrationResult;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.event.ConfigDeployed;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.port.BpmnValidationService;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRegistrar;
import com.wkspower.platform.domain.port.CaseTypeSource;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.workflow.BpmnValidationResult;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates load + validate + register for case-type YAML and the joint YAML+BPMN deploy. Pure
 * Java — no Spring, no Jackson, no SnakeYAML, no engine SDK. The infrastructure adapters wire the
 * ports into the Spring context.
 *
 * <p>First real callers: {@code CaseTypeStartupLoader} (Story 2.1) and the admin deploy endpoint
 * (Story 2.2). The startup loader continues to use {@link #validateAndRegister(Path)}; the HTTP
 * layer drives {@link #deploy(byte[], byte[], String)}.
 */
public class ConfigService {

  private final CaseTypeSource source;
  private final CaseTypeRegistrar registrar;
  private final CaseTypeReader reader;
  private final BpmnValidationService bpmnValidator;
  private final WorkflowEngine workflowEngine;
  private final EventPublisher eventPublisher;

  public ConfigService(
      CaseTypeSource source,
      CaseTypeRegistrar registrar,
      CaseTypeReader reader,
      BpmnValidationService bpmnValidator,
      WorkflowEngine workflowEngine,
      EventPublisher eventPublisher) {
    this.source = source;
    this.registrar = registrar;
    this.reader = reader;
    this.bpmnValidator = bpmnValidator;
    this.workflowEngine = workflowEngine;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Load {@code file}, validate it, and on success register with the registry. Returns the {@link
   * ValidationResult} so callers can react to errors.
   */
  public ValidationResult validateAndRegister(Path file) {
    ValidationResult result = source.load(file);
    if (result.isInvalid() || result.config().isEmpty()) {
      return result;
    }
    RegistrationResult reg = registrar.register(result.config().get());
    if (reg.outcome() == RegistrationResult.Outcome.REJECTED_OLDER_VERSION) {
      return ValidationResult.invalid(reg.errors());
    }
    return result;
  }

  /** Byte-driven YAML-only variant. Retained for the startup loader's BPMN-missing path. */
  public ValidationResult validateAndRegister(String sourceName, byte[] bytes) {
    ValidationResult result = this.source.loadBytes(sourceName, bytes);
    if (result.isInvalid() || result.config().isEmpty()) {
      return result;
    }
    RegistrationResult reg = registrar.register(result.config().get());
    if (reg.outcome() == RegistrationResult.Outcome.REJECTED_OLDER_VERSION) {
      return ValidationResult.invalid(reg.errors());
    }
    return result;
  }

  /**
   * Joint YAML + BPMN deploy used by the admin endpoint and the startup loader's BPMN-present path.
   * Always runs both validators (collect-all) before touching the registry; engine deploy happens
   * after a successful registry write, with rollback on engine failure so the operator never sees a
   * registry advertising a config whose BPMN never deployed.
   *
   * @param actorEmail authenticated principal driving the deploy, or {@code null} for startup
   *     loader emissions
   */
  public DeployResult deploy(byte[] yamlBytes, byte[] bpmnBytes, String actorEmail) {
    ValidationResult yamlResult = source.loadBytes("api-deploy.yaml", yamlBytes);
    CaseTypeConfig caseType = yamlResult.config().orElse(null);

    BpmnValidationResult bpmnResult = bpmnValidator.validate(bpmnBytes, caseType);

    List<ErrorDetail> aggregate = new ArrayList<>(yamlResult.errors());
    aggregate.addAll(bpmnResult.errors());
    if (!aggregate.isEmpty() || caseType == null) {
      // Either side reported errors, OR YAML failed catastrophically (no caseType produced).
      if (aggregate.isEmpty()) {
        // Defensive: shouldn't happen because invalid yamlResult always carries errors, but
        // guarantee the result invariant either way.
        aggregate.add(ErrorDetail.of("WKS-CFG-099", "Case type configuration could not be parsed"));
      }
      return DeployResult.invalid(aggregate);
    }

    Optional<CaseTypeConfig> priorState = reader.find(caseType.id());

    RegistrationResult reg = registrar.register(caseType);
    if (reg.outcome() == RegistrationResult.Outcome.REJECTED_OLDER_VERSION) {
      return DeployResult.invalid(reg.errors());
    }

    DeploymentResult deployment;
    try {
      deployment =
          workflowEngine.deploy(
              new DeploymentRequest(
                  caseType.id() + " v" + caseType.version(),
                  bpmnResult.processDefinitionKey().orElseThrow(),
                  bpmnBytes,
                  caseType.id(),
                  caseType.version()));
    } catch (RuntimeException ex) {
      restoreRegistryAfterEngineFailure(caseType.id(), priorState);
      throw ex;
    }

    eventPublisher.publish(
        new ConfigDeployed(
            caseType.id(),
            caseType.version(),
            deployment.deploymentId(),
            deployment.processDefinitionId(),
            actorEmail,
            deployment.deployedAt()));

    return DeployResult.ok(caseType, deployment);
  }

  /**
   * Deploy ONLY the BPMN side for an already-registered case type. Used by the startup loader,
   * where the YAML is registered up-front via {@link #validateAndRegister(String, byte[])} and the
   * BPMN may fail independently without losing the YAML config.
   */
  public DeployResult deployBpmnFor(CaseTypeConfig caseType, byte[] bpmnBytes, String actorEmail) {
    BpmnValidationResult bpmnResult = bpmnValidator.validate(bpmnBytes, caseType);
    if (bpmnResult.isInvalid()) {
      return DeployResult.invalid(bpmnResult.errors());
    }
    DeploymentResult deployment =
        workflowEngine.deploy(
            new DeploymentRequest(
                caseType.id() + " v" + caseType.version(),
                bpmnResult.processDefinitionKey().orElseThrow(),
                bpmnBytes,
                caseType.id(),
                caseType.version()));
    eventPublisher.publish(
        new ConfigDeployed(
            caseType.id(),
            caseType.version(),
            deployment.deploymentId(),
            deployment.processDefinitionId(),
            actorEmail,
            deployment.deployedAt()));
    return DeployResult.ok(caseType, deployment);
  }

  private void restoreRegistryAfterEngineFailure(String id, Optional<CaseTypeConfig> prior) {
    try {
      // The registrar enforces version-monotonic register: a bare register(prior) where prior is
      // an older version than what was just written would be REJECTED_OLDER_VERSION, leaving the
      // registry advertising an unbacked config. Remove first so register() takes the
      // no-existing-entry path and writes prior cleanly.
      registrar.remove(id);
      if (prior.isPresent()) {
        registrar.register(prior.get());
      }
    } catch (RuntimeException ignored) {
      // Best-effort restore. Cascading the secondary failure would mask the original engine
      // exception. The caller still sees the original WksWorkflowEngineException; the registry
      // may briefly advertise an unbacked config — see Dev Notes §Atomic deploy.
    }
  }
}
