package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.DeployResult;
import com.wkspower.platform.domain.config.RegistrationResult;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.event.ConfigDeployed;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.exception.WksWorkflowEngineException;
import com.wkspower.platform.domain.port.BpmnValidationService;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRegistrar;
import com.wkspower.platform.domain.port.CaseTypeSource;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.workflow.BpmnValidationResult;
import com.wkspower.platform.domain.workflow.DeploymentInfo;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Pure-Java enumeration of {@code ConfigService.deploy(...)} branches. No Spring context, no
 * engine, no real BPMN — every dependency is a hand-rolled stub so the orchestration logic itself
 * is the unit under test.
 */
class ConfigServiceDeployTest {

  private static final byte[] YAML_BYTES = "id: application".getBytes();
  private static final byte[] BPMN_BYTES = "<bpmn/>".getBytes();

  /**
   * Story 3.4 — every {@link ConfigService} construction in this test feeds a fresh fake version
   * registry so the registry-write side-effect is exercised but the existing assertions (which
   * check pre-registry behaviour like StubRegistrar.registered) keep working unchanged. The fake
   * starts empty; the first deploy() call writes v1.
   */
  private static ConfigService newCfg(
      CaseTypeSource source,
      CaseTypeRegistrar registrar,
      CaseTypeReader reader,
      BpmnValidationService bpmn,
      WorkflowEngine engine,
      EventPublisher publisher) {
    return new ConfigService(
        source,
        registrar,
        reader,
        bpmn,
        engine,
        publisher,
        new com.wkspower.platform.testsupport.FakeCaseTypeVersionRegistry(),
        new MappingRegistry());
  }

  // ---- four-cases enumeration --------------------------------------------

  @Test
  void yamlParseFailureProducesAggregate() {
    StubSource source = new StubSource(invalidYaml("WKS-CFG-099", "yaml broken"));
    StubBpmn bpmn = new StubBpmn(BpmnValidationResult.invalid(List.of(err("WKS-CFG-010", "bpmn"))));
    StubRegistrar registrar = new StubRegistrar();
    StubReader reader = new StubReader();
    StubEngine engine = new StubEngine();
    RecordingPublisher publisher = new RecordingPublisher();
    ConfigService svc = newCfg(source, registrar, reader, bpmn, engine, publisher);

    DeployResult result = svc.deploy(YAML_BYTES, BPMN_BYTES, "ops@x");

    assertThat(result.isInvalid()).isTrue();
    assertThat(result.errors())
        .extracting(ErrorDetail::code)
        .as("yaml + bpmn errors aggregate even when yaml fails catastrophically")
        .contains("WKS-CFG-099", "WKS-CFG-010");
    assertThat(registrar.registered).isEmpty();
    assertThat(engine.deployed).isEmpty();
    assertThat(publisher.events).isEmpty();
  }

  @Test
  void yamlValidationFailurePlusBpmnArchetypeFailureAggregate() {
    StubSource source = new StubSource(invalidYaml("WKS-CFG-001", "yaml field missing"));
    StubBpmn bpmn =
        new StubBpmn(BpmnValidationResult.invalid(List.of(err("WKS-CFG-020", "archetype"))));
    StubRegistrar registrar = new StubRegistrar();
    ConfigService svc =
        newCfg(
            source, registrar, new StubReader(), bpmn, new StubEngine(), new RecordingPublisher());

    DeployResult result = svc.deploy(YAML_BYTES, BPMN_BYTES, null);

    assertThat(result.errors())
        .extracting(ErrorDetail::code)
        .containsExactlyInAnyOrder("WKS-CFG-001", "WKS-CFG-020");
    assertThat(registrar.registered).isEmpty();
  }

  @Test
  void bpmnParseFailureWithCleanYamlSurfacesBoth() {
    CaseTypeConfig config = caseType();
    StubSource source = new StubSource(ValidationResult.ok(config));
    StubBpmn bpmn =
        new StubBpmn(BpmnValidationResult.invalid(List.of(err("WKS-CFG-010", "bpmn parse"))));
    StubRegistrar registrar = new StubRegistrar();
    ConfigService svc =
        newCfg(
            source, registrar, new StubReader(), bpmn, new StubEngine(), new RecordingPublisher());

    DeployResult result = svc.deploy(YAML_BYTES, BPMN_BYTES, null);

    assertThat(result.errors()).extracting(ErrorDetail::code).containsExactly("WKS-CFG-010");
    assertThat(registrar.registered).isEmpty();
  }

