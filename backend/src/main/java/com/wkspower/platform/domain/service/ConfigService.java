package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.CaseTypeVersionRecord;
import com.wkspower.platform.domain.config.CaseTypeVersionRegistration;
import com.wkspower.platform.domain.config.DeployResult;
import com.wkspower.platform.domain.config.RegistrationResult;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.event.ConfigDeployed;
import com.wkspower.platform.domain.exception.ErrorCode;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
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
   * Story 4.5 AC3 — injectable BPMN hash function. Pure-bytes → hex-hash. Injected by the
   * infrastructure config as {@code CaseTypeContentHasher::hashBytes} so that {@code ConfigService}
   * stays framework-free and passes ArchUnit's hexagonal-layering rule (domain may not depend on
   * infrastructure packages). Tests inject a simple SHA-256 lambda.
   */
  private final Function<byte[], String> bpmnHasher;

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
    this(
        source,
        registrar,
        reader,
        bpmnValidator,
        workflowEngine,
        eventPublisher,
        versionRegistry,
        mappingRegistry,
        bytes -> {
          // Fallback no-op hasher: returns null (zero-attachment / test path).
          return null;
        });
  }

  /**
   * Story 4.5 AC3 — full constructor that accepts the BPMN hash function. Wired by the
   * infrastructure {@code ConfigServiceConfig} with {@code CaseTypeContentHasher::hashBytes}.
   */
  public ConfigService(
      CaseTypeSource source,
      CaseTypeRegistrar registrar,
      CaseTypeReader reader,
      BpmnValidationService bpmnValidator,
      WorkflowEngine workflowEngine,
      EventPublisher eventPublisher,
      CaseTypeVersionRegistry versionRegistry,
      MappingRegistry mappingRegistry,
      Function<byte[], String> bpmnHasher) {
    this.source = source;
    this.registrar = registrar;
    this.reader = reader;
    this.bpmnValidator = bpmnValidator;
    this.workflowEngine = workflowEngine;
    this.eventPublisher = eventPublisher;
    this.versionRegistry = versionRegistry;
    this.mappingRegistry = mappingRegistry;
    this.bpmnHasher = bpmnHasher;
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
   *
   * <p>Story 4.5 AC1 — execution order (LOCKED):
   *
   * <ol>
   *   <li>Parse CaseType YAML ({@link #source#loadBytes}).
   *   <li>Parse + validate BPMN ({@link #bpmnValidator#validate}).
   *   <li>Cross-validate Mapping YAML refs against BPMN element IDs ({@link MappingValidator} is
   *       called from inside {@code source.loadBytes} — errors surface in {@code yamlResult}).
   *   <li>Collect all validation errors from steps 1–3; if any → return {@code
   *       DeployResult.invalid(errors)} WITHOUT touching the engine or any registry.
   *   <li>Deploy BPMN to the engine ({@link WorkflowEngine#deploy}). If engine deploy fails →
   *       return {@code DeployResult.invalid(WKS-CFG-025)} WITHOUT writing to {@code
   *       case_type_versions} or {@link MappingRegistry}.
   *   <li>Register version with computed fingerprints ({@link
   *       CaseTypeVersionRegistry#register(String, byte[], String, String, String)}).
   *   <li>Register mapping ({@link MappingRegistry}).
   *   <li>Publish {@link ConfigDeployed} event.
   * </ol>
   *
   * <p>AC2 (atomic rollback) is achieved structurally by this ordering: engine deploy is the gate
   * before any registry write. The {@code synchronized(lock)} block wraps steps 5–8 so two
   * concurrent deploys of the same {@code caseTypeId} cannot interleave.
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

    // Story 4.5 AC3 — compute fingerprints BEFORE entering the lock; pure computation, no I/O.
    // bpmnBytes is non-null at this point (BPMN validation succeeded above).
    // P12 — treat empty byte array the same as null (zero-attachment) to guard against
    // CaseTypeContentHasher.hashBytes throwing IllegalArgumentException on empty input.
    final String bpmnHash =
        (bpmnBytes != null && bpmnBytes.length > 0) ? bpmnHasher.apply(bpmnBytes) : null;
    final MappingDefinition mapping =
        yamlResult.mappingDefinition().orElse(MappingDefinition.empty());
    final String mappingHash = mapping.attachments().isEmpty() ? null : mapping.computeHash();

    Object lock = deployLocks.computeIfAbsent(caseType.id(), k -> new Object());
    DeploymentResult deployment;
    synchronized (lock) {
      // Story 4.5 AC3 P2 — idempotent re-deploy short-circuit: if the current version's
      // bpmnContentHash already matches the incoming hash, skip the engine deploy entirely and
      // return the existing version. This prevents an unconditional engine deploy on every hot-
      // reload or polling redeploy that produces identical BPMN bytes.
      if (bpmnHash != null) {
        Optional<Integer> existingVersion = versionRegistry.currentVersion(caseType.id());
        if (existingVersion.isPresent()) {
          Optional<CaseTypeVersionRecord> existingRecord =
              versionRegistry.findVersion(caseType.id(), existingVersion.get());
          if (existingRecord.isPresent()
              && bpmnHash.equals(existingRecord.get().bpmnContentHash())) {
            log.debug(
                "ConfigService.deploy: bpmn hash match for caseTypeId={} version={} — skipping"
                    + " engine deploy (idempotent re-deploy)",
                caseType.id(),
                existingVersion.get());
            CaseTypeConfig versionedCaseType =
                applyVersionRegistry(caseType, yamlBytes, actorEmail, bpmnHash, mappingHash);
            return DeployResult.ok(
                versionedCaseType,
                new DeploymentResult(
                    "idempotent-skip",
                    bpmnResult.processDefinitionKey().orElseThrow(),
                    "idempotent-skip",
                    existingVersion.get(),
                    Instant.now()));
          }
        }
      }

      // Story 4.5 AC1 — step 5: deploy engine BEFORE any registry write.
      // On engine failure return WKS-CFG-025 without writing to case_type_versions or
      // MappingRegistry.
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
        log.error(
            "ConfigService.deploy: engine deploy failed for caseTypeId={} — returning"
                + " WKS-CFG-025; registry NOT written",
            caseType.id(),
            ex);
        return DeployResult.invalid(
            List.of(ErrorDetail.of(ErrorCode.WKS_CFG_025.wire(), "BPMN engine deployment failed")));
      }

      // Story 4.5 AC1 — step 6: register version with fingerprints (engine deploy succeeded).
      // Story 3.4 / Decision 20 — registry assigns the authoritative version; in-memory swap
      // follows so caseType.version() carries the registry value through to the ConfigDeployed
      // event.
      caseType = applyVersionRegistry(caseType, yamlBytes, actorEmail, bpmnHash, mappingHash);

      RegistrationResult reg = registrar.register(caseType);
      if (reg.outcome() == RegistrationResult.Outcome.REJECTED_OLDER_VERSION) {
        return DeployResult.invalid(reg.errors());
      }

      // Story 4.3.1 AC2 / Story 4.5 AC1 step 7 — register MappingRegistry under the
      // registry-assigned version, NOT the author-supplied version. Rebuild the ValidationResult
      // around the version-overridden caseType, preserving mappingDefinition + warnings.
      ValidationResult versionedYamlResult =
          ValidationResult.ok(
              caseType,
              yamlResult.warnings(),
              yamlResult.mappingDefinition().orElse(MappingDefinition.empty()));
      publishMappingToRegistry(versionedYamlResult);

      // Story 4.5 AC1 step 8 — publish inside the lock so two concurrent deploys of the same
      // caseTypeId cannot interleave their event publication (Story 2.4 review).
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
   *
   * <p>Story 4.5 AC1 P3 — follows the same atomicity ordering as {@link #deploy}: engine deploy is
   * the gate before any event publication. On engine failure, returns {@code
   * DeployResult.invalid(WKS-CFG-025)} without publishing a {@link ConfigDeployed} event — no
   * orphan version row can exist here because the YAML version row was written prior to this call,
   * but the event invariant is preserved (no event without a successful deployment).
   */
  public DeployResult deployBpmnFor(CaseTypeConfig caseType, byte[] bpmnBytes, String actorEmail) {
    BpmnValidationResult bpmnResult = bpmnValidator.validate(bpmnBytes, caseType);
    if (bpmnResult.isInvalid()) {
      return DeployResult.invalid(bpmnResult.errors());
    }
    // AC1 ordering: engine deploy FIRST. On failure → WKS-CFG-025, no event published.
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
      log.error(
          "ConfigService.deployBpmnFor: engine deploy failed for caseTypeId={} — returning"
              + " WKS-CFG-025; no event published",
          caseType.id(),
          ex);
      return DeployResult.invalid(
          List.of(ErrorDetail.of(ErrorCode.WKS_CFG_025.wire(), "BPMN engine deployment failed")));
    }
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
    return applyVersionRegistry(caseType, rawYamlBytes, actor, null, null);
  }

  /**
   * Story 4.5 AC3 — overload accepting deployment fingerprints. Used by {@link #deploy} after
   * engine deploy succeeds. Zero-attachment deploys pass {@code null} for both hashes.
   */
  private CaseTypeConfig applyVersionRegistry(
      CaseTypeConfig caseType,
      byte[] rawYamlBytes,
      String actor,
      String bpmnContentHash,
      String mappingHash) {
    String publishedBy = (actor == null || actor.isBlank()) ? "system:startup" : actor;
    int authorVersion = caseType.version();
    CaseTypeVersionRegistration result =
        versionRegistry.register(
            caseType.id(), rawYamlBytes, publishedBy, bpmnContentHash, mappingHash);
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
}
