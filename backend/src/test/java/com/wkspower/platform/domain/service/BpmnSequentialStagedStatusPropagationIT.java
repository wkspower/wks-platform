package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.OutcomeMapping;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.port.AttachmentScope;
import com.wkspower.platform.domain.port.CaseInstanceRef;
import com.wkspower.platform.domain.port.CaseRepository;
import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.CaseTypeReader;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.ExecutionSignal;
import com.wkspower.platform.domain.port.ExecutionSignalKind;
import com.wkspower.platform.domain.port.FakeRecordingWorkflowAdapter;
import com.wkspower.platform.domain.port.StageRepository;
import com.wkspower.platform.infrastructure.persistence.RouterItPersistenceImports;
import com.wkspower.platform.infrastructure.persistence.entity.RoleEntity;
import com.wkspower.platform.infrastructure.persistence.entity.UserEntity;
import com.wkspower.platform.infrastructure.persistence.repository.CaseEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.RoleEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.UserEntityRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Story 6.2 AC7 — integration test for gap-10 fix (b): {@code resetStatusForAdvancedStage}
 * propagates the next stage's {@code initialStatus} into {@code case.status} on OUTCOME-dispatch
 * stage advance.
 *
 * <p>Also covers AC6 (gap-10 fix-a): case creation with a stage-scoped CaseType initialises {@code
 * case.status} from the first stage's {@code initialStatus} (NOT the ConfigValidator-injected
 * {@code "open"} default).
 *
 * <p>Uses H2 in-memory DB (DataJpaTest slice). Postgres-IT parity is provided by {@link
 * BpmnSequentialStagedStatusPropagationPostgresIT}. H2-only rationale for {@code OutcomeDispatchIT}
 * is documented separately.
 *
 * <p>Walk (mirrors JOURNEYS F4 steps 2/4/6/8):
 *
 * <ol>
 *   <li>Create case → assert {@code case.status = "drafting"} (AC6 — JOURNEYS F4 step 2).
 *   <li>Dispatch OUTCOME signal (approve) on {@code intake} stage → assert {@code case.status =
 *       "in-review"} (AC7 — JOURNEYS F4 step 4).
 *   <li>Dispatch OUTCOME signal (approve) on {@code review} stage → assert {@code case.status =
 *       "approved"} (AC7 — JOURNEYS F4 step 6).
 * </ol>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
  BpmnSequentialStagedStatusPropagationIT.TestConfig.class,
  RouterItPersistenceImports.class
})
@ActiveProfiles("dev")
@Transactional
public class BpmnSequentialStagedStatusPropagationIT {

  private static final Instant NOW = Instant.parse("2026-05-11T10:00:00Z");
  private static final String CASE_TYPE_ID = "bpmn-seq-staged-it";
  private static final String VERSION = "1";

  // Three stages mirroring bpmn-sequential-staged.yaml fixture
  public static final List<StageDefinition> STAGES =
      List.of(
          new StageDefinition(
              "intake",
              "Intake",
              0,
              List.of(new StatusDefinition("drafting", "Drafting", StatusColor.AMBER, false)),
              Optional.of("drafting")),
          new StageDefinition(
              "review",
              "Review",
              1,
              List.of(new StatusDefinition("in-review", "In Review", StatusColor.BLUE, false)),
              Optional.of("in-review")),
          new StageDefinition(
              "decision",
              "Decision",
              2,
              List.of(
                  new StatusDefinition("approved", "Approved", StatusColor.EMERALD, false),
                  new StatusDefinition("rejected", "Rejected", StatusColor.ZINC, true)),
              Optional.of("approved")));

  @Autowired ExecutionSignalRouter router;
  @Autowired MappingRegistry mappingRegistry;
  @Autowired WorkflowAdapterBinder binder;
  @Autowired CaseRepository caseRepository;
  @Autowired StageRepository stageRepository;
  @Autowired CaseStatusUpdater statusUpdater;
  @Autowired UserEntityRepository userRepo;
  @Autowired RoleEntityRepository roleRepo;
  @Autowired CaseEntityRepository caseEntityRepository;
  @Autowired EntityManager em;

  private UUID actorId;
  private FakeRecordingWorkflowAdapter fake;

