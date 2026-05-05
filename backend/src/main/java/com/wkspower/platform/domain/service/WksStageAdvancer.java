package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.event.StageEntered;
import com.wkspower.platform.domain.event.StageExited;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksStageException;
import com.wkspower.platform.domain.model.Stage;
import com.wkspower.platform.domain.model.StageState;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.domain.port.EventPublisher;
import com.wkspower.platform.domain.port.StageRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain service for stage lifecycle transitions (Story 3.1 AC4–AC8). Framework-free — no Spring
 * annotations on this class; the {@code @Transactional} boundary is applied at the wiring layer
 * ({@code ConfigServiceConfig} or a dedicated {@code WksStageAdvancerProxy}) per Decision 4 /
 * NFR36.
 *
 * <p>Three public entry points:
 *
 * <ul>
 *   <li>{@link #bootstrap} — called from {@code CaseService.create} inside the create transaction;
 *       materialises pending rows and flips stage 0 to ACTIVE. No-op on empty stage list.
 *   <li>{@link #advance} — manual advance of one stage (or last-stage completion).
 *   <li>{@link #skipTo} — jump to a future-ordinal stage; intermediates flip to SKIPPED.
 * </ul>
 *
 * <p>Concurrency: {@link StageRepository#appendTransition} performs a conditional UPDATE that
 * pivots on the previous state; rowcount-zero raises {@link ErrorCode#WKS_STG_003}. Two parallel
 * advance calls cannot both succeed.
 */
public class WksStageAdvancer {

  /** Allowed values for the {@code source} parameter — Decision 1. */
  private static final List<String> ALLOWED_SOURCES =
      List.of("wks-auto-rule", "manual", "backend-signal");

  private final StageRepository stageRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public WksStageAdvancer(
      StageRepository stageRepository, EventPublisher eventPublisher, Clock clock) {
    this.stageRepository = Objects.requireNonNull(stageRepository, "stageRepository");
    this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  /**
   * Materialise all declared stages as PENDING and flip stage 0 to ACTIVE inside the same
   * transaction (Story 3.1 AC4). Empty stage list is a no-op — Decision 19's "stage-less paths must
   * remain unbranched" lives here.
   */
  public void bootstrap(
      UUID caseId, List<StageDefinition> stages, String source, String sourceRef) {
    Objects.requireNonNull(caseId, "caseId");
    requireValidSource(source);
    if (stages == null || stages.isEmpty()) {
      return;
    }
    Instant now = clock.now();
    stageRepository.materialiseStages(caseId, stages, now);
    StageDefinition first = stages.get(0);
    stageRepository.appendTransition(
        new StageRepository.Transition(
            caseId, null, null, first.id(), first.ordinal(), List.of(), source, sourceRef, now));
    eventPublisher.publishAfterCommit(
        new StageEntered(caseId, first.id(), first.ordinal(), source, sourceRef, now));
  }

  /**
   * Advance the active stage to the next ordinal, or — when the active stage is last — complete it
   * and clear the case head (Story 3.1 AC5, AC7). Emits {@link StageExited} for the previous active
   * stage and (when not last) {@link StageEntered} for the new active stage. Both events fire
   * {@code afterCommit}.
   */
  public void advance(UUID caseId, String source, String sourceRef) {
    Objects.requireNonNull(caseId, "caseId");
    requireValidSource(source);
    List<Stage> history = requireHistory(caseId);
    Stage active = findActive(history);
    Instant now = clock.now();

    Stage next = findByOrdinal(history, active.ordinal() + 1);
    String toStageId = next == null ? null : next.stageId();
    Integer toOrdinal = next == null ? null : next.ordinal();

    stageRepository.appendTransition(
        new StageRepository.Transition(
            caseId,
            active.stageId(),
            active.ordinal(),
            toStageId,
            toOrdinal,
            List.of(),
            source,
            sourceRef,
            now));

    eventPublisher.publishAfterCommit(
        new StageExited(caseId, active.stageId(), active.ordinal(), source, sourceRef, now));
    if (next != null) {
      eventPublisher.publishAfterCommit(
          new StageEntered(caseId, next.stageId(), next.ordinal(), source, sourceRef, now));
    }
  }

  /**
   * Skip directly to {@code targetStageId}, marking every PENDING intermediate as SKIPPED (Story
   * 3.1 AC6). Backward skip ({@code targetOrdinal <= activeOrdinal}) is rejected with {@code
   * WKS-STG-002}. Skipping forward by exactly one is equivalent to {@link #advance}.
   */
  public void skipTo(UUID caseId, String targetStageId, String source, String sourceRef) {
    Objects.requireNonNull(caseId, "caseId");
    Objects.requireNonNull(targetStageId, "targetStageId");
    requireValidSource(source);
    List<Stage> history = requireHistory(caseId);
    Stage active = findActive(history);
    Stage target = findByStageId(history, targetStageId);
    if (target == null) {
      throw new WksStageException(
          ErrorCode.WKS_STG_002,
          "Skip target '" + targetStageId + "' is not a stage on case " + caseId);
    }
    if (target.ordinal() <= active.ordinal()) {
      throw new WksStageException(
          ErrorCode.WKS_STG_002,
          "Skip target ordinal "
              + target.ordinal()
              + " must be greater than active ordinal "
              + active.ordinal());
    }
    Instant now = clock.now();
    List<StageRepository.SkippedStage> skipped = new ArrayList<>();
    for (int o = active.ordinal() + 1; o < target.ordinal(); o++) {
      Stage intermediate = findByOrdinal(history, o);
      if (intermediate == null || intermediate.state() != StageState.PENDING) {
        // Defensive: history is read-then-write; an intermediate not in PENDING means a
        // concurrent caller mutated the row first. Surface as STG-003.
        throw new WksStageException(
            ErrorCode.WKS_STG_003,
            "Concurrent stage transition on case " + caseId + "; reload and retry");
      }
      skipped.add(new StageRepository.SkippedStage(intermediate.stageId(), intermediate.ordinal()));
    }

    stageRepository.appendTransition(
        new StageRepository.Transition(
            caseId,
            active.stageId(),
            active.ordinal(),
            target.stageId(),
            target.ordinal(),
            skipped,
            source,
            sourceRef,
            now));

    eventPublisher.publishAfterCommit(
        new StageExited(caseId, active.stageId(), active.ordinal(), source, sourceRef, now));
    eventPublisher.publishAfterCommit(
        new StageEntered(caseId, target.stageId(), target.ordinal(), source, sourceRef, now));
    // Skipped intermediates emit no events in Story 3.1 (AC6).
  }

  // ---- helpers ----------------------------------------------------------

  private List<Stage> requireHistory(UUID caseId) {
    List<Stage> history = stageRepository.loadHistory(caseId);
    if (history == null || history.isEmpty()) {
      throw new WksStageException(
          ErrorCode.WKS_STG_001, "Case " + caseId + " has no stages — cannot advance / skip");
    }
    return history;
  }

  private Stage findActive(List<Stage> history) {
    return history.stream()
        .filter(s -> s.state() == StageState.ACTIVE)
        .findFirst()
        .orElseThrow(
            () ->
                new WksStageException(
                    ErrorCode.WKS_STG_001,
                    "Case stages are all completed; nothing to advance / skip"));
  }

  private static Stage findByOrdinal(List<Stage> history, int ordinal) {
    return history.stream().filter(s -> s.ordinal() == ordinal).findFirst().orElse(null);
  }

  private static Stage findByStageId(List<Stage> history, String stageId) {
    return history.stream().filter(s -> s.stageId().equals(stageId)).findFirst().orElse(null);
  }

  private static void requireValidSource(String source) {
    if (source == null || source.isBlank()) {
      throw new IllegalArgumentException("source is required (one of " + ALLOWED_SOURCES + ")");
    }
    if (!ALLOWED_SOURCES.contains(source)) {
      throw new IllegalArgumentException(
          "source '" + source + "' must be one of " + ALLOWED_SOURCES);
    }
  }
}
