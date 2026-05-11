package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.CaseTypeVersionRecord;
import com.wkspower.platform.domain.config.CaseTypeVersionRegistration;
import com.wkspower.platform.domain.config.DeployResult;
import com.wkspower.platform.domain.config.RegistrationResult;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.diff.BlastRadiusReport;
import com.wkspower.platform.domain.config.diff.CaseTypeDiff;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.MappingChangeClass;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
   * Story 3.11 — active Spring-profile supplier used by the AC4 force-override path to detect the
   * {@code production} profile and refuse to bypass the blast-radius gate. Domain stays
   * framework-free: the infrastructure {@code ConfigServiceConfig} wires this as a lambda over
   * {@code Environment::getActiveProfiles}; tests inject a fixed {@link List}. Defaults to an empty
   * list (treated as dev/test) when the legacy 8-arg / 9-arg constructor is used.
   */
  private final Supplier<List<String>> activeProfilesSupplier;

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
   * Story 4.5 AC3 — 9-arg constructor that accepts the BPMN hash function. Delegates to the
   * Story-3.11 full constructor with an empty active-profiles supplier (treated as dev/test —
   * force-override is permitted, but admin must still pass {@code force=true}).
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
    this(
        source,
        registrar,
        reader,
        bpmnValidator,
        workflowEngine,
        eventPublisher,
        versionRegistry,
        mappingRegistry,
        bpmnHasher,
        List::of);
  }

  /**
   * Story 3.11 — full constructor accepting the active-profile supplier (Spring {@code
   * Environment::getActiveProfiles} adapted to a {@link Supplier}). Wired by the infrastructure
   * {@code ConfigServiceConfig}.
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
      Function<byte[], String> bpmnHasher,
      Supplier<List<String>> activeProfilesSupplier) {
    this.source = source;
    this.registrar = registrar;
    this.reader = reader;
    this.bpmnValidator = bpmnValidator;
    this.workflowEngine = workflowEngine;
    this.eventPublisher = eventPublisher;
    this.versionRegistry = versionRegistry;
    this.mappingRegistry = mappingRegistry;
    this.bpmnHasher = bpmnHasher;
    this.activeProfilesSupplier =
        activeProfilesSupplier == null ? List::of : activeProfilesSupplier;
  }

  /**
   * Story 3.11 AC3 — true when the active Spring profile set contains {@code "production"}. Used by
   * {@link com.wkspower.platform.api.controller.AdminController} to refuse {@code force=true}
   * deploys before the multipart body is parsed. Matches the convention used by {@code
   * ProductionBootstrapValidator} (literal string comparison; no abstraction).
   */
  public boolean isProductionProfile() {
    List<String> profiles = activeProfilesSupplier.get();
    return profiles != null && profiles.contains("production");
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
    return validateAndRegister(sourceName, bytes, "system:startup", false, false);
  }

  /**
   * Story 3.4 — overload that accepts {@code publishedBy}; threaded from the admin REST surface
   * with the actor email (Spring Security context) and from the startup loader with {@code
   * "system:startup"}. Defaults {@code bumpRequested=false}.
   */
  public ValidationResult validateAndRegister(String sourceName, byte[] bytes, String publishedBy) {
    return validateAndRegister(sourceName, bytes, publishedBy, false, false);
  }

  /** Story 3.8 — overload accepting {@code bumpRequested}; delegates with {@code force=false}. */
  public ValidationResult validateAndRegister(
      String sourceName, byte[] bytes, String publishedBy, boolean bumpRequested) {
    return validateAndRegister(sourceName, bytes, publishedBy, bumpRequested, false);
  }

  /**
   * Story 3.11 — full overload accepting {@code forceRequested}. The blast-radius classifier still
   * runs here when a prior version exists; {@code forceRequested=true} only matters when the prior
   * YAML cannot be loaded or re-parsed (the AC4 dev/test escape hatch for the unparseable-prior
   * path). Production-profile rejection of {@code force=true} happens at the controller layer (AC3)
   * — by the time this method runs in production, {@code forceRequested} is guaranteed false.
   *
   * @param bumpRequested {@code true} when the caller explicitly requested a version bump
   * @param forceRequested {@code true} when the caller supplied {@code ?force=true} (dev/test
   *     unparseable-prior override). Requires {@code bumpRequested=true} to take effect — the admin
   *     controller already enforces the pairing pre-parse, so this method assumes the invariant
   *     holds.
   */
  public ValidationResult validateAndRegister(
      String sourceName,
      byte[] bytes,
      String publishedBy,
      boolean bumpRequested,
      boolean forceRequested) {
    ValidationResult result = this.source.loadBytes(sourceName, bytes);
    if (result.isInvalid() || result.config().isEmpty()) {
      return result;
    }
    CaseTypeConfig validated = result.config().get();

    // Story 3.8 AC2 / AC3 / AC4 — blast-radius gate (runs BEFORE applyVersionRegistry per AC2
    // ordering invariant: must not touch the engine or any registry before the gate passes).
    Optional<Integer> priorVersion = versionRegistry.currentVersion(validated.id());
    if (priorVersion.isPresent()) {
      ValidationResult gateResult =
          runBlastRadiusGate(validated, result, priorVersion.get(), bumpRequested, forceRequested);
      if (gateResult != null) {
        return gateResult; // Rejected — return the error result
      }
    } else if (bumpRequested) {
      // AC4: first deploy with bumpVersion=true — warn + ignore
      log.warn(
          "ConfigService.validateAndRegister: bumpVersion=true ignored for caseTypeId={}"
              + " — no prior version exists",
          validated.id());
    }

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
   * Story 3.8 — run the blast-radius classifier against the prior version. Returns a {@link
   * ValidationResult#invalid(List) invalid result} when the change is mutate-class and {@code
   * bumpRequested=false}; returns {@code null} when the gate passes (caller should continue).
   *
   * <p>Story 3.11 — adds {@code forceRequested}. Force-override ONLY waives the WKS-CFG-030
   * unparseable-prior path (AC4). It does NOT waive WKS-CFG-029 (mutate-class without bump — that
   * path requires the classifier to have run, which {@code force=true} skips entirely; the
   * mandatory {@code bumpRequested=true} pairing means the operator pays the same cost anyway). It
   * does NOT waive WKS-CFG-001..028 validator findings on the candidate YAML. The schema-drift case
   * is NOT eligible for force-override — AC1's lenient path handles it cleanly without operator
   * intervention; if lenient parsing succeeds {@code forceRequested} is silently ignored.
   *
   * <p>This method does NOT throw — it returns an invalid ValidationResult which the caller must
   * propagate. This mirrors the existing multi-error pattern in {@code ConfigService}.
   */
  private ValidationResult runBlastRadiusGate(
      CaseTypeConfig validated,
      ValidationResult yamlResult,
      int priorVersionNum,
      boolean bumpRequested,
      boolean forceRequested) {
    CaseTypeConfig prev = loadPriorConfig(validated.id(), priorVersionNum);
    if (prev == null) {
      // Story 3.11 AC4 — dev/test force-override for unparseable prior. Production-profile
      // rejection already happened at the controller layer (AC3); by the time we're here in
      // production, forceRequested is guaranteed false. The bumpRequested pairing is also
      // enforced at the controller layer (AC4 hard rule), but defensively re-check here so
      // direct service callers (tests, future call sites) can't bypass.
      if (forceRequested && bumpRequested) {
        log.warn(
            "ConfigService.runBlastRadiusGate: force-override active for caseTypeId={} v{}"
                + " — WKS-CFG-030 path bypassed (prior YAML unparseable); blast-radius"
                + " classification SKIPPED, deploy proceeding under admin force-override",
            validated.id(),
            priorVersionNum);
        return null; // Gate bypassed
      }
      // Story 3.8 PR #417 follow-up — fail CLOSED. AC2 requires the gate to apply on every
      // deploy with a prior version; if we cannot load or re-parse the prior YAML we cannot
      // classify the change, and silently bypassing the gate would let mutate-class edits ship
      // unchecked. Reject with WKS-CFG-030 so the operator can investigate (corrupt row,
      // missing bytes, schema drift).
      log.error(
          "ConfigService.deploy: REJECTED — prior YAML for caseTypeId={} v{} could not be loaded"
              + " or re-parsed; blast-radius gate cannot apply (fail-closed, WKS-CFG-030)",
          validated.id(),
          priorVersionNum);
      return ValidationResult.invalid(
          List.of(
              ErrorDetail.of(
                  ErrorCode.WKS_CFG_030.wire(),
                  "Blast-radius gate could not apply — prior version v"
                      + priorVersionNum
                      + " for caseTypeId="
                      + validated.id()
                      + " could not be loaded or re-parsed. Deploy rejected fail-closed.")));
    }

    MappingDefinition prevMapping =
        mappingRegistry
            .resolve(
                new CaseTypeRef(validated.id(), String.valueOf(priorVersionNum)),
                String.valueOf(priorVersionNum))
            .orElse(MappingDefinition.empty());
    MappingDefinition nextMapping =
        yamlResult.mappingDefinition().orElse(MappingDefinition.empty());

    BlastRadiusReport report = CaseTypeDiff.classify(prev, validated, prevMapping, nextMapping);

    if (report.changeClass() == MappingChangeClass.MUTATE_CLASS && !bumpRequested) {
      String paths =
          report.mutateDeltas().stream()
              .map(com.wkspower.platform.domain.config.diff.Delta::path)
              .limit(5)
              .collect(Collectors.joining(", "));
      log.info(
          "ConfigService.deploy: REJECTED mutate-class without bumpVersion for caseTypeId={}"
              + " — {} delta(s)",
          validated.id(),
          report.mutateDeltas().size());
      return ValidationResult.invalidWithMeta(
          List.of(
              ErrorDetail.of(
                  ErrorCode.WKS_CFG_029.wire(),
                  "Mutate-class change requires version bump — "
                      + report.mutateDeltas().size()
                      + " delta(s): "
                      + paths)),
          Map.of("blastRadius", report));
    }

    if (bumpRequested) {
      log.info(
          "ConfigService.deploy: bumpVersion=true with {} change for caseTypeId={}"
              + " — version bumped per admin opt-in",
          report.changeClass(),
          validated.id());
    }

    return null; // Gate passed
  }

  /**
   * Story 3.8 — load the prior {@link CaseTypeConfig} from the version registry's stored YAML.
   * Returns {@code null} when the YAML cannot be loaded or re-parsed (fail-open at this layer;
   * caller emits WKS-CFG-030).
   *
   * <p>Story 3.11 AC1 — strict-then-lenient fallback. Try the strict {@link
   * CaseTypeSource#loadBytes} first; on failure (typically schema-drift: prior YAML written under
   * an older config schema with unknown legacy keys, or mapping-subtree records that dropped
   * {@code @JsonIgnoreProperties(ignoreUnknown = true)} in Story 4.3.1 AC8), try {@link
   * CaseTypeSource#loadBytesLenient} which tolerates unknown keys for diff purposes only. INFO log
   * emitted exactly once per gate-pass when the lenient path was actually needed (strict failed but
   * lenient succeeded). Returns {@code null} ONLY when BOTH paths fail.
   */
  private CaseTypeConfig loadPriorConfig(String caseTypeId, int version) {
    Optional<CaseTypeVersionRecord> record = versionRegistry.findVersion(caseTypeId, version);
    if (record.isEmpty() || record.get().rawYaml() == null) {
      return null;
    }
    byte[] yaml = record.get().rawYaml();
    ValidationResult strict = source.loadBytes("prior:" + caseTypeId + ":" + version, yaml);
    if (strict.config().isPresent()) {
      return strict.config().get();
    }
    // Strict failed — try lenient (Story 3.11 AC1).
    ValidationResult lenient = source.loadBytesLenient("prior:" + caseTypeId + ":" + version, yaml);
    if (lenient.config().isPresent()) {
      log.info(
          "ConfigService.runBlastRadiusGate: prior YAML for caseTypeId={} v{} required lenient"
              + " re-parse (schema-drift); blast-radius classification proceeded",
          caseTypeId,
          version);
      return lenient.config().get();
    }
    return null; // Both paths failed — caller emits WKS-CFG-030 (AC2).
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
  /**
   * Overload that accepts a BPMN filename so the MappingValidator can cross-validate attachment
   * file references against the supplied BPMN bytes. Called from the admin deploy endpoint which
   * has the multipart filename available. Callers that lack a filename (startup loader BPMN path,
   * tests) fall through to the two-arg overload below.
   */
  public DeployResult deploy(
      byte[] yamlBytes, byte[] bpmnBytes, String bpmnFilename, String actorEmail) {
    return deploy(yamlBytes, bpmnBytes, bpmnFilename, actorEmail, false, false);
  }

  /** Story 3.8 — overload that threads {@code bumpRequested}; delegates with force=false. */
  public DeployResult deploy(
      byte[] yamlBytes,
      byte[] bpmnBytes,
      String bpmnFilename,
      String actorEmail,
      boolean bumpRequested) {
    return deploy(yamlBytes, bpmnBytes, bpmnFilename, actorEmail, bumpRequested, false);
  }

  /** Story 3.11 — full overload threading {@code forceRequested}. */
  public DeployResult deploy(
      byte[] yamlBytes,
      byte[] bpmnBytes,
      String bpmnFilename,
      String actorEmail,
      boolean bumpRequested,
      boolean forceRequested) {
    Map<String, byte[]> bpmnByName =
        (bpmnFilename != null && !bpmnFilename.isBlank() && bpmnBytes != null)
            ? Map.of(bpmnFilename, bpmnBytes)
            : Map.of();
    ValidationResult yamlResult = source.loadBytes("api-deploy.yaml", yamlBytes, bpmnByName);
    return deployWithYamlResult(
        yamlResult, yamlBytes, bpmnBytes, actorEmail, bumpRequested, forceRequested);
  }

  public DeployResult deploy(byte[] yamlBytes, byte[] bpmnBytes, String actorEmail) {
    return deploy(yamlBytes, bpmnBytes, actorEmail, false, false);
  }

  /** Story 3.8 — overload that threads {@code bumpRequested}; delegates with force=false. */
  public DeployResult deploy(
      byte[] yamlBytes, byte[] bpmnBytes, String actorEmail, boolean bumpRequested) {
    return deploy(yamlBytes, bpmnBytes, actorEmail, bumpRequested, false);
  }

  /** Story 3.11 — full overload threading {@code forceRequested} (no BPMN filename variant). */
  public DeployResult deploy(
      byte[] yamlBytes,
      byte[] bpmnBytes,
      String actorEmail,
      boolean bumpRequested,
      boolean forceRequested) {
    ValidationResult yamlResult = source.loadBytes("api-deploy.yaml", yamlBytes);
    return deployWithYamlResult(
        yamlResult, yamlBytes, bpmnBytes, actorEmail, bumpRequested, forceRequested);
  }

  private DeployResult deployWithYamlResult(
      ValidationResult yamlResult, byte[] yamlBytes, byte[] bpmnBytes, String actorEmail) {
    return deployWithYamlResult(yamlResult, yamlBytes, bpmnBytes, actorEmail, false, false);
  }

  private DeployResult deployWithYamlResult(
      ValidationResult yamlResult,
      byte[] yamlBytes,
      byte[] bpmnBytes,
      String actorEmail,
      boolean bumpRequested,
      boolean forceRequested) {
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

    // Story 3.8 AC2 — blast-radius gate. Runs BEFORE fingerprint computation and BEFORE engine
    // deploy (Story 4.5 AC1 ordering invariant: gate before any side-effect).
    Optional<Integer> priorVersion = versionRegistry.currentVersion(caseType.id());
    if (priorVersion.isPresent()) {
      ValidationResult gateResult =
          runBlastRadiusGate(
              caseType, yamlResult, priorVersion.get(), bumpRequested, forceRequested);
      if (gateResult != null) {
        // Rejected — propagate meta (blastRadius report) in the DeployResult
        return DeployResult.invalidWithMeta(gateResult.errors(), gateResult.responseMeta());
      }
    } else if (bumpRequested) {
      // AC4: first deploy with bumpVersion=true — warn + ignore
      log.warn(
          "ConfigService.deploy: bumpVersion=true ignored for caseTypeId={} — no prior version"
              + " exists",
          caseType.id());
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
            // Re-register in the in-memory registry — the case type may be absent after a JVM
            // restart even though its version row exists in the DB (the startup loader only reads
            // the case-types directory, not the version table). Skipping engine deploy is correct;
            // skipping the registry write is not.
            RegistrationResult idempotentReg = registrar.register(versionedCaseType);
            if (idempotentReg.outcome() == RegistrationResult.Outcome.REJECTED_OLDER_VERSION) {
              return DeployResult.invalid(idempotentReg.errors());
            }
            ValidationResult idempotentYamlResult =
                ValidationResult.ok(
                    versionedCaseType,
                    yamlResult.warnings(),
                    yamlResult.mappingDefinition().orElse(MappingDefinition.empty()));
            publishMappingToRegistry(idempotentYamlResult);
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
