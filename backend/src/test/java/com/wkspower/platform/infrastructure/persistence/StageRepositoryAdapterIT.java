package com.wkspower.platform.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.exception.WksStageException;
import com.wkspower.platform.domain.model.Case;
import com.wkspower.platform.domain.model.Stage;
import com.wkspower.platform.domain.model.StageState;
import com.wkspower.platform.domain.port.StageRepository;
import com.wkspower.platform.infrastructure.persistence.entity.RoleEntity;
import com.wkspower.platform.infrastructure.persistence.entity.UserEntity;
import com.wkspower.platform.infrastructure.persistence.repository.CaseEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.CaseStageHistoryJpaRepository;
import com.wkspower.platform.infrastructure.persistence.repository.RoleEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.UserEntityRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * {@code @DataJpaTest} IT for {@link StageRepositoryAdapter} (Story 3.1 AC3, AC5, AC6, AC8). Uses
 * the H2 dev database the existing CaseRepository IT runs against. Postgres-IT parity is a Phase-1
 * follow-up — the migration is in {@code db/migration/common/} so both databases will run the same
 * SQL once the dual-IT lane is wired.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({StageRepositoryAdapter.class, CaseRepositoryAdapter.class})
@ActiveProfiles("dev")
class StageRepositoryAdapterIT {

  @Autowired StageRepositoryAdapter adapter;
  @Autowired CaseRepositoryAdapter caseAdapter;
  @Autowired CaseStageHistoryJpaRepository historyRepo;
  @Autowired CaseEntityRepository caseRepo;
  @Autowired UserEntityRepository userRepo;
  @Autowired RoleEntityRepository roleRepo;
  @Autowired EntityManager em;

  private UUID actorId;
  private static final Instant NOW = Instant.parse("2026-05-05T12:00:00Z");

  private static final List<StageDefinition> THREE =
      List.of(
          new StageDefinition("intake", "Intake", 0),
          new StageDefinition("review", "Review", 1),
          new StageDefinition("decision", "Decision", 2));

  @BeforeEach
  void seedUser() {
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
                "stage-it-" + UUID.randomUUID() + "@x",
                "x",
                true,
                Instant.now(),
                Instant.now(),
                new HashSet<>(List.of(role))));
    actorId = u.getId();
  }

  @Test
  void materialiseAndBootstrapTransitionFlipsFirstToActive() {
    UUID caseId = newCase();

    adapter.materialiseStages(caseId, THREE, NOW);
    flushClear();

    List<Stage> hist = adapter.loadHistory(caseId);
    assertThat(hist).hasSize(3);
    assertThat(hist).allMatch(s -> s.state() == StageState.PENDING);

    adapter.appendTransition(
        new StageRepository.Transition(
            caseId, null, null, "intake", 0, List.of(), "wks-auto-rule", "case-create", NOW));
    flushClear();

    List<Stage> after = adapter.loadHistory(caseId);
    assertThat(after.get(0).state()).isEqualTo(StageState.ACTIVE);
    assertThat(after.get(0).enteredAt()).isEqualTo(NOW);
    assertThat(after.get(1).state()).isEqualTo(StageState.PENDING);
    assertThat(after.get(2).state()).isEqualTo(StageState.PENDING);

    // current_stage_id cache populated.
    var cached = caseRepo.findById(caseId).orElseThrow();
    assertThat(cached.getCurrentStageId()).isEqualTo("intake");
    assertThat(cached.getCurrentStageOrdinal()).isZero();
  }

  @Test
  void linearAdvanceCompletesPriorActivatesNext() {
    UUID caseId = bootstrapped();

    adapter.appendTransition(
        new StageRepository.Transition(
            caseId, "intake", 0, "review", 1, List.of(), "manual", "u1", NOW.plusSeconds(60)));
    flushClear();

    var hist = adapter.loadHistory(caseId);
    assertThat(hist.get(0).state()).isEqualTo(StageState.COMPLETED);
    assertThat(hist.get(0).exitedAt()).isNotNull();
    assertThat(hist.get(1).state()).isEqualTo(StageState.ACTIVE);
    assertThat(hist.get(1).enteredAt()).isNotNull();
  }

  @Test
  void skipMarksIntermediateAsSkippedWithNullEnteredAt() {
    UUID caseId = bootstrapped();

    adapter.appendTransition(
        new StageRepository.Transition(
            caseId,
            "intake",
            0,
            "decision",
            2,
            List.of(new StageRepository.SkippedStage("review", 1)),
            "manual",
            "u1",
            NOW.plusSeconds(60)));
    flushClear();

    var hist = adapter.loadHistory(caseId);
    assertThat(hist.get(0).state()).isEqualTo(StageState.COMPLETED);
    assertThat(hist.get(1).state()).isEqualTo(StageState.SKIPPED);
    assertThat(hist.get(1).enteredAt()).isNull();
    assertThat(hist.get(1).exitedAt()).isNotNull();
    assertThat(hist.get(2).state()).isEqualTo(StageState.ACTIVE);
  }

  @Test
  void lastStageCompletionClearsCurrentStageCache() {
    UUID caseId = bootstrapped();
    // Walk: intake → review → decision → done.
    adapter.appendTransition(
        new StageRepository.Transition(
            caseId, "intake", 0, "review", 1, List.of(), "manual", null, NOW.plusSeconds(10)));
    adapter.appendTransition(
        new StageRepository.Transition(
            caseId, "review", 1, "decision", 2, List.of(), "manual", null, NOW.plusSeconds(20)));
    adapter.appendTransition(
        new StageRepository.Transition(
            caseId, "decision", 2, null, null, List.of(), "manual", null, NOW.plusSeconds(30)));
    flushClear();

    var hist = adapter.loadHistory(caseId);
    assertThat(hist).allMatch(s -> s.state() == StageState.COMPLETED);
    var c = caseRepo.findById(caseId).orElseThrow();
    assertThat(c.getCurrentStageId()).isNull();
    assertThat(c.getCurrentStageOrdinal()).isNull();
  }

  @Test
  void concurrentMissRaisesStg003WhenFromStageAlreadyAdvanced() {
    UUID caseId = bootstrapped();
    // First caller wins.
    adapter.appendTransition(
        new StageRepository.Transition(
            caseId, "intake", 0, "review", 1, List.of(), "manual", null, NOW.plusSeconds(10)));
    flushClear();

    // Second caller still thinks intake is ACTIVE — conditional UPDATE returns 0.
    assertThatThrownBy(
            () ->
                adapter.appendTransition(
                    new StageRepository.Transition(
                        caseId,
                        "intake",
                        0,
                        "review",
                        1,
                        List.of(),
                        "manual",
                        null,
                        NOW.plusSeconds(20))))
        .isInstanceOf(WksStageException.class)
        .satisfies(ex -> assertThat(((WksStageException) ex).getCode()).isEqualTo("WKS-STG-003"));
  }

  // ---- helpers ----

  private UUID bootstrapped() {
    UUID caseId = newCase();
    adapter.materialiseStages(caseId, THREE, NOW);
    adapter.appendTransition(
        new StageRepository.Transition(
            caseId, null, null, "intake", 0, List.of(), "wks-auto-rule", "case-create", NOW));
    flushClear();
    return caseId;
  }

  private UUID newCase() {
    UUID id = UUID.randomUUID();
    Case c =
        new Case(
            id, "loan-application", 1, "open", null, Map.of(), "pi-" + id, NOW, actorId, NOW, 0L);
    caseAdapter.save(c);
    flushClear();
    return id;
  }

  private void flushClear() {
    em.flush();
    em.clear();
  }
}
