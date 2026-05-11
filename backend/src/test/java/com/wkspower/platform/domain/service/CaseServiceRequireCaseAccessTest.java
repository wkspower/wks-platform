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
import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.CaseQuery;
import com.wkspower.platform.domain.model.CaseSummary;
import com.wkspower.platform.domain.page.Page;
import com.wkspower.platform.domain.page.PageRequest;
import com.wkspower.platform.domain.port.CaseDataValidator;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.CaseTypeReader;
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
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Story 5.6 AC-0 — unit coverage for {@link CaseService#requireCaseAccess(UUID, UUID, Set)}. Sprint
 * 8 retro Action 2 — single source of truth for case-level access on write-side controllers.
 */
class CaseServiceRequireCaseAccessTest {

  private static final UUID ACTOR = UUID.randomUUID();
  private static final Instant FIXED = Instant.parse("2026-05-09T10:00:00Z");

  @Test
  void caseFoundReturnsLoadedCase() {
    CaseTypeConfig config = caseType();
    SimpleRepo repo = new SimpleRepo();
    UUID caseId = UUID.randomUUID();
    Case existing =
        new Case(caseId, config.id(), 1, "open", null, Map.of(), null, FIXED, ACTOR, FIXED, 0L);
    repo.save(existing);
    CaseService svc = svc(repo, config);

    Case loaded = svc.requireCaseAccess(caseId, ACTOR, Set.of("admin"));

    assertThat(loaded.id()).isEqualTo(caseId);
    assertThat(loaded.caseTypeId()).isEqualTo(config.id());
  }

  @Test
  void caseAbsentThrowsNotFound() {
    CaseTypeConfig config = caseType();
    SimpleRepo repo = new SimpleRepo(); // empty
    UUID caseId = UUID.randomUUID();
    CaseService svc = svc(repo, config);

    assertThatThrownBy(() -> svc.requireCaseAccess(caseId, ACTOR, Set.of()))
        .isInstanceOf(WksNotFoundException.class)
        .hasMessageContaining(caseId.toString())
        .hasMessageContaining("not found");
  }

  // ---- helpers ---------------------------------------------------------------

  private static CaseTypeConfig caseType() {
    return new CaseTypeConfig(
        "loan-application",
        "Loan Application",
        1,
        null,
        null,
        List.of(new FieldDefinition("name", "Name", FieldType.TEXT, true, 0, List.of(), null)),
        List.of(new StatusDefinition("open", "Open", StatusColor.ZINC)),
        List.of("name"),
        List.of(new RoleDefinition("admin", List.of(Permission.CREATE))),
        List.of(),
        List.of());
  }

  private static CaseService svc(CaseRepository repo, CaseTypeConfig config) {
    WksStageAdvancer advancer = new WksStageAdvancer(new NullStageRepo(), ev -> {}, () -> FIXED);
    com.wkspower.platform.testsupport.FakeCaseTypeVersionRegistry registry =
        new com.wkspower.platform.testsupport.FakeCaseTypeVersionRegistry();
    registry.seed(config.id(), 1, "id: x".getBytes());
    return new CaseService(
        repo,
        reader(config),
        new NullValidator(),
        new NullEngine(),
        new NullResolver(),
        ev -> {}, // EventPublisher
        () -> FIXED,
        advancer,
        registry,
        signal -> {}, // ExecutionSignalHandler
        new CaseStatusUpdater() {
          @Override
          public Optional<String> updateStatus(UUID caseId, String newStatus) {
            return Optional.empty();
          }
        });
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

  private static final class SimpleRepo implements CaseRepository {
    private final List<Case> saved = new ArrayList<>();

    @Override
    public Case save(Case c) {
      saved.removeIf(x -> x.id().equals(c.id()));
      saved.add(c);
      return c;
    }

    @Override
    public Optional<Case> findById(UUID id) {
      return saved.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    @Override
    public Page<CaseSummary> findByCaseType(CaseQuery q, PageRequest pr) {
      return new Page<>(List.of(), 0, pr.page(), pr.size());
    }

    @Override
    public Map<UUID, Map<String, Object>> findDataByIds(
        Collection<UUID> ids, Set<String> fieldIds) {
      return Map.of();
    }

    @Override
    public int updateCaseTypeVersion(UUID caseId, int toCaseTypeVersion, long expectedVersion) {
      return 0;
    }
  }

  private static final class NullValidator implements CaseDataValidator {
    @Override
    public List<ErrorDetail> validate(CaseTypeConfig caseType, Map<String, Object> data) {
      return List.of();
    }
  }

  private static final class NullEngine implements WorkflowEngine {
    @Override
    public DeploymentResult deploy(DeploymentRequest req) {
      return new DeploymentResult("d", req.processDefinitionKey(), "p", 1, FIXED);
    }

    @Override
    public Optional<DeploymentInfo> latestDeployment(String key) {
      return Optional.empty();
    }

    @Override
    public String startProcessInstance(String key, Map<String, Object> vars) {
      return "pi-1";
    }

    @Override
    public Optional<com.wkspower.platform.domain.model.Task> findTask(String taskId) {
      return Optional.empty();
    }

    @Override
    public void completeTask(String taskId, Map<String, Object> vars) {}

    @Override
    public void claimTask(String taskId, UUID userId) {}

    @Override
    public void signalTransition(String pi, String action, Map<String, Object> vars) {}

    @Override
    public List<com.wkspower.platform.domain.model.Task> findTasksByCase(UUID caseId) {
      return List.of();
    }

    @Override
    public String readActionLabel(String processDefinitionId, String taskDefinitionKey) {
      return null;
    }
  }

  private static final class NullResolver implements ProcessDefinitionKeyResolver {
    @Override
    public Optional<String> resolve(String caseTypeId) {
      return Optional.empty();
    }
  }

  private static final class NullStageRepo implements StageRepository {
    @Override
    public List<com.wkspower.platform.domain.model.Stage> loadHistory(UUID caseId) {
      return List.of();
    }

    @Override
    public void materialiseStages(UUID caseId, List<StageDefinition> stages, Instant createdAt) {}

    @Override
    public void appendTransition(Transition transition) {}
  }
}
