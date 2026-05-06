package com.wkspower.platform.infrastructure.persistence.repository;

import com.wkspower.platform.infrastructure.persistence.entity.StatusOptionEntity;
import com.wkspower.platform.infrastructure.persistence.entity.StatusOptionId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA repository for {@link StatusOptionEntity} (Story 3.7).
 *
 * <p>Derived-name finders use the @IdClass field names on the entity ({@code caseTypeId}, {@code
 * version}, {@code stageId}, {@code statusId}) directly.
 */
public interface StatusOptionJpaRepository
    extends JpaRepository<StatusOptionEntity, StatusOptionId> {

  List<StatusOptionEntity> findByCaseTypeIdAndVersionAndStageIdOrderByOrdinalAsc(
      String caseTypeId, int version, String stageId);

  Optional<StatusOptionEntity> findByCaseTypeIdAndVersionAndStageIdAndStatusId(
      String caseTypeId, int version, String stageId, String statusId);

  @Query(
      "SELECT COALESCE(MAX(s.ordinal), -1) FROM StatusOptionEntity s "
          + "WHERE s.caseTypeId = :caseTypeId AND s.version = :version AND s.stageId = :stageId")
  int findMaxOrdinal(String caseTypeId, int version, String stageId);
}