  @BeforeEach
  void seedActorAndAdapter() {
    RoleEntity role =
        roleRepo
            .findByName("admin")
            .orElseGet(
                () ->
                    roleRepo.save(
                        new RoleEntity(UUID.randomUUID(), "admin", Instant.now(), Instant.now())));
    UserEntity u =
        userRepo.save(
            new UserEntity(
                UUID.randomUUID(),
                "seq-staged-it-" + UUID.randomUUID() + "@x",
                "x",
                true,
                Instant.now(),
                Instant.now(),
                new HashSet<>(List.of(role))));
    actorId = u.getId();
    fake = new FakeRecordingWorkflowAdapter(binder);
    fake.onExecutionSignal(router::onSignal);
  }

  /**
   * Full lifecycle walk asserting case.status tracks stage advance (gap-10 fix a + b).
   *
   * <p>AC6: After create, case.status = "drafting" (not "open").
   *
   * <p>AC7: After OUTCOME dispatch, case.status = next stage's initialStatus.
   */
  @Test
  void statusTracksStageInitialStatusThroughLifecycle() {
    // Register the multi-outcome mapping for the staged case type.
    CaseTypeRef caseTypeRef = new CaseTypeRef(CASE_TYPE_ID, VERSION);
    fake.attach(caseTypeRef, AttachmentScope.ofCase());
    MappingDefinition mapping =
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    CASE_TYPE_ID + ".bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.empty(),
                    Map.of(),
                    List.of(),
                    Map.of(
                        "approve", new OutcomeMapping("intake -> review"),
                        "reject", new OutcomeMapping("intake -> closed")))));
    mappingRegistry.register(caseTypeRef, VERSION, mapping);

    // --- AC6: create case with stage-scoped status type ---
    // The CaseService.initialStatus() fix-a reads stages[0].initialStatus = "drafting".
    UUID caseId = UUID.randomUUID();
    Case toSave =
        new Case(
            caseId,
            CASE_TYPE_ID,
            1,
            // gap-10 fix-a: use stage[0].initialStatus = "drafting" rather than "open"
            CaseService.initialStatus(buildCaseTypeConfig()),
            actorId,
            Map.of(),
            null,
            NOW,
            actorId,
            NOW,
            0L);
    caseRepository.save(toSave);
    stageRepository.materialiseStages(caseId, STAGES, NOW);
    stageRepository.appendTransition(
        new StageRepository.Transition(
            caseId, null, null, "intake", 0, List.of(), "wks-auto-rule", "case-create", NOW));
    em.flush();
    em.clear();

    // Assert AC6: initial status is "drafting" (gap-10 fix-a).
    Case created = caseRepository.findById(caseId).orElseThrow();
    assertThat(created.status())
        .as(
            "AC6 gap-10 fix-a: case.status should be 'drafting' from intake stage initialStatus,"
                + " not the ConfigValidator-injected 'open' default")
        .isEqualTo("drafting");
    assertThat(created.currentStageId()).isEqualTo("intake");

    // --- AC7: OUTCOME dispatch on intake stage — advance to review ---
    // Register updated mapping for review stage (approve routes intake -> review).
    MappingDefinition reviewMapping =
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    CASE_TYPE_ID + ".bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.empty(),
                    Map.of(),
                    List.of(),
                    Map.of("approve", new OutcomeMapping("intake -> review")))));
    mappingRegistry.register(caseTypeRef, VERSION, reviewMapping);

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.OUTCOME,
            "formOutcome",
            new CaseInstanceRef(caseId, caseTypeRef),
            "intake-triage",
            Map.of("outcome", "approve")));
    em.flush();
    em.clear();

    Case afterFirstAdvance = caseRepository.findById(caseId).orElseThrow();
    assertThat(afterFirstAdvance.currentStageId())
        .as("stage should advance from intake to review")
        .isEqualTo("review");
    assertThat(afterFirstAdvance.status())
        .as(
            "AC7 gap-10 fix-b: case.status should be 'in-review' from review stage initialStatus"
                + " after OUTCOME dispatch stage advance")
        .isEqualTo("in-review");

    // --- AC7 step 2: OUTCOME dispatch on review stage — advance to decision ---
    MappingDefinition decisionMapping =
        new MappingDefinition(
            List.of(
                new AttachmentDefinition(
                    "bpmn",
                    CASE_TYPE_ID + ".bpmn",
                    "case",
                    Optional.empty(),
                    Map.of(),
                    Optional.empty(),
                    Map.of(),
                    List.of(),
                    Map.of("approve", new OutcomeMapping("review -> decision")))));
    mappingRegistry.register(caseTypeRef, VERSION, decisionMapping);

    fake.emit(
        new ExecutionSignal(
            ExecutionSignalKind.OUTCOME,
            "formOutcome",
            new CaseInstanceRef(caseId, caseTypeRef),
            "review-assess",
            Map.of("outcome", "approve")));
    em.flush();
    em.clear();

    Case afterSecondAdvance = caseRepository.findById(caseId).orElseThrow();
    assertThat(afterSecondAdvance.currentStageId())
        .as("stage should advance from review to decision")
        .isEqualTo("decision");
    assertThat(afterSecondAdvance.status())
        .as(
            "AC7: case.status should be 'approved' from decision stage initialStatus (JOURNEYS F4 step 6)")
        .isEqualTo("approved");
  }

  /** Helper: build the CaseTypeConfig mirror of bpmn-sequential-staged for fix-a test. */
  public static CaseTypeConfig buildCaseTypeConfig() {
    return CaseTypeConfig.builder()
        .id(CASE_TYPE_ID)
        .displayName("BPMN Sequential Staged IT")
        .version(1)
        // Top-level statuses: ConfigValidator injects [open, closed] default when YAML omits them.
        .statuses(
            List.of(
                new StatusDefinition("open", "Open", StatusColor.ZINC, false),
                new StatusDefinition("closed", "Closed", StatusColor.ZINC, true)))
        .stages(STAGES)
        .build();
  }

  // ---- test config -------------------------------------------------------

  /**
   * Test configuration for BpmnSequentialStagedStatusPropagationIT. Provides the CaseTypeReader
   * stub with the bpmn-sequential-staged stage layout (intake → review → decision with distinct
   * initialStatus per stage). The CaseTypeReader is the key collaborator for
   * resetStatusForAdvancedStage's status-reset path.
   */
  /** Minimal event publisher stub for the test context — records nothing, discards events. */
  static class SimpleEventPublisher implements EventPublisher {
    final List<Object> published = new ArrayList<>();

    @Override
    public void publish(Object event) {
      published.add(event);
    }

    @Override
    public void publishAfterCommit(Object event) {
      published.add(event);
    }
  }

  static class TestConfig {

    @Bean
    NullAdapter nullAdapter() {
      return new NullAdapter();
    }

    @Bean
    WorkflowAdapterBinder binder(NullAdapter nullAdapter) {
      return new WorkflowAdapterBinder(nullAdapter);
    }

    @Bean
    MappingRegistry mappingRegistry() {
      return new MappingRegistry();
    }

    @Bean
    com.wkspower.platform.domain.port.Clock testClock() {
      return () -> NOW;
    }

    @Primary
    @Bean
    SimpleEventPublisher simpleEventPublisher() {
      return new SimpleEventPublisher();
    }

    @Bean
    WksStageAdvancer wksStageAdvancer(
        StageRepository stageRepository,
        EventPublisher eventPublisher,
        com.wkspower.platform.domain.port.Clock clock) {
      return new WksStageAdvancer(stageRepository, eventPublisher, clock);
    }

    /**
     * CaseTypeReader stub returning the bpmn-sequential-staged layout. This is the key bean for
     * gap-10 fix-b: resetStatusForAdvancedStage reads the next stage's initialStatus from this
     * reader to update case.status after a stage advance.
     */
    @Bean
    CaseTypeReader stubCaseTypeReader() {
      return new CaseTypeReader() {
        @Override
        public Optional<CaseTypeConfig> find(String id) {
          return Optional.of(buildCaseTypeConfig());
        }

        @Override
        public Collection<CaseTypeConfig> all() {
          return List.of(buildCaseTypeConfig());
        }

        @Override
        public Optional<CaseTypeConfig> findVersion(String id, int version) {
          return Optional.of(buildCaseTypeConfig());
        }
      };
    }

    @Bean
    ExecutionSignalRouter executionSignalRouter(
        MappingRegistry mappingRegistry,
        WksStageAdvancer wksStageAdvancer,
        com.wkspower.platform.domain.port.CaseStatusUpdater caseStatusUpdater,
        com.wkspower.platform.domain.port.CaseRepository caseRepository,
        EventPublisher eventPublisher,
        com.wkspower.platform.domain.port.Clock clock,
        CaseTypeReader caseTypeReader) {
      return new ExecutionSignalRouter(
          mappingRegistry,
          wksStageAdvancer,
          caseStatusUpdater,
          caseRepository,
          eventPublisher,
          clock,
          caseTypeReader);
    }
  }
}
