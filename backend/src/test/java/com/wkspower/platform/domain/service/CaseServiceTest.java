package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.FieldDefinition;
import com.wkspower.platform.domain.config.model.FieldType;
import com.wkspower.platform.domain.config.model.Permission;
import com.wkspower.platform.domain.config.model.RoleDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.config.model.WorkflowRef;
import com.wkspower.platform.domain.event.CaseCreated;
import com.wkspower.platform.domain.event.CaseUpdated;
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.exception.WksValidationAggregateException;
import com.wkspower.platform.domain.exception.WksWorkflowEngineException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseQuery;
import com.wkspower.platform.domain.model.CaseSummary;
import com.wkspower.platform.domain.page.Page;
import com.wkspower.platform.domain.page.PageRequest;
import com.wkspower.platform.domain.port.CaseDataValidator;
import com.wkspower.platform.domain.port.CaseRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pure-Java enumeration of {@code CaseService} branches. No Spring context, no engine, no DB —
 * every collaborator is a hand-rolled stub.
 */
class CaseServiceTest {

  private static final UUID ACTOR = UUID.randomUUID();
  private static final Instant FIXED = Instant.parse("2026-04-26T10:00:00Z");

  private StubRepo repo;
  private StubValidator validator;
  private StubEngine engine;
  private StubPublisher publisher;
  private StubResolver resolver;

  @BeforeEach
  void resetStubs() {
    repo = new StubRepo();
    validator = new StubValidator();
    engine = new StubEngine("pi-1", null);
    publisher = new StubPublisher();
    resolver = new StubResolver("applicationProcess");
  }

