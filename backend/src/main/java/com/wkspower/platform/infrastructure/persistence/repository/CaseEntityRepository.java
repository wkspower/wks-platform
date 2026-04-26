package com.wkspower.platform.infrastructure.persistence.repository;

import com.wkspower.platform.infrastructure.persistence.entity.CaseEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for {@link CaseEntity}. The list query uses a JPQL constructor
 * projection into {@link CaseSummaryProjection} so the wide JSON {@code data} column is never
 * fetched on list-path SQL (perf NFR16 — story 2.3 AC7).
 */
public interface CaseEntityRepository extends JpaRepository<CaseEntity, UUID> {

  @Query(
      "SELECT new com.wkspower.platform.infrastructure.persistence.repository.CaseSummaryProjection("
          + "c.id, c.caseTypeId, c.status, c.assignee, c.createdAt, c.updatedAt) "
          + "FROM CaseEntity c "
          + "WHERE c.caseTypeId = :caseTypeId "
          + "AND (:status IS NULL OR c.status = :status)")
  Page<CaseSummaryProjection> findSummaryByCaseType(
      @Param("caseTypeId") String caseTypeId, @Param("status") String status, Pageable pageable);

  /**
   * Fetch only id + data for the visible page (Story 2.3 D4) — keeps the wide JSON column out of
   * the main list projection while still letting the adapter enrich {@code CaseSummary.fields}.
   */
  @Query("SELECT c FROM CaseEntity c WHERE c.id IN :ids")
  List<CaseEntity> findDataByIds(@Param("ids") Collection<UUID> ids);
}
