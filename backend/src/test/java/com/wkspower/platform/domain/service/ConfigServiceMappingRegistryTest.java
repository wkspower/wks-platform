package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.DeployResult;
import com.wkspower.platform.domain.config.RegistrationResult;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.EndEventMapping;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.port.BpmnValidationService;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.CaseTypeRegistrar;
import com.wkspower.platform.domain.port.CaseTypeSource;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.workflow.BpmnValidationResult;
import com.wkspower.platform.domain.workflow.DeploymentInfo;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import com.wkspower.platform.infrastructure.config.CaseTypeContentHasher;
import com.wkspower.platform.testsupport.FakeCaseTypeVersionRegistry;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Story 4.3.1 AC1 + AC2 — pin the C1 fix from the Sprint 1-3 retroactive code review. {@link
 * ConfigService#validateAndRegister(String, byte[], String)} (startup-loader path) and {@link
 * ConfigService#deploy(byte[], byte[], String)} (admin-deploy path) must both register the
 * validated {@link MappingDefinition} into {@link MappingRegistry} keyed by the registry-overridden
 * {@code (caseTypeId, version)} — not the empty mapping the 2-arg {@code
 * ValidationResult.ok(versioned, warnings)} overload silently produces, and not the author-supplied
 * version that {@code yamlResult.config()} carries before the version-registry override.
 */
class ConfigServiceMappingRegistryTest {

  private static final byte[] YAML_BYTES = "id: app".getBytes();
  private static final byte[] BPMN_BYTES = "<bpmn/>".getBytes();

  // ---------- AC1 — startup-loader path ----------

  @Test
  void validateAndRegisterPreservesMappingDefinitionThroughRegistryRebuild() {
    CaseTypeConfig cfg = caseType(1);
    MappingDefinition mapping =
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    "x.bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.of(new EndEventMapping("stage1 -> stage2")),
                    Map.of(),
                    List.of())));
    ValidationResult validatorOutput = ValidationResult.ok(cfg, List.of(), mapping);
    StubSource source = new StubSource(validatorOutput);
    StubRegistrar registrar = new StubRegistrar();
    MappingRegistry mappingRegistry = new MappingRegistry();
    ConfigService svc =
        new ConfigService(
            source,
            registrar,
            new StubReader(),
            new StubBpmn(BpmnValidationResult.ok("noop")),
            new StubEngine(),
            new RecordingPublisher(),
            new FakeCaseTypeVersionRegistry(),
            mappingRegistry);

    svc.validateAndRegister("app.yaml", YAML_BYTES);

    Optional<MappingDefinition> resolved =
        mappingRegistry.resolve(new CaseTypeRef(cfg.id(), "1"), "1");
    assertThat(resolved)
        .as("AC1: MappingRegistry must hold the validated mapping, not empty()")
        .isPresent();
    assertThat(resolved.get().attachments()).hasSize(1);
    assertThat(resolved.get().attachments().get(0).endEventMapping())
        .isPresent()
        .get()
        .extracting(EndEventMapping::stageTransition)
        .isEqualTo("stage1 -> stage2");
  }

  // ---------- AC2 — admin-deploy path with version override ----------

  @Test
  void deployRegistersMappingUnderRegistryAssignedVersionNotAuthorVersion() {
    // Author declares version=5, but the version registry assigns version=1 (first deploy).
    CaseTypeConfig authorCfg = caseType(5);
    MappingDefinition mapping =
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    "x.bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.of(new EndEventMapping("stage1 -> stage2")),
                    Map.of(),
                    List.of())));
    ValidationResult validatorOutput = ValidationResult.ok(authorCfg, List.of(), mapping);
    StubSource source = new StubSource(validatorOutput);
    StubRegistrar registrar = new StubRegistrar();
    MappingRegistry mappingRegistry = new MappingRegistry();
    FakeCaseTypeVersionRegistry versionRegistry = new FakeCaseTypeVersionRegistry();
    // P11 — use the real CaseTypeContentHasher::hashBytes so the BPMN hash is computed for real;
    // this validates that a non-null 64-char SHA-256 hex string lands in the registry row after
    // a successful deploy.
    ConfigService svc =
        new ConfigService(
            source,
            registrar,
            new StubReader(),
            new StubBpmn(BpmnValidationResult.ok("appProc")),
            new StubEngine(),
            new RecordingPublisher(),
            versionRegistry,
            mappingRegistry,
            CaseTypeContentHasher::hashBytes);

    DeployResult result = svc.deploy(YAML_BYTES, BPMN_BYTES, "ops@x");

    assertThat(result.isInvalid()).isFalse();
    // The caseType returned to callers carries version=1 (registry override).
    assertThat(result.caseType()).isPresent();
    assertThat(result.caseType().get().version()).isEqualTo(1);

    // AC2 — MappingRegistry MUST be keyed by version "1" (registry-assigned), not "5" (author).
    assertThat(mappingRegistry.resolve(new CaseTypeRef(authorCfg.id(), "1"), "1"))
        .as("AC2: mapping registered under registry-assigned version 1")
        .isPresent();
    assertThat(mappingRegistry.resolve(new CaseTypeRef(authorCfg.id(), "5"), "5"))
        .as("AC2: must NOT be registered under the author-supplied version 5")
        .isEmpty();

    // P11 — assert the version row carries a real non-null 64-char SHA-256 hex bpmnContentHash.
    var record = versionRegistry.findVersion(authorCfg.id(), 1);
    assertThat(record).as("version row must exist after deploy").isPresent();
    assertThat(record.get().bpmnContentHash())
        .as("P11: bpmnContentHash must be a non-null 64-char SHA-256 hex string")
        .isNotNull()
        .hasSize(64)
        .matches("[0-9a-f]{64}");
  }

  // ---------- helpers ----------

  private static CaseTypeConfig caseType(int version) {
    return new CaseTypeConfig(
        "app",
        "App",
        version,
        null,
        new WorkflowRef("app.bpmn"),
        List.<FieldDefinition>of(),
        List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
        List.of(),
        List.of(new RoleDefinition("admin", List.of())));
  }

  private static final class StubSource implements CaseTypeSource {
    private final ValidationResult result;

    StubSource(ValidationResult result) {
      this.result = result;
    }

    @Override
    public ValidationResult load(Path file) {
      return result;
    }

    @Override
    public ValidationResult loadBytes(String s, byte[] b) {
      return result;
    }
  }

  private static final class StubBpmn implements BpmnValidationService {
    private final BpmnValidationResult result;

    StubBpmn(BpmnValidationResult result) {
      this.result = result;
    }

    @Override
    public BpmnValidationResult validate(byte[] xml, CaseTypeConfig ct) {
      return result;
    }
  }

  private static final class StubRegistrar implements CaseTypeRegistrar {
    final Map<String, CaseTypeConfig> byId = new HashMap<>();

    @Override
    public RegistrationResult register(CaseTypeConfig cfg) {
      byId.put(cfg.id(), cfg);
      return RegistrationResult.registered();
    }

    @Override
    public void remove(String id) {
      byId.remove(id);
    }
  }

  private static final class StubReader implements CaseTypeReader {
    @Override
    public Optional<CaseTypeConfig> find(String id) {
      return Optional.empty();
    }

    @Override
    public java.util.Collection<CaseTypeConfig> all() {
      return List.of();
    }

    @Override
    public Optional<CaseTypeConfig> findVersion(String id, int version) {
      return Optional.empty();
    }
  }

  private static final class StubEngine implements WorkflowEngine {
    @Override
    public DeploymentResult deploy(DeploymentRequest req) {
      return new DeploymentResult(
          "dep-1", req.processDefinitionKey(), "procDef-1", 1, Instant.now());
    }

    @Override
    public Optional<DeploymentInfo> latestDeployment(String key) {
      return Optional.empty();
    }

    @Override
    public String startProcessInstance(String key, Map<String, Object> vars) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<com.wkspower.platform.domain.model.Task> findTask(String taskId) {
      return Optional.empty();
    }

    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {}

    @Override
    public void claimTask(String taskId, java.util.UUID userId) {}

    @Override
    public void signalTransition(String pid, String action, Map<String, Object> vars) {}

    @Override
    public List<com.wkspower.platform.domain.model.Task> findTasksByCase(java.util.UUID caseId) {
      return List.of();
    }

    @Override
    public String readActionLabel(String processDefinitionId, String taskDefinitionKey) {
      return null;
    }
  }

  private static final class RecordingPublisher implements EventPublisher {
    final List<Object> events = new ArrayList<>();

    @Override
    public synchronized void publish(Object event) {
      events.add(event);
    }
  }

  // ---------- AC1/AC2 — atomic failure on engine error (Story 4.5) ----------

  @Test
  void deployFailsAtomicallyOnEngineError() {
    // Story 4.5 AC1/AC2 — engine deploy is step 5 (BEFORE registry writes at steps 6-7).
    // When the engine throws, deploy() must return DeployResult.invalid(WKS-CFG-025)
    // without writing any version row or MappingRegistry entry.
    CaseTypeConfig cfg = caseType(1);
    MappingDefinition mapping =
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    "x.bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.of(new EndEventMapping("stage1 -> stage2")),
                    Map.of(),
                    List.of())));
    ValidationResult validatorOutput = ValidationResult.ok(cfg, List.of(), mapping);
    StubSource source = new StubSource(validatorOutput);
    StubRegistrar registrar = new StubRegistrar();
    MappingRegistry mappingRegistry = new MappingRegistry();
    FakeCaseTypeVersionRegistry versionRegistry = new FakeCaseTypeVersionRegistry();

    // Engine throws on deploy — inline WorkflowEngine that always fails
    WorkflowEngine failingEngine =
        new WorkflowEngine() {
          @Override
          public DeploymentResult deploy(DeploymentRequest req) {
            throw new RuntimeException("engine-down");
          }

          @Override
          public Optional<DeploymentInfo> latestDeployment(String key) {
            return Optional.empty();
          }

          @Override
          public String startProcessInstance(String key, Map<String, Object> vars) {
            throw new UnsupportedOperationException();
          }

          @Override
          public Optional<com.wkspower.platform.domain.model.Task> findTask(String taskId) {
            return Optional.empty();
          }

          @Override
          public void completeTask(String taskId, Map<String, Object> variables) {}

          @Override
          public void claimTask(String taskId, java.util.UUID userId) {}

          @Override
          public void signalTransition(String pid, String action, Map<String, Object> vars) {}

          @Override
          public List<com.wkspower.platform.domain.model.Task> findTasksByCase(
              java.util.UUID caseId) {
            return List.of();
          }

          @Override
          public String readActionLabel(String processDefinitionId, String taskDefinitionKey) {
            return null;
          }
        };

    ConfigService svc =
        new ConfigService(
            source,
            registrar,
            new StubReader(),
            new StubBpmn(BpmnValidationResult.ok("noop")),
            failingEngine,
            new RecordingPublisher(),
            versionRegistry,
            mappingRegistry);

    DeployResult result = svc.deploy(YAML_BYTES, BPMN_BYTES, "ops@x");

    assertThat(result.isInvalid()).isTrue();
    assertThat(result.errors())
        .extracting(com.wkspower.platform.domain.exception.ErrorDetail::code)
        .containsExactly("WKS-CFG-025");

    // AC2 — no version row written
    assertThat(versionRegistry.currentVersion(cfg.id())).isEmpty();

    // AC2 — no MappingRegistry entry written
    assertThat(mappingRegistry.resolve(new CaseTypeRef(cfg.id(), "1"), "1"))
        .as("MappingRegistry must be empty after engine failure")
        .isEmpty();

    // AC2 — in-memory registrar not written
    assertThat(registrar.byId).doesNotContainKey(cfg.id());
  }

  private static ErrorDetail err(String code, String msg) {
    return ErrorDetail.of(code, msg);
  }
}
