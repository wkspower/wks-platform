package com.wkspower.platform.infrastructure.persistence.repository;

import com.wkspower.platform.infrastructure.persistence.entity.FormDraftEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Story 5.4 — Spring Data repository for {@link FormDraftEntity}. The single read accessor scopes
 * to {@code (caseId, formId, userId)} — there is intentionally no {@code
 * findByCaseIdAndFormId(...)} variant (AC5 cross-user leakage guard).
 */
public interface FormDraftJpaRepository extends JpaRepository<FormDraftEntity, UUID> {

  Optional<FormDraftEntity> findByCaseIdAndFormIdAndUserId(UUID caseId, String formId, UUID userId);

  @Modifying
  @Query(
      "delete from FormDraftEntity d "
          + "where d.caseId = :caseId and d.formId = :formId and d.userId = :userId")
  int deleteByScope(
      @Param("caseId") UUID caseId, @Param("formId") String formId, @Param("userId") UUID userId);

  List<FormDraftEntity> findByUpdatedAtBefore(Instant cutoff);
}