  @Test
  void bpmnValidationFailureBlocksRegisterAndDeploy() {
    StubSource source = new StubSource(ValidationResult.ok(caseType()));
    StubBpmn bpmn =
        new StubBpmn(BpmnValidationResult.invalid(List.of(err("WKS-CFG-021", "contradiction"))));
    StubRegistrar registrar = new StubRegistrar();
    StubEngine engine = new StubEngine();
    ConfigService svc =
        newCfg(source, registrar, new StubReader(), bpmn, engine, new RecordingPublisher());

    DeployResult result = svc.deploy(YAML_BYTES, BPMN_BYTES, null);

    assertThat(result.isInvalid()).isTrue();
    assertThat(registrar.registered).isEmpty();
    assertThat(engine.deployed).isEmpty();
  }

  // ---- happy path --------------------------------------------------------

  @Test
  void happyPathRegistersDeploysAndPublishesEvent() {
    CaseTypeConfig config = caseType();
    StubSource source = new StubSource(ValidationResult.ok(config));
    StubBpmn bpmn = new StubBpmn(BpmnValidationResult.ok("applicationProcess"));
    StubRegistrar registrar = new StubRegistrar();
    StubEngine engine = new StubEngine();
    RecordingPublisher publisher = new RecordingPublisher();
    ConfigService svc = newCfg(source, registrar, new StubReader(), bpmn, engine, publisher);

    DeployResult result = svc.deploy(YAML_BYTES, BPMN_BYTES, "ops@x");

    assertThat(result.isInvalid()).isFalse();
    assertThat(result.caseType()).contains(config);
    assertThat(result.deployment()).isPresent();
    assertThat(registrar.registered).containsExactly(config.id());
    assertThat(engine.deployed).hasSize(1);
    assertThat(engine.deployed.get(0).processDefinitionKey()).isEqualTo("applicationProcess");
    assertThat(publisher.events).hasSize(1);
    assertThat(publisher.events.get(0)).isInstanceOf(ConfigDeployed.class);
    ConfigDeployed event = (ConfigDeployed) publisher.events.get(0);
    assertThat(event.caseTypeId()).isEqualTo(config.id());
    assertThat(event.actorEmail()).isEqualTo("ops@x");
  }

  // ---- engine failure atomicity (Story 4.5 AC1 / AC2) -----------------------
  //
  // Under the AC1 reordering, the engine deploy is step 5 and registry writes (version +
  // mapping) are steps 6-7. Engine failure now returns DeployResult.invalid(WKS-CFG-025)
  // WITHOUT writing to case_type_versions or MappingRegistry — no rollback needed.

  @Test
  void engineFailureBeforeRegistryWriteReturnsWksCfg025() {
    // Story 4.5 AC1/AC2 — engine deploy is the gate before any registry write.
    // A pre-existing registration exists for the same case type (v1), but the incoming
    // deploy (v2 YAML) fails at the engine step. The registrar should NOT have written
    // the new version — the AC1 reorder guarantees no partial state.
    CaseTypeConfig prior = caseType();
    CaseTypeConfig incoming =
        new CaseTypeConfig(
            prior.id(),
            prior.displayName(),
            2,
            null,
            prior.workflow(),
            prior.fields(),
            prior.statuses(),
            prior.listColumns(),
            prior.roles());
    StubSource source = new StubSource(ValidationResult.ok(incoming));
    StubBpmn bpmn = new StubBpmn(BpmnValidationResult.ok("applicationProcess"));
    StubRegistrar registrar = new StubRegistrar();
    registrar.register(prior); // pre-existing registration
    StubReader reader = new StubReader();
    reader.preset.put(prior.id(), prior);
    StubEngine engine = new StubEngine();
    engine.failNext = new WksWorkflowEngineException("engine down", new RuntimeException());

    ConfigService svc = newCfg(source, registrar, reader, bpmn, engine, new RecordingPublisher());

    DeployResult result = svc.deploy(YAML_BYTES, BPMN_BYTES, null);

    assertThat(result.isInvalid()).isTrue();
    assertThat(result.errors()).extracting(ErrorDetail::code).containsExactly("WKS-CFG-025");
    // AC2 — no new registration written (engine deploy gated registry writes)
    assertThat(registrar.registered).containsExactly(prior.id()); // only the pre-seed
    assertThat(registrar.removed).isEmpty(); // no rollback needed — nothing was written
  }

