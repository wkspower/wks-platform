package com.wkspower.platform.infrastructure.persistence;

import com.wkspower.platform.domain.config.model.StageDefinition;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksStageException;
import com.wkspower.platform.domain.model.Stage;
import com.wkspower.platform.domain.model.StageState;
import com.wkspower.platform.domain.port.StageRepository;
import com.wkspower.platform.infrastructure.persistence.entity.CaseStageHistoryEntity;
import com.wkspower.platform.infrastructure.persistence.repository.CaseEntityRepository;
import com.wkspower.platform.infrastructure.persistence.repository.CaseStageHistoryJpaRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter for {@link StageRepository} (Story 3.1). Pure JPA — no engine package import (Decision
 * 19: stage lifecycle is engine-agnostic). The optimistic-lock surface (AC8) is the conditional
 * UPDATE methods on {@link CaseStageHistoryJpaRepository}; rowcount-zero raises {@code
 * WKS-STG-003}.
 */
@Component
class StageRepositoryAdapter implements StageRepository {

  private final CaseStageHistoryJpaRepository history;
  private final CaseEntityRepository cases;

  StageRepositoryAdapter(CaseStageHistoryJpaRepository history, CaseEntityRepository cases) {
    this.history = history;
    this.cases = cases;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Stage> loadHistory(UUID caseId) {
    List<CaseStageHistoryEntity> rows = history.findByCaseIdOrderByOrdinalAsc(caseId);
    List<Stage> out = new ArrayList<>(rows.size());
    for (CaseStageHistoryEntity r : rows) {
      out.add(toDomain(r));
    }
    return out;
  }

  @Override
  @Transactional
  public void materialiseStages(UUID caseId, List<StageDefinition> stages, Instant createdAt) {
    if (stages == null || stages.isEmpty()) {
      return;
    }
    for (StageDefinition s : stages) {
      CaseStageHistoryEntity row =
          new CaseStageHistoryEntity(
              UUID.randomUUID(),
              caseId,
              s.id(),
              s.ordinal(),
              StageState.PENDING,
              null,
              null,
              null,
              null,
              createdAt);
      history.save(row);
    }
  }

  @Override
  @Transactional
  public void appendTransition(Transition t) {
    // 1) flip skipped intermediates first — keeps the active row visible if any of these miss.
    for (SkippedStage skipped : t.skipped()) {
      int rc =
          history.conditionalUpdateSkip(
              t.caseId(),
              skipped.stageId(),
              StageState.PENDING,
              StageState.SKIPPED,
              t.timestamp(),
              t.source(),
              t.sourceRef());
      if (rc == 0) {
        throw new WksStageException(
            ErrorCode.WKS_STG_003,
            "Concurrent stage transition on case "
                + t.caseId()
                + " (skipped intermediate '"
                + skipped.stageId()
                + "' was not in PENDING)");
      }
    }

    // 2) flip the previously-active row to COMPLETED — null fromStageId means bootstrap (no prior
    //    active row).
    if (t.fromStageId() != null) {
      int rc =
          history.conditionalUpdateStateAndExitedAt(
              t.caseId(), t.fromStageId(), StageState.ACTIVE, StageState.COMPLETED, t.timestamp());
      if (rc == 0) {
        throw new WksStageException(
            ErrorCode.WKS_STG_003,
            "Concurrent stage transition on case "
                + t.caseId()
                + " (stage '"
                + t.fromStageId()
                + "' was not ACTIVE)");
      }
    }

    // 3) flip the new active row from PENDING → ACTIVE — null toStageId means last-stage
    //    completion (no next row to activate).
    if (t.toStageId() != null) {
      int rc =
          history.conditionalUpdateActivate(
              t.caseId(),
              t.toStageId(),
              StageState.PENDING,
              StageState.ACTIVE,
              t.timestamp(),
              t.source(),
              t.sourceRef());
      if (rc == 0) {
        throw new WksStageException(
            ErrorCode.WKS_STG_003,
            "Concurrent stage transition on case "
                + t.caseId()
                + " (stage '"
                + t.toStageId()
                + "' was not PENDING)");
      }
    }

    // 4) update the denormalised cache on cases. toStageId == null clears the cache (last stage
    //    completed) so the case has no further head.
    cases.updateStageCache(t.caseId(), t.toStageId(), t.toOrdinal());
  }

  @Override
  @Transactional
  public void remapStage(
      UUID caseId, String fromStageId, String toStageId, int toOrdinal, Instant at) {
    // 1) Close the previously-active fromStageId row with REMAPPED state.
    int rc = history.conditionalUpdateRemapped(caseId, fromStageId, at);
    if (rc == 0) {
      throw new WksStageException(
          ErrorCode.WKS_STG_003,
          "Stage remap failed on case "
              + caseId
              + ": stage '"
              + fromStageId
              + "' was not ACTIVE (concurrent modification)");
    }

    // 2) Insert a new ACTIVE row for toStageId. The target stage has no PENDING row in
    //    case_stage_history because the case has never been pinned to this toVersion's stages.
    //    Direct INSERT into ACTIVE state; entered_at stamped at the remap time.
    CaseStageHistoryEntity newRow =
        new CaseStageHistoryEntity(
            UUID.randomUUID(),
            caseId,
            toStageId,
            toOrdinal,
            StageState.ACTIVE,
            at, // entered_at = remap timestamp
            null, // exited_at = null (still active)
            "rebase-remap",
            fromStageId, // sourceRef = the stage being remapped FROM (for audit traceability)
            at);
    history.save(newRow);
  }

  private static Stage toDomain(CaseStageHistoryEntity e) {
    return new Stage(
        e.getId(),
        e.getCaseId(),
        e.getStageId(),
        e.getOrdinal(),
        e.getState(),
        e.getEnteredAt(),
        e.getExitedAt(),
        e.getSource(),
        e.getSourceRef());
  }
}
