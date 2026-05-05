package com.wkspower.platform.infrastructure.persistence.repository;

import com.wkspower.platform.domain.model.StageState;
import com.wkspower.platform.infrastructure.persistence.entity.CaseStageHistoryEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data repository for {@link CaseStageHistoryEntity} (Story 3.1). The conditional UPDATE
 * methods are the optimistic-lock surface for AC8 — each pivots on a previous {@link StageState} so
 * concurrent transitions cannot both succeed.
 */
public interface CaseStageHistoryJpaRepository extends JpaRepository<CaseStageHistoryEntity, UUID> {

  List<CaseStageHistoryEntity> findByCaseIdOrderByOrdinalAsc(UUID caseId);

  /**
   * Conditional UPDATE pivoting on the previous state — used to flip ACTIVE → COMPLETED. Returns
   * the rowcount; the adapter raises {@code WKS-STG-003} when zero.
   */
  @Modifying
  @Query(
      "UPDATE CaseStageHistoryEntity h "
          + "   SET h.state = :newState, h.exitedAt = :exitedAt "
          + " WHERE h.caseId = :caseId "
          + "   AND h.stageId = :stageId "
          + "   AND h.state = :expectedState")
  int conditionalUpdateStateAndExitedAt(
      @Param("caseId") UUID caseId,
      @Param("stageId") String stageId,
      @Param("expectedState") StageState expectedState,
      @Param("newState") StageState newState,
      @Param("exitedAt") Instant exitedAt);

  /**
   * Conditional UPDATE pivoting on PENDING — flips PENDING → ACTIVE while stamping {@code
   * entered_at}, {@code source}, {@code source_ref}.
   */
  @Modifying
  @Query(
      "UPDATE CaseStageHistoryEntity h "
          + "   SET h.state = :newState, "
          + "       h.enteredAt = :enteredAt, "
          + "       h.source = :source, "
          + "       h.sourceRef = :sourceRef "
          + " WHERE h.caseId = :caseId "
          + "   AND h.stageId = :stageId "
          + "   AND h.state = :expectedState")
  int conditionalUpdateActivate(
      @Param("caseId") UUID caseId,
      @Param("stageId") String stageId,
      @Param("expectedState") StageState expectedState,
      @Param("newState") StageState newState,
      @Param("enteredAt") Instant enteredAt,
      @Param("source") String source,
      @Param("sourceRef") String sourceRef);

  /**
   * Conditional UPDATE pivoting on PENDING — flips PENDING → SKIPPED while stamping {@code
   * exited_at}, {@code source}, {@code source_ref}. {@code entered_at} stays NULL — these rows were
   * never active.
   */
  @Modifying
  @Query(
      "UPDATE CaseStageHistoryEntity h "
          + "   SET h.state = :newState, "
          + "       h.exitedAt = :exitedAt, "
          + "       h.source = :source, "
          + "       h.sourceRef = :sourceRef "
          + " WHERE h.caseId = :caseId "
          + "   AND h.stageId = :stageId "
          + "   AND h.state = :expectedState")
  int conditionalUpdateSkip(
      @Param("caseId") UUID caseId,
      @Param("stageId") String stageId,
      @Param("expectedState") StageState expectedState,
      @Param("newState") StageState newState,
      @Param("exitedAt") Instant exitedAt,
      @Param("source") String source,
      @Param("sourceRef") String sourceRef);
}
