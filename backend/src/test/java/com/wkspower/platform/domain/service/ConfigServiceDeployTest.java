package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.junit.jupiter.api.Test;

/**
 * Pure-Java enumeration of {@code ConfigService.deploy(...)} branches. No Spring context, no
 * engine, no real BPMN — every dependency is a hand-rolled stub so the orchestration logic itself
 * is the unit under test.
 */
class ConfigServiceDeployTest {

  private static final byte[] YAML_BYTES = "id: application".getBytes();
  private static final byte[] BPMN_BYTES = "<bpmn/>".getBytes();

  // ---- four-cases enumeration --------------------------------------------

  @Test
  void yamlParseFailureProducesAggregate() {
    StubSource source = new StubSource(invalidYaml("WKS-CFG-099", "yaml broken"));
    StubBpmn bpmn = new StubBpmn(BpmnValidationResult.invalid(List.of(err("WKS-CFG-010", "bpmn"))));
    StubRegistrar registrar = new StubRegistrar();
    StubReader reader = new StubReader();
    StubEngine engine = new StubEngine();
    RecordingPublisher publisher = new RecordingPublisher();
    ConfigService svc = new ConfigService(source, registrar, reader, bpmn, engine, publisher);

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
        new ConfigService(
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
        new ConfigService(
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
        new ConfigService(
            source, registrar, new StubReader(), bpmn, engine, new RecordingPublisher());

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
    ConfigService svc =
        new ConfigService(source, registrar, new StubReader(), bpmn, engine, publisher);

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

  // ---- engine failure rollback ------------------------------------------

  @Test
  void engineFailureRollsBackToPriorState() {
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

    ConfigService svc =
        new ConfigService(source, registrar, reader, bpmn, engine, new RecordingPublisher());

    assertThatThrownBy(() -> svc.deploy(YAML_BYTES, BPMN_BYTES, null))
        .isInstanceOf(WksWorkflowEngineException.class);
    // Prior registration restored — last register() call put `prior` back after the failure.
    assertThat(registrar.lastRegistered()).isEqualTo(prior);
  }

  @Test
  void engineFailureWithoutPriorStateRemovesRegistration() {
    CaseTypeConfig incoming = caseType();
    StubSource source = new StubSource(ValidationResult.ok(incoming));
    StubBpmn bpmn = new StubBpmn(BpmnValidationResult.ok("applicationProcess"));
    StubRegistrar registrar = new StubRegistrar();
    StubReader reader = new StubReader(); // no prior — find returns empty
    StubEngine engine = new StubEngine();
    engine.failNext = new WksWorkflowEngineException("engine down", new RuntimeException());

    ConfigService svc =
        new ConfigService(source, registrar, reader, bpmn, engine, new RecordingPublisher());

    assertThatThrownBy(() -> svc.deploy(YAML_BYTES, BPMN_BYTES, null))
        .isInstanceOf(WksWorkflowEngineException.class);
    assertThat(registrar.removed).contains(incoming.id());
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
    public ValidationResult loadBytes(String s, byte[] b) {
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
  }

  private static final class RecordingPublisher implements EventPublisher {
    final List<Object> events = new ArrayList<>();

    @Override
    public void publish(Object event) {
      events.add(event);
    }
  }
}
