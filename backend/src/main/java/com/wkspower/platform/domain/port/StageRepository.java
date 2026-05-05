package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.model.Stage;
import com.wkspower.platform.domain.model.StageState;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Outbound port for the case stage history (Story 3.1 AC3, AC5, AC8). The infrastructure adapter
 * persists rows in {@code case_stage_history} and updates the denormalised cache columns on {@code
 * cases}; the domain only sees the contract here.
 *
 * <p>The append-only model: every state change is a row update, never a delete. Concurrency is
 * serialised via a conditional UPDATE inside {@link #appendTransition} that pivots on the previous
 * {@link StageState} — rowcount of zero raises {@link
 * com.wkspower.platform.domain.exception.WksStageException} with code {@code WKS-STG-003} (Story
 * 3.1 AC8 — preferred over {@code @Version} on {@code cases} because stage transitions own their
 * own optimistic-lock surface).
 */
public interface StageRepository {

  /** Load the full stage history for a case, ordered by ordinal ascending. */
  List<Stage> loadHistory(UUID caseId);

  /**
   * Materialise pending rows for every declared stage on a freshly created case (Story 3.1 AC4).
   * Inserts one {@link StageState#PENDING} row per definition. Caller (e.g. {@code
   * WksStageAdvancer.bootstrap}) follows up with an {@link #appendTransition} call to flip stage 0
   * to {@link StageState#ACTIVE}. No-op on an empty list.
   *
   * @param caseId owning case id
   * @param stages immutable list of stage definitions (already validated)
   * @param createdAt timestamp stamped on every {@code created_at} column
   */
  void materialiseStages(UUID caseId, List<StageDefinition> stages, Instant createdAt);

  /**
   * Apply a single stage-state transition atomically. The adapter performs:
   *
   * <ol>
   *   <li>conditional UPDATE on the {@code from} row: pivots on {@code (case_id, stage_id, state =
   *       fromState)} — rowcount must equal 1, else throw {@code WKS-STG-003}
   *   <li>conditional UPDATE on the {@code to} row (when {@code toStageId} is non-null): pivots on
   *       {@code (case_id, stage_id, state = PENDING)} — rowcount must equal 1
   *   <li>UPDATE on {@code cases.current_stage_id} / {@code cases.current_stage_ordinal} (caller
   *       supplies the target values; pass nulls to clear after last-stage completion)
   * </ol>
   *
   * <p>Skipped intermediates are passed via {@link Transition#skipped}: each is updated from {@link
   * StageState#PENDING} to {@link StageState#SKIPPED} with {@code entered_at = NULL} and {@code
   * exited_at = clock.now()}. No event is emitted by this port — the caller publishes domain events
   * at the service layer.
   *
   * @param transition fully-described transition; nullable fields are tolerated where documented
   */
  void appendTransition(Transition transition);

  /**
   * Description of a single atomic stage transition. {@code fromStageId} is the previously-active
   * stage (may be {@code null} on bootstrap). {@code toStageId} is the new active stage (may be
   * {@code null} on last-stage completion). {@code skipped} carries any intermediates that flip
   * directly from {@code PENDING} → {@code SKIPPED}.
   */
  record Transition(
      UUID caseId,
      String fromStageId,
      Integer fromOrdinal,
      String toStageId,
      Integer toOrdinal,
      List<SkippedStage> skipped,
      String source,
      String sourceRef,
      Instant timestamp) {

    public Transition {
      skipped = skipped == null ? List.of() : List.copyOf(skipped);
    }
  }

  /** A pending stage that the transition skips over. */
  record SkippedStage(String stageId, int ordinal) {}
}
