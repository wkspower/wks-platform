package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.event.CaseCreated;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseQuery;
import com.wkspower.platform.domain.model.CaseSummary;
import com.wkspower.platform.domain.model.Stage;
import com.wkspower.platform.domain.page.Page;
import com.wkspower.platform.domain.page.PageRequest;
import com.wkspower.platform.domain.port.ExecutionSignal;
import com.wkspower.platform.domain.port.ExecutionSignalHandler;
import com.wkspower.platform.domain.port.CaseDataValidator;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.ProcessDefinitionKeyResolver;
import com.wkspower.platform.domain.port.StageRepository;
import com.wkspower.platform.domain.port.WorkflowEngine;
import com.wkspower.platform.domain.workflow.DeploymentInfo;
import com.wkspower.platform.domain.workflow.DeploymentRequest;
import com.wkspower.platform.domain.workflow.DeploymentResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Story 3.2 — headline-matrix unit tests for the zero-stage zero-process CaseType. Pure-Java stubs
 * (mirrors {@link CaseServiceTest}'s no-Spring no-DB enumeration) so a regression here is easy to
 * triage without booting an integration slice.
 *
 * <p>Covers AC11 scenarios 1, 4, 5 (downshifted), 6, 8. Scenarios 2, 3 are exercised in {@code
 * ConfigValidatorTest#story32_*}. Scenario 7 (comment / attachment / ad-hoc task surfaces) lives in
 * the Story 2.x controller-IT base; this class does not duplicate that surface.
 */
class ZeroStageZeroProcessTest {

  private static final UUID ACTOR = UUID.randomUUID();
  private static final Instant FIXED = Instant.parse("2026-05-05T10:00:00Z");

  private final RecordingRepo repo = new RecordingRepo();
  private final NoopValidator validator = new NoopValidator();
  private final TrackingEngine engine = new TrackingEngine();
  private final TrackingResolver resolver = new TrackingResolver();
  private final RecordingPublisher publisher = new RecordingPublisher();
  // Story 4.4b AC1 / AC2 — zero-process path uses direct status updater, not router.
  private final RecordingStatusUpdater statusUpdater = new RecordingStatusUpdater(repo);
  private final NoopSignalHandler signalHandler = new NoopSignalHandler();

  private CaseService svc(CaseTypeConfig config) {
    WksStageAdvancer advancer =
        new WksStageAdvancer(new NoopStageRepository(), publisher, () -> FIXED);
    com.wkspower.platform.testsupport.FakeCaseTypeVersionRegistry registry =
        new com.wkspower.platform.testsupport.FakeCaseTypeVersionRegistry();
    registry.seed(
        config.id(),
        config.version() == 0 ? 1 : config.version(),
        ("id: " + config.id()).getBytes());
    return new CaseService(
        repo,
        reader(config),
        validator,
        engine,
        resolver,
        publisher,
        () -> FIXED,
        advancer,
        registry,
        signalHandler,
        statusUpdater);
  }

  // ---- AC11 §4 — create on zero-zero CaseType ----

  @Test
  void story32_createOnZeroProcess_skipsEngineCallAndPersistsNullProcessInstanceId() {
    CaseService svc = svc(zeroZeroType());

    Case created = svc.create("zero-zero", Map.of(), null, ACTOR);

    assertThat(created.processInstanceId())
        .as("zero-process create must NOT invoke the engine")
        .isNull();
    assertThat(created.currentStageId()).isNull();
    assertThat(created.currentStageOrdinal()).isNull();
    assertThat(created.status()).isEqualTo("open");
    assertThat(engine.startCalls)
        .as("engine.startProcessInstance must NOT be invoked for zero-process CaseTypes")
        .isZero();
    assertThat(resolver.calls)
        .as("processKeyResolver.resolve must NOT be invoked for zero-process CaseTypes")
        .isZero();
    assertThat(publisher.events).hasSize(1);
    assertThat(publisher.events.get(0)).isInstanceOf(CaseCreated.class);
  }

  @Test
  void story32_createOnWorkflowAttached_stillInvokesEngine() {
    CaseService svc = svc(workflowAttachedType());

    Case created = svc.create("loan-application", Map.of("name", "Asha"), null, ACTOR);

    assertThat(created.processInstanceId()).isEqualTo("pi-1");
    assertThat(engine.startCalls).isEqualTo(1);
    assertThat(resolver.calls).isEqualTo(1);
  }

  // ---- AC11 §5 — FLIPPED (Story 4.4b AC2): zero-process transition succeeds via pure-domain path

  @Test
  void story32_transitionOnZeroProcessCase_succeedsViaPureDomainPath() {
    // Story 4.4b AC2 — replaces the engine-coupling error expectation locked in Story 3.2 Q5.
    // CaseService.transition() now bypasses the router for zero-process cases and calls
    // CaseStatusUpdater directly. The case must reflect the new status after transition.
    CaseService svc = svc(zeroZeroType());
    Case created = svc.create("zero-zero", Map.of(), null, ACTOR);
    assertThat(created.status()).isEqualTo("open");
    assertThat(created.processInstanceId())
        .as("zero-process case must have no processInstanceId")
        .isNull();

    // Action must be a declared status id on the zero-process path (I1 guard). "closed" is
    // declared in zeroZeroType() — this is the correct declared status to transition to.
    Case afterTransition = svc.transition(created.id(), "closed", Map.of(), ACTOR);

    assertThat(afterTransition.status())
        .as("status must be updated to 'closed' via pure-domain path")
        .isEqualTo("closed");
    assertThat(engine.startCalls)
        .as("engine must NOT be invoked on zero-process transition")
        .isZero();
    assertThat(signalHandler.signals)
        .as("router must NOT be called on zero-process transition")
        .isEmpty();
  }

  // ---- AC11 §1 — repeated parse-time confirmation: workflowOpt empty ----

  @Test
  void story32_zeroZeroCaseTypeConfig_workflowOptIsEmpty() {
    CaseTypeConfig zz = zeroZeroType();
    assertThat(zz.workflowOpt()).isEmpty();
    assertThat(zz.stages()).isEmpty();
    assertThat(zz.statuses()).hasSize(2);
    assertThat(zz.statuses().get(0).id()).isEqualTo("open");
    assertThat(zz.statuses().get(1).id()).isEqualTo("closed");
  }

  // ---- AC11 §8 — coexistence with workflow-attached CaseType ----

  @Test
  void story32_zeroZeroAndWorkflowAttachedCoexist() {
    CaseService zzSvc = svc(zeroZeroType());
    CaseService waSvc = svc(workflowAttachedType());

    Case zz = zzSvc.create("zero-zero", Map.of(), null, ACTOR);
    engine.reset();
    resolver.reset();
    Case wa = waSvc.create("loan-application", Map.of("name", "Asha"), null, ACTOR);

    assertThat(zz.processInstanceId()).isNull();
    assertThat(wa.processInstanceId()).isEqualTo("pi-1");
    assertThat(engine.startCalls).isEqualTo(1);
    assertThat(resolver.calls).isEqualTo(1);
  }

  // ---- helpers ----

  private static CaseTypeConfig zeroZeroType() {
    // Story 3.2 — smallest valid CaseType: no stages, no workflow, default statuses.
    // Story 4.4b AC5 — closed.terminal flips to true to match DEFAULT_STATUSES contract.
    return new CaseTypeConfig(
        "zero-zero",
        "Zero Zero",
        1,
        null,
        null, // workflow omitted
        List.of(), // fields empty (legal additive default)
        List.of(
            new StatusDefinition("open", "Open", StatusColor.BLUE, false),
            new StatusDefinition("closed", "Closed", StatusColor.ZINC, true)),
        List.of(),
        List.of(new RoleDefinition("admin", List.of(Permission.VIEW, Permission.CREATE))),
        List.of()); // stages empty
  }

  private static CaseTypeConfig workflowAttachedType() {
    return new CaseTypeConfig(
        "loan-application",
        "Loan Application",
        1,
        null,
        new com.wkspower.platform.domain.config.model.WorkflowRef("loan-application.bpmn"),
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(new RoleDefinition("officer", List.of(Permission.VIEW, Permission.CREATE))));
  }

  private static CaseTypeReader reader(CaseTypeConfig config) {
    return new CaseTypeReader() {
      @Override
      public Optional<CaseTypeConfig> find(String id) {
        return id.equals(config.id()) ? Optional.of(config) : Optional.empty();
      }

      @Override
      public Collection<CaseTypeConfig> all() {
        return List.of(config);
      }

      @Override
      public Optional<CaseTypeConfig> findVersion(String id, int version) {
        return id.equals(config.id()) ? Optional.of(config) : Optional.empty();
      }
    };
  }

  private static final class RecordingRepo implements CaseRepository {
    final List<Case> saved = new ArrayList<>();

    @Override
    public Case save(Case caseToSave) {
      saved.removeIf(c -> c.id().equals(caseToSave.id()));
      saved.add(caseToSave);
      return caseToSave;
    }

    @Override
    public Optional<Case> findById(UUID id) {
      return saved.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    @Override
    public Page<CaseSummary> findByCaseType(CaseQuery query, PageRequest pageRequest) {
      return new Page<>(List.of(), 0, pageRequest.page(), pageRequest.size());
    }

    @Override
    public Map<UUID, Map<String, Object>> findDataByIds(
        Collection<UUID> ids, java.util.Set<String> projectedFieldIds) {
      return Map.of();
    }
  }

  private static final class NoopValidator implements CaseDataValidator {
    @Override
    public List<ErrorDetail> validate(CaseTypeConfig caseType, Map<String, Object> data) {
      return List.of();
    }
  }

  private static final class TrackingEngine implements WorkflowEngine {
    int startCalls = 0;

    void reset() {
      startCalls = 0;
    }

    @Override
    public DeploymentResult deploy(DeploymentRequest request) {
      return new DeploymentResult("d", request.processDefinitionKey(), "p", 1, FIXED);
    }

    @Override
    public Optional<DeploymentInfo> latestDeployment(String key) {
      return Optional.empty();
    }

    @Override
    public String startProcessInstance(String key, Map<String, Object> variables) {
      startCalls++;
      return "pi-1";
    }

    @Override
    public Optional<com.wkspower.platform.domain.model.Task> findTask(String taskId) {
      return Optional.empty();
    }

    @Override
    public void completeTask(String taskId, Map<String, Object> variables) {}

    @Override
    public void claimTask(String taskId, UUID userId) {}

    @Override
    public void signalTransition(
        String processInstanceId, String action, Map<String, Object> variables) {}

    @Override
    public List<com.wkspower.platform.domain.model.Task> findTasksByCase(UUID caseId) {
      return List.of();
    }

    @Override
    public String readActionLabel(String processDefinitionId, String taskDefinitionKey) {
      return null;
    }
  }

  private static final class TrackingResolver implements ProcessDefinitionKeyResolver {
    int calls = 0;

    void reset() {
      calls = 0;
    }

    @Override
    public Optional<String> resolve(String caseTypeId) {
      calls++;
      return Optional.of("processKey-" + caseTypeId);
    }
  }

  private static final class RecordingPublisher implements EventPublisher {
    final List<Object> events = new ArrayList<>();

    @Override
    public void publish(Object event) {
      events.add(event);
    }
  }

  private static final class NoopStageRepository implements StageRepository {
    @Override
    public List<Stage> loadHistory(UUID caseId) {
      return List.of();
    }

    @Override
    public void materialiseStages(
        UUID caseId, List<StageDefinition> stages, java.time.Instant createdAt) {}

    @Override
    public void appendTransition(Transition transition) {}
  }

  /**
   * Story 4.4b AC1/AC2 — RecordingStatusUpdater: applies status update directly to the in-memory
   * RecordingRepo so CaseService.findById returns the updated status after transition.
   */
  private static final class RecordingStatusUpdater implements CaseStatusUpdater {
    private final RecordingRepo repo;

    RecordingStatusUpdater(RecordingRepo repo) {
      this.repo = repo;
    }

    @Override
    public Optional<String> updateStatus(UUID caseId, String newStatus) {
      return repo.findById(caseId)
          .map(
              existing -> {
                String prev = existing.status();
                Case updated =
                    new Case(
                        existing.id(),
                        existing.caseTypeId(),
                        existing.caseTypeVersion(),
                        newStatus,
                        existing.assignee(),
                        existing.data(),
                        existing.processInstanceId(),
                        existing.createdAt(),
                        existing.createdBy(),
                        existing.updatedAt(),
                        existing.version());
                repo.save(updated);
                return prev;
              });
    }
  }

  /**
   * Story 4.4b AC1/AC2 — NoopSignalHandler: records signal calls so assertions can verify the
   * router is NOT invoked for zero-process transitions.
   */
  private static final class NoopSignalHandler implements ExecutionSignalHandler {
    final List<ExecutionSignal> signals = new ArrayList<>();

    @Override
    public void onSignal(ExecutionSignal signal) {
      signals.add(signal);
    }
  }
}
