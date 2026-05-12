package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.event.StageEntered;
import com.wkspower.platform.domain.event.StageExited;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksStageException;
import com.wkspower.platform.domain.model.Stage;
import com.wkspower.platform.domain.model.StageState;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.StageRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pure-Java enumeration of {@link WksStageAdvancer} branches (Story 3.1 AC2/AC4–AC9). Stubs the
 * {@link StageRepository} port so the test exercises the domain decision logic without JPA. The IT
 * class covers the SQL conditional-update concurrency surface.
 */
class WksStageAdvancerTest {

  private static final UUID CASE = UUID.randomUUID();
  private static final Instant FIXED = Instant.parse("2026-05-05T12:00:00Z");

  private FakeRepo repo;
  private RecordingPublisher publisher;
  private WksStageAdvancer advancer;

  @BeforeEach
  void resetStubs() {
    repo = new FakeRepo();
    publisher = new RecordingPublisher();
    advancer = new WksStageAdvancer(repo, publisher, () -> FIXED);
  }

  @Test
  void bootstrapEmptyStageListIsNoOp() {
    advancer.bootstrap(CASE, List.of(), "wks-auto-rule", "case-create");

    assertThat(repo.materialised).isEmpty();
    assertThat(repo.transitions).isEmpty();
    assertThat(publisher.events).isEmpty();
  }

  @Test
  void bootstrapMaterialisesStagesAndActivatesFirst() {
    var stages = stages3();
    advancer.bootstrap(CASE, stages, "wks-auto-rule", "case-create");

    assertThat(repo.materialised).hasSize(1);
    assertThat(repo.materialised.get(0)).isEqualTo(stages);
    assertThat(repo.transitions).hasSize(1);
    var t = repo.transitions.get(0);
    assertThat(t.fromStageId()).isNull();
    assertThat(t.toStageId()).isEqualTo("intake");
    assertThat(t.toOrdinal()).isZero();
    assertThat(publisher.events).hasSize(1);
    assertThat(publisher.events.get(0)).isInstanceOf(StageEntered.class);
    StageEntered ev = (StageEntered) publisher.events.get(0);
    assertThat(ev.stageId()).isEqualTo("intake");
    assertThat(ev.source()).isEqualTo("wks-auto-rule");
    assertThat(ev.sourceRef()).isEqualTo("case-create");
  }

  @Test
  void advanceMovesToNextStageAndEmitsBothEvents() {
    repo.history =
        List.of(
            stage("intake", 0, StageState.ACTIVE),
            stage("review", 1, StageState.PENDING),
            stage("decision", 2, StageState.PENDING));

    advancer.advance(CASE, "manual", "user-1");

    assertThat(repo.transitions).hasSize(1);
    var t = repo.transitions.get(0);
    assertThat(t.fromStageId()).isEqualTo("intake");
    assertThat(t.toStageId()).isEqualTo("review");
    assertThat(t.toOrdinal()).isEqualTo(1);

    assertThat(publisher.events).hasSize(2);
    assertThat(publisher.events.get(0)).isInstanceOf(StageExited.class);
    assertThat(publisher.events.get(1)).isInstanceOf(StageEntered.class);
    assertThat(((StageExited) publisher.events.get(0)).stageId()).isEqualTo("intake");
    assertThat(((StageEntered) publisher.events.get(1)).stageId()).isEqualTo("review");
  }

  @Test
  void lastStageAdvanceCompletesAndClearsHead() {
    repo.history =
        List.of(
            stage("intake", 0, StageState.COMPLETED),
            stage("review", 1, StageState.COMPLETED),
            stage("decision", 2, StageState.ACTIVE));

    advancer.advance(CASE, "manual", null);

    var t = repo.transitions.get(0);
    assertThat(t.fromStageId()).isEqualTo("decision");
    assertThat(t.toStageId()).isNull();
    assertThat(t.toOrdinal()).isNull();

    assertThat(publisher.events).hasSize(1);
    assertThat(publisher.events.get(0)).isInstanceOf(StageExited.class);
    assertThat(((StageExited) publisher.events.get(0)).stageId()).isEqualTo("decision");
  }

  @Test
  void advanceWhenAllCompletedRaisesStg001() {
    repo.history =
        List.of(
            stage("intake", 0, StageState.COMPLETED), stage("decision", 1, StageState.COMPLETED));

    assertThatThrownBy(() -> advancer.advance(CASE, "manual", null))
        .isInstanceOf(WksStageException.class)
        .satisfies(ex -> assertThat(((WksStageException) ex).getCode()).isEqualTo("WKS-STG-001"));
  }