  private CaseService svc(CaseTypeConfig config) {
    WksStageAdvancer advancer =
        new WksStageAdvancer(new NoopStageRepository(), publisher, () -> FIXED);
    com.wkspower.platform.testsupport.FakeCaseTypeVersionRegistry registry =
        new com.wkspower.platform.testsupport.FakeCaseTypeVersionRegistry();
    // Seed a v1 row so CaseService.create's registry bind succeeds for the fixture id.
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
        registry);
  }

  @Test
  void createHappyPathPersistsAndPublishesCaseCreated() {
    CaseService svc = svc(loanType());

    Case created = svc.create("loan-application", Map.of("name", "Asha"), null, ACTOR);

    assertThat(created.caseTypeId()).isEqualTo("loan-application");
    assertThat(created.caseTypeVersion()).isEqualTo(1);
    assertThat(created.status()).isEqualTo("open");
    assertThat(created.processInstanceId()).isEqualTo("pi-1");
    assertThat(created.data()).containsEntry("name", "Asha");
    assertThat(repo.saved).hasSize(1);

    assertThat(publisher.events).hasSize(1);
    CaseCreated event = (CaseCreated) publisher.events.get(0);
    assertThat(event.caseId()).isEqualTo(created.id());
    assertThat(event.actorId()).isEqualTo(ACTOR);
  }

  @Test
  void createWithUnknownCaseTypeThrows404() {
    CaseService svc = svc(loanType());

    assertThatThrownBy(() -> svc.create("no-such-type", Map.of(), null, ACTOR))
        .isInstanceOf(WksNotFoundException.class);
  }

  @Test
  void createWithFailingValidationAggregatesErrors() {
    validator.queue(
        List.of(
            ErrorDetail.ofField("WKS-API-001", "must not be blank", "name"),
            ErrorDetail.ofField("WKS-API-001", "too long", "notes")));
    CaseService svc = svc(loanType());

    assertThatThrownBy(() -> svc.create("loan-application", Map.of(), null, ACTOR))
        .isInstanceOf(WksValidationAggregateException.class)
        .satisfies(
            ex -> {
              WksValidationAggregateException agg = (WksValidationAggregateException) ex;
              assertThat(agg.getErrors()).hasSize(2);
            });
  }

  @Test
  void createWhenProcessKeyUnknownThrowsEngineException() {
    resolver = new StubResolver(null);
    CaseService svc = svc(loanType());

    assertThatThrownBy(() -> svc.create("loan-application", Map.of("name", "x"), null, ACTOR))
        .isInstanceOf(WksWorkflowEngineException.class);
  }

  @Test
  void createWhenEngineFailsPropagates() {
    engine = new StubEngine(null, new WksWorkflowEngineException("engine down"));
    CaseService svc = svc(loanType());

    assertThatThrownBy(() -> svc.create("loan-application", Map.of("name", "x"), null, ACTOR))
        .isInstanceOf(WksWorkflowEngineException.class);
  }

  @Test
  void updateHappyPathBumpsVersionAndPublishesCaseUpdated() {
    CaseTypeConfig loan = loanType();
    CaseService svc = svc(loan);
    Case seeded = svc.create("loan-application", Map.of("name", "Asha"), null, ACTOR);
    publisher.events.clear();

    Case updated =
        svc.update(seeded.id(), Map.of("name", "Asha", "amount", 250000), seeded.version(), ACTOR);

    assertThat(updated.data()).containsEntry("amount", 250000);
    assertThat(updated.version()).isGreaterThan(seeded.version());
    assertThat(publisher.events).hasSize(1);
    CaseUpdated event = (CaseUpdated) publisher.events.get(0);
    assertThat(event.changedFieldIds()).contains("amount");
  }

  @Test
  void updateWithVersionMismatchThrowsConflict() {
    CaseService svc = svc(loanType());
    Case seeded = svc.create("loan-application", Map.of("name", "Asha"), null, ACTOR);

    assertThatThrownBy(
            () -> svc.update(seeded.id(), Map.of("name", "Bob"), seeded.version() + 99, ACTOR))
        .isInstanceOf(WksConflictException.class);
  }

  @Test
  void updateWhenCaseMissingThrows404() {
    CaseService svc = svc(loanType());

    assertThatThrownBy(() -> svc.update(UUID.randomUUID(), Map.of(), 0L, ACTOR))
        .isInstanceOf(WksNotFoundException.class);
  }

  @Test
  void updateWithFailingValidationAggregates() {
    CaseService svc = svc(loanType());
    Case seeded = svc.create("loan-application", Map.of("name", "Asha"), null, ACTOR);

    validator.queue(List.of(ErrorDetail.ofField("WKS-API-001", "bad value", "name")));

    assertThatThrownBy(() -> svc.update(seeded.id(), Map.of("name", ""), seeded.version(), ACTOR))
        .isInstanceOf(WksValidationAggregateException.class);
  }

  @Test
  void diffFieldIdsHandlesAddedRemovedAndChanged() {
    Map<String, Object> oldMap = Map.of("a", 1, "b", "two");
    Map<String, Object> newMap = Map.of("a", 1, "b", "TWO", "c", true);

    var changed = CaseService.diffFieldIds(oldMap, newMap);

    assertThat(changed).contains("b", "c");
    assertThat(changed).doesNotContain("a");
  }

  // ---- helpers -----------------------------------------------------------

  private static CaseTypeConfig loanType() {
    return new CaseTypeConfig(
        "loan-application",
        "Loan Application",
        1,
        null,
        new WorkflowRef("loan-application.bpmn"),
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

  private static final class StubRepo implements CaseRepository {
    final List<Case> saved = new ArrayList<>();

    @Override
    public Case save(Case caseToSave) {
      saved.removeIf(c -> c.id().equals(caseToSave.id()));
      Case bumped =
          new Case(
              caseToSave.id(),
              caseToSave.caseTypeId(),
              caseToSave.caseTypeVersion(),
              caseToSave.status(),
              caseToSave.assignee(),
              caseToSave.data(),
              caseToSave.processInstanceId(),
              caseToSave.createdAt(),
              caseToSave.createdBy(),
              caseToSave.updatedAt(),
              caseToSave.version() + 1);
      saved.add(bumped);
      return bumped;
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
        java.util.Collection<UUID> ids, java.util.Set<String> projectedFieldIds) {
      return Map.of();
    }
  }

  private static final class StubValidator implements CaseDataValidator {
    private List<ErrorDetail> next = List.of();

    void queue(List<ErrorDetail> errors) {
      this.next = errors;
    }

    @Override
    public List<ErrorDetail> validate(CaseTypeConfig caseType, Map<String, Object> data) {
      List<ErrorDetail> r = next;
      next = List.of();
      return r;
    }
  }

  private static final class StubEngine implements WorkflowEngine {
    private final String returnedPi;
    private final RuntimeException failure;

    StubEngine(String returnedPi, RuntimeException failure) {
      this.returnedPi = returnedPi;
      this.failure = failure;
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
      if (failure != null) throw failure;
      return returnedPi;
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

  private static final class StubPublisher implements EventPublisher {
    final List<Object> events = new ArrayList<>();

    @Override
    public void publish(Object event) {
      events.add(event);
    }
  }

  /**
   * Stage repo stub — never persists, returns empty history. CaseService stubs use zero-stage
   * CaseTypes.
   */
  private static final class NoopStageRepository implements StageRepository {
    @Override
    public List<com.wkspower.platform.domain.model.Stage> loadHistory(UUID caseId) {
      return List.of();
    }

    @Override
    public void materialiseStages(
        UUID caseId, List<StageDefinition> stages, java.time.Instant createdAt) {}

    @Override
    public void appendTransition(Transition transition) {}
  }

  private static final class StubResolver implements ProcessDefinitionKeyResolver {
    private final String key;

    StubResolver(String key) {
      this.key = key;
    }

    @Override
    public Optional<String> resolve(String caseTypeId) {
      return Optional.ofNullable(key);
    }
  }
}