  @Test
  void engineFailureFirstDeployReturnsWksCfg025WithoutRegistration() {
    // Story 4.5 AC1/AC2 — no prior state; first deploy fails at engine step.
    // Registry must remain empty.
    CaseTypeConfig incoming = caseType();
    StubSource source = new StubSource(ValidationResult.ok(incoming));
    StubBpmn bpmn = new StubBpmn(BpmnValidationResult.ok("applicationProcess"));
    StubRegistrar registrar = new StubRegistrar();
    StubReader reader = new StubReader(); // no prior
    StubEngine engine = new StubEngine();
    engine.failNext = new WksWorkflowEngineException("engine down", new RuntimeException());

    ConfigService svc = newCfg(source, registrar, reader, bpmn, engine, new RecordingPublisher());

    DeployResult result = svc.deploy(YAML_BYTES, BPMN_BYTES, null);

    assertThat(result.isInvalid()).isTrue();
    assertThat(result.errors()).extracting(ErrorDetail::code).containsExactly("WKS-CFG-025");
    assertThat(registrar.registered).isEmpty();
    assertThat(registrar.removed).isEmpty();
  }

  // ---- folded debt #2: per-caseTypeId deploy serialization (Story 2.4 Task 7.2) ----------

  /**
   * Folded debt #2 — Story 2.4 review decision 4. The per-{@code caseTypeId} mutex in {@code
   * ConfigService.deploy} must serialize concurrent deploys of the same id so that the {@code
   * reader.find → registrar.register → workflowEngine.deploy} window is atomic. A deterministic
   * two-thread test (latch-coordinated, not stress) is the way to assert the lock actually holds —
   * a stress test would be flaky and architecturally redundant.
   */
  @Test
  void concurrentDeploysOfSameCaseTypeAreSerialized() throws Exception {
    StubSource source = new StubSource(ValidationResult.ok(caseType()));
    StubBpmn bpmn = new StubBpmn(BpmnValidationResult.ok("applicationProcess"));
    StubRegistrar registrar = new StubRegistrar();
    AtomicInteger concurrentInDeploy = new AtomicInteger(0);
    AtomicInteger maxObservedConcurrency = new AtomicInteger(0);
    CountDownLatch insideEngineDeploy = new CountDownLatch(1);
    CountDownLatch holdInEngineDeploy = new CountDownLatch(1);
    AtomicInteger deployCount = new AtomicInteger(0);

    WorkflowEngine engine =
        new WorkflowEngine() {
          @Override
          public DeploymentResult deploy(DeploymentRequest request) {
            int now = concurrentInDeploy.incrementAndGet();
            maxObservedConcurrency.updateAndGet(prev -> Math.max(prev, now));
            insideEngineDeploy.countDown();
            try {
              holdInEngineDeploy.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
            int n = deployCount.incrementAndGet();
            concurrentInDeploy.decrementAndGet();
            return new DeploymentResult(
                "deployment-" + n,
                request.processDefinitionKey(),
                "procDef-" + n,
                1,
                Instant.now());
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
          public java.util.List<com.wkspower.platform.domain.model.Task> findTasksByCase(
              java.util.UUID caseId) {
            return java.util.List.of();
          }

          @Override
          public String readActionLabel(String processDefinitionId, String taskDefinitionKey) {
            return null;
          }
        };

    ConfigService svc =
        newCfg(source, registrar, new StubReader(), bpmn, engine, new RecordingPublisher());

    ExecutorService pool = Executors.newFixedThreadPool(2);
    try {
      Future<DeployResult> a = pool.submit(() -> svc.deploy(YAML_BYTES, BPMN_BYTES, "thread-a"));
      // Wait until thread A is parked inside engine.deploy holding the per-id lock.
      assertThat(insideEngineDeploy.await(2, TimeUnit.SECONDS)).isTrue();
      Future<DeployResult> b = pool.submit(() -> svc.deploy(YAML_BYTES, BPMN_BYTES, "thread-b"));
      // Give B a chance to attempt to enter; if the lock is broken it would barge into
      // engine.deploy.
      Thread.sleep(100);
      assertThat(concurrentInDeploy.get())
          .as("Thread B must NOT have entered engine.deploy while A holds the per-caseTypeId lock")
          .isEqualTo(1);
      holdInEngineDeploy.countDown();
      a.get(2, TimeUnit.SECONDS);
      b.get(2, TimeUnit.SECONDS);
    } finally {
      pool.shutdown();
    }

    assertThat(maxObservedConcurrency.get())
        .as("Per-caseTypeId lock must serialize concurrent deploys (max concurrency = 1)")
        .isEqualTo(1);
    assertThat(deployCount.get()).isEqualTo(2);
  }

  // ---- helpers + stubs ---------------------------------------------------

  private static ValidationResult invalidYaml(String code, String msg) {
    return ValidationResult.invalid(List.of(err(code, msg)));
  }

  private static ErrorDetail err(String code, String msg) {
    return ErrorDetail.of(code, msg);
  }

  private static CaseTypeConfig caseType() {
    return new CaseTypeConfig(
        "application",
        "Application",
        1,
        null,
        new WorkflowRef("application.bpmn"),
        List.<FieldDefinition>of(),
        List.of(new StatusDefinition("open", "Open", StatusColor.BLUE)),
        List.of(),
        List.of(new RoleDefinition("admin", List.of())));
  }

  private static final class StubSource implements CaseTypeSource {
    private final ValidationResult bytesResult;

    StubSource(ValidationResult bytesResult) {
      this.bytesResult = bytesResult;
    }

    @Override
    public ValidationResult load(Path file) {
      return bytesResult;
    }

    @Override
    public ValidationResult loadBytes(
        String s, byte[] b, java.util.Map<String, byte[]> bpmnByName) {
      return bytesResult;
    }
  }

  private static final class StubBpmn implements BpmnValidationService {
    private final BpmnValidationResult result;

    StubBpmn(BpmnValidationResult result) {
      this.result = result;
    }

    @Override
    public BpmnValidationResult validate(byte[] bpmnXml, CaseTypeConfig caseType) {
      return result;
    }
  }

  private static final class StubRegistrar implements CaseTypeRegistrar {
    final List<String> registered = new ArrayList<>();
    final List<String> removed = new ArrayList<>();
    final Map<String, CaseTypeConfig> lastByConfig = new HashMap<>();

    // Mirror CaseTypeRegistry's version-monotonic semantics so the rollback path is exercised
    // honestly: registering an older version than the current one is rejected with WKS-CFG-011,
    // matching production behaviour. A test that bypasses this would mask the rollback bug
    // surfaced in code review (rollback bare-register(prior) → REJECTED → orphan registration).
    @Override
    public RegistrationResult register(CaseTypeConfig c) {
      CaseTypeConfig existing = lastByConfig.get(c.id());
      if (existing != null && c.version() < existing.version()) {
        return RegistrationResult.rejectedOlderVersion(
            ErrorDetail.ofField(
                "WKS-CFG-011",
                "Incoming version "
                    + c.version()
                    + " is older than registered version "
                    + existing.version(),
                "version"));
      }
      if (existing != null && c.version() == existing.version()) {
        return RegistrationResult.idempotent();
      }
      registered.add(c.id());
      lastByConfig.put(c.id(), c);
      return existing == null ? RegistrationResult.registered() : RegistrationResult.replaced();
    }

    @Override
    public void remove(String id) {
      removed.add(id);
      lastByConfig.remove(id);
    }

    CaseTypeConfig lastRegistered() {
      return lastByConfig.values().stream().findFirst().orElse(null);
    }
  }

  private static final class StubReader implements CaseTypeReader {
    final Map<String, CaseTypeConfig> preset = new HashMap<>();

    @Override
    public Optional<CaseTypeConfig> find(String id) {
      return Optional.ofNullable(preset.get(id));
    }

    @Override
    public Collection<CaseTypeConfig> all() {
      return preset.values();
    }

    @Override
    public Optional<CaseTypeConfig> findVersion(String id, int version) {
      return Optional.ofNullable(preset.get(id));
    }
  }

  private static final class StubEngine implements WorkflowEngine {
    final List<DeploymentRequest> deployed = new ArrayList<>();
    RuntimeException failNext;

    @Override
    public DeploymentResult deploy(DeploymentRequest request) {
      if (failNext != null) {
        RuntimeException ex = failNext;
        failNext = null;
        throw ex;
      }
      deployed.add(request);
      return new DeploymentResult(
          "deployment-" + deployed.size(),
          request.processDefinitionKey(),
          "procDef-" + deployed.size(),
          1,
          Instant.now());
    }

    @Override
    public Optional<DeploymentInfo> latestDeployment(String key) {
      return Optional.empty();
    }

    @Override
    public String startProcessInstance(
        String processDefinitionKey, java.util.Map<String, Object> variables) {
      throw new UnsupportedOperationException("not exercised by ConfigServiceDeployTest");
    }

    @Override
    public Optional<com.wkspower.platform.domain.model.Task> findTask(String taskId) {
      return Optional.empty();
    }

    @Override
    public void completeTask(String taskId, java.util.Map<String, Object> variables) {}

    @Override
    public void claimTask(String taskId, java.util.UUID userId) {}

    @Override
    public void signalTransition(
        String processInstanceId, String action, java.util.Map<String, Object> variables) {}

    @Override
    public java.util.List<com.wkspower.platform.domain.model.Task> findTasksByCase(
        java.util.UUID caseId) {
      return java.util.List.of();
    }

    @Override
    public String readActionLabel(String processDefinitionId, String taskDefinitionKey) {
      return null;
    }
  }

  private static final class RecordingPublisher implements EventPublisher {
    final List<Object> events = new ArrayList<>();

    @Override
    public void publish(Object event) {
      events.add(event);
    }
  }
}