  @Test
  void advanceOnZeroStageCaseRaisesStg001() {
    repo.history = List.of();

    assertThatThrownBy(() -> advancer.advance(CASE, "manual", null))
        .isInstanceOf(WksStageException.class)
        .satisfies(ex -> assertThat(((WksStageException) ex).getCode()).isEqualTo("WKS-STG-001"));
  }

  @Test
  void skipToFutureMarksIntermediatesSkipped() {
    repo.history =
        List.of(
            stage("intake", 0, StageState.ACTIVE),
            stage("review", 1, StageState.PENDING),
            stage("decision", 2, StageState.PENDING));

    advancer.skipTo(CASE, "decision", "manual", null);

    var t = repo.transitions.get(0);
    assertThat(t.fromStageId()).isEqualTo("intake");
    assertThat(t.toStageId()).isEqualTo("decision");
    assertThat(t.skipped()).hasSize(1);
    assertThat(t.skipped().get(0).stageId()).isEqualTo("review");

    assertThat(publisher.events).hasSize(2);
    assertThat(publisher.events.get(0)).isInstanceOf(StageExited.class);
    assertThat(publisher.events.get(1)).isInstanceOf(StageEntered.class);
    // No StageEntered / StageExited for the skipped intermediate.
  }

  @Test
  void backwardSkipRaisesStg002() {
    repo.history =
        List.of(
            stage("intake", 0, StageState.COMPLETED),
            stage("review", 1, StageState.ACTIVE),
            stage("decision", 2, StageState.PENDING));

    assertThatThrownBy(() -> advancer.skipTo(CASE, "intake", "manual", null))
        .isInstanceOf(WksStageException.class)
        .satisfies(ex -> assertThat(((WksStageException) ex).getCode()).isEqualTo("WKS-STG-002"));
  }

  @Test
  void skipToUnknownStageRaisesStg002() {
    repo.history =
        List.of(stage("intake", 0, StageState.ACTIVE), stage("review", 1, StageState.PENDING));

    assertThatThrownBy(() -> advancer.skipTo(CASE, "no-such", "manual", null))
        .isInstanceOf(WksStageException.class)
        .satisfies(ex -> assertThat(((WksStageException) ex).getCode()).isEqualTo("WKS-STG-002"));
  }

  @Test
  void nullSourceRejected() {
    assertThatThrownBy(() -> advancer.bootstrap(CASE, stages3(), null, "x"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void invalidSourceRejected() {
    assertThatThrownBy(() -> advancer.bootstrap(CASE, stages3(), "ai", "x"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void concurrentMissPropagatedAsStg003() {
    repo.failOnAppend = ErrorCode.WKS_STG_003;
    repo.history =
        List.of(stage("intake", 0, StageState.ACTIVE), stage("r", 1, StageState.PENDING));

    assertThatThrownBy(() -> advancer.advance(CASE, "manual", null))
        .isInstanceOf(WksStageException.class)
        .satisfies(ex -> assertThat(((WksStageException) ex).getCode()).isEqualTo("WKS-STG-003"));
  }

  // ---- helpers ----

  private static List<StageDefinition> stages3() {
    return List.of(
        new StageDefinition("intake", "Intake", 0),
        new StageDefinition("review", "Review", 1),
        new StageDefinition("decision", "Decision", 2));
  }

  private static Stage stage(String id, int ordinal, StageState state) {
    return new Stage(UUID.randomUUID(), CASE, id, ordinal, state, null, null, null, null);
  }

  private static final class FakeRepo implements StageRepository {
    List<Stage> history = new ArrayList<>();
    final List<List<StageDefinition>> materialised = new ArrayList<>();
    final List<Transition> transitions = new ArrayList<>();
    ErrorCode failOnAppend = null;

    @Override
    public List<Stage> loadHistory(UUID caseId) {
      return List.copyOf(history);
    }

    @Override
    public void materialiseStages(UUID caseId, List<StageDefinition> stages, Instant createdAt) {
      materialised.add(List.copyOf(stages));
    }

    @Override
    public void appendTransition(Transition t) {
      if (failOnAppend != null) {
        throw new WksStageException(failOnAppend, "stub concurrent miss");
      }
      transitions.add(t);
    }

    @Override
    public void remapStage(
        UUID caseId, String fromStageId, String toStageId, int toOrdinal, Instant at) {}
  }

  private static final class RecordingPublisher implements EventPublisher {
    final List<Object> events = new ArrayList<>();

    @Override
    public void publish(Object event) {
      events.add(event);
    }
  }
}
