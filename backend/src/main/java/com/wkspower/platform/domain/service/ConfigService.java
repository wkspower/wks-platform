package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.CaseTypeVersionRegistration;
import com.wkspower.platform.domain.config.DeployResult;
import com.wkspower.platform.domain.config.RegistrationResult;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.event.ConfigDeployed;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.port.BpmnValidationService;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.CaseTypeRegistrar;
import com.wkspower.platform.domain.port.CaseTypeSource;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.workflow.BpmnValidationResult;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

  private final CaseTypeSource source;
  private final CaseTypeRegistrar registrar;
  private final CaseTypeReader reader;
  private final BpmnValidationService bpmnValidator;
  private final WorkflowEngine workflowEngine;
  private final EventPublisher eventPublisher;
  private final MappingRegistry mappingRegistry;

  /**
   * Story 3.4 / Decision 20 — every successful YAML validate flows through {@link
   * #applyVersionRegistry(CaseTypeConfig, byte[], String)} before in-memory {@link
   * CaseTypeRegistrar#register(CaseTypeConfig)}. Registry assigns the authoritative {@code
   * version}; the in-memory CaseTypeRegistry holds the registry-overridden value.
   */
  private final CaseTypeVersionRegistry versionRegistry;

  /**
   * Per-{@code caseTypeId} mutex (Story 2.4 folded debt #2 — TOCTOU on concurrent deploys of the
   * same case-type id). Two threads racing the {@code reader.find → registrar.register} window
   * could both observe the same prior state, leading to interleaved registry writes. Locking on a
   * stable per-key monitor closes that window.
   */
  private final Map<String, Object> deployLocks = new ConcurrentHashMap<>();

  public ConfigService(
      CaseTypeSource source,
      CaseTypeRegistrar registrar,
      CaseTypeReader reader,
      BpmnValidationService bpmnValidator,
      WorkflowEngine workflowEngine,
      EventPublisher eventPublisher,
      CaseTypeVersionRegistry versionRegistry,
      MappingRegistry mappingRegistry) {
    this.source = source;
    this.registrar = registrar;
    this.reader = reader;
    this.bpmnValidator = bpmnValidator;
    this.workflowEngine = workflowEngine;
    this.eventPublisher = eventPublisher;
    this.versionRegistry = versionRegistry;
    this.mappingRegistry = mappingRegistry;
  }

  /**
   * Load {@code file}, validate it, and on success register with the registry. Returns the {@link
   * ValidationResult} so callers can react to errors.
   *
   * <p>Story 3.4 — runs the path-based source loader (preserving the existing contract for stubs
   * that route through {@link CaseTypeSource#load(Path)}), then reads the raw bytes for the
   * version-registry write.
   */
  public ValidationResult validateAndRegister(Path file) {
    ValidationResult result = source.load(file);
    if (result.isInvalid() || result.config().isEmpty()) {
      return result;
    }
    byte[] bytes;
    try {
      bytes = Files.readAllBytes(file);
    } catch (IOException e) {
      // Cannot reach the registry without the raw bytes — surface as catastrophic.
      return ValidationResult.invalid(
          List.of(
              ErrorDetail.of(
                  "WKS-CFG-099", "I/O failure reading " + file + ": " + e.getMessage())));
    }
    CaseTypeConfig validated = result.config().get();
    CaseTypeConfig versioned = applyVersionRegistry(validated, bytes, "system:startup");
    RegistrationResult reg = registrar.register(versioned);
    if (reg.outcome() == RegistrationResult.Outcome.REJECTED_OLDER_VERSION) {
      return ValidationResult.invalid(reg.errors());
    }
    // Story 4.3.1 AC1 — preserve the original ValidationResult's MappingDefinition through the
    // registry-binding rebuild. The 2-arg ValidationResult.ok(versioned, warnings) overload
    // hardcodes Optional.empty() for mappingDefinition, so publishMappingToRegistry would
    // register MappingDefinition.empty() under (caseTypeId, registryVersion) — every backend
    // signal would then hit WKS-MAP-404 in production despite tests passing. Use the 3-arg
    // overload that threads result.mappingDefinition() forward.
    ValidationResult versionedResult =
        ValidationResult.ok(
            versioned,
            result.warnings(),
            result.mappingDefinition().orElse(MappingDefinition.empty()));
    publishMappingToRegistry(versionedResult);
    return versionedResult;
  }

  /** Byte-driven YAML-only variant. Retained for the startup loader's BPMN-missing path. */
  public ValidationResult validateAndRegister(String sourceName, byte[] bytes) {
    return validateAndRegister(sourceName, bytes, "system:startup");
  }

  /**
   * Story 3.4 — overload that accepts {@code publishedBy}; threaded from the admin REST surface
   * with the actor email (Spring Security context) and from the startup loader with {@code
   * "system:startup"}.
   */
  public ValidationResult validateAndRegister(String sourceName, byte[] bytes, String publishedBy) {
    ValidationResult result = this.source.loadBytes(sourceName, bytes);
    if (result.isInvalid() || result.config().isEmpty()) {
      return result;
    }
    CaseTypeConfig validated = result.config().get();
    CaseTypeConfig versioned = applyVersionRegistry(validated, bytes, publishedBy);
    RegistrationResult reg = registrar.register(versioned);
    if (reg.outcome() == RegistrationResult.Outcome.REJECTED_OLDER_VERSION) {
      return ValidationResult.invalid(reg.errors());
    }
    // Carry the version-overridden config back to callers (startup loader, admin endpoint) so the
    // ConfigDeployed event and any post-validate logging see the registry-authoritative version.
    // Story 4.3 — publishMappingToRegistry runs against the same versioned result so the
    // (caseTypeId, version) key in MappingRegistry matches what the in-memory CaseTypeRegistry
    // advertises. Story 4.3.1 AC1 — thread result.mappingDefinition() through the rebuild so the
    // validated MappingDefinition reaches MappingRegistry.register; the 2-arg ok() overload would
    // silently downgrade to MappingDefinition.empty().
    ValidationResult versionedResult =
        ValidationResult.ok(
            versioned,
            result.warnings(),
            result.mappingDefinition().orElse(MappingDefinition.empty()));
    publishMappingToRegistry(versionedResult);
    return versionedResult;
  }

  /**
   * Story 4.3 AC9 — populate {@link MappingRegistry} keyed by {@code (caseTypeId, version)} after a
   * successful CaseType registration. Empty {@link MappingDefinition} is registered for every
   * zero-attachment CaseType (D22, first-class). When the registry was not provided (legacy
   * constructor) the call short-circuits — the router's {@code WKS-MAP-404} path covers the case.
   */
  private void publishMappingToRegistry(ValidationResult result) {
    if (mappingRegistry == null || result.config().isEmpty()) {
      return;
    }
    CaseTypeConfig config = result.config().get();
    MappingDefinition definition = result.mappingDefinition().orElse(MappingDefinition.empty());
    String version = String.valueOf(config.version());
    mappingRegistry.register(new CaseTypeRef(config.id(), version), version, definition);
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

    Object lock = deployLocks.computeIfAbsent(caseType.id(), k -> new Object());
    Optional<CaseTypeConfig> priorState;
    RegistrationResult reg;
    DeploymentResult deployment;
    synchronized (lock) {
      priorState = reader.find(caseType.id());

      // Story 3.4 / Decision 20 — registry assigns the authoritative version BEFORE the
      // in-memory CaseTypeRegistrar swap so caseType.version() carries the registry value
      // through to the engine deploy + ConfigDeployed event.
      caseType = applyVersionRegistry(caseType, yamlBytes, actorEmail);

      reg = registrar.register(caseType);
      if (reg.outcome() == RegistrationResult.Outcome.REJECTED_OLDER_VERSION) {
        return DeployResult.invalid(reg.errors());
      }
      // Story 4.3.1 AC2 — register MappingRegistry under the registry-assigned version, NOT the
      // author-supplied version carried by yamlResult.config(). When the version registry assigns
      // version=1 (idempotent first-deploy or hash-mismatch override) but the YAML declared
      // version=5, the original yamlResult would key the mapping under version "5" while the
      // CaseTypeRegistry advertises "1" to CaseService.create — every signal would WKS-MAP-404.
      // Rebuild the ValidationResult around the version-overridden caseType, preserving
      // mappingDefinition + warnings.
      ValidationResult versionedYamlResult =
          ValidationResult.ok(
              caseType,
              yamlResult.warnings(),
              yamlResult.mappingDefinition().orElse(MappingDefinition.empty()));
      publishMappingToRegistry(versionedYamlResult);

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

      // Publish inside the lock so two concurrent deploys of the same caseTypeId cannot interleave
      // their event publication and leave the ProcessDefinitionKeyCache stuck on an older mapping
      // (Story 2.4 review).
      eventPublisher.publish(
          new ConfigDeployed(
              caseType.id(),
              caseType.version(),
              deployment.deploymentId(),
              deployment.processDefinitionKey(),
              deployment.processDefinitionId(),
              actorEmail,
              deployment.deployedAt()));
    }

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
            deployment.processDefinitionKey(),
            deployment.processDefinitionId(),
            actorEmail,
            deployment.deployedAt()));
    return DeployResult.ok(caseType, deployment);
  }

  /**
   * Story 3.4 / Decision 20 — write the immutable version row and override the in-memory {@link
   * CaseTypeConfig#version()} with the registry-assigned value.
   *
   * <ol>
   *   <li>Compute canonical SHA-256 of {@code rawYamlBytes} via the version registry's hasher.
   *   <li>Look up by hash; on hit return existing version (idempotent).
   *   <li>Otherwise insert at {@code max(version)+1} and return the new version.
   *   <li>If the author-supplied YAML carried a {@code version:} that disagrees with the
   *       registry-assigned value, log a WARN-level structured line. Per Q1 LOCKED the registry is
   *       authoritative; no error code is emitted.
   * </ol>
   *
   * <p>The registry write and the in-memory {@link CaseTypeRegistrar} register are NOT
   * transactionally coupled — JPA writes the version row, the in-memory map mutation is a {@link
   * ConcurrentHashMap} swap. The version row is the durable record; if the in-memory swap fails
   * after the row is written, the next reload re-reads from the registry — eventual consistency is
   * acceptable per Decision 20.
   */
  private CaseTypeConfig applyVersionRegistry(
      CaseTypeConfig caseType, byte[] rawYamlBytes, String actor) {
    String publishedBy = (actor == null || actor.isBlank()) ? "system:startup" : actor;
    int authorVersion = caseType.version();
    CaseTypeVersionRegistration result =
        versionRegistry.register(caseType.id(), rawYamlBytes, publishedBy);
    int registryVersion = result.version();
    // Story 3.4.1 AC6 (finding I8) — gate the author-version-mismatch WARN on actual registration
    // (REGISTERED outcome). Idempotent re-deploys (file-watcher hot-reload, polling redeploy)
    // produce CaseTypeVersionRegistration.Outcome.IDEMPOTENT and previously emitted this WARN on
    // every poll, drowning the log buffer in production hot-reload scenarios. Emit once per
    // outcome that actually changed registry state.
    if (authorVersion != registryVersion
        && result.outcome() == CaseTypeVersionRegistration.Outcome.REGISTERED) {
      log.warn(
          "author-supplied version {} for {} differs from registry-assigned version {};"
              + " registry is authoritative (Decision 20)",
          authorVersion,
          caseType.id(),
          registryVersion);
    }
    return caseType.withVersion(registryVersion);
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
    } catch (RuntimeException secondary) {
      // Best-effort restore. Cascading would mask the original engine exception, so we log at
      // ERROR and let the original bubble. The registry may end up empty for this caseTypeId
      // until the next successful deploy — operators can see this in the logs (Story 2.4 review).
      log.error(
          "ConfigService: registry rollback after engine failure for caseTypeId={} also failed —"
              + " registry may have no entry for this id until the next successful deploy",
          id,
          secondary);
    }
  }
}
