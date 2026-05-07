package com.wkspower.platform.infrastructure.persistence.repository;

import com.wkspower.platform.infrastructure.persistence.entity.CaseDocumentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for {@code case_documents} (Story 14.2). */
public interface CaseDocumentJpaRepository extends JpaRepository<CaseDocumentEntity, UUID> {

  /**
   * Returns all documents for a given case ordered by upload time descending (newest first). The
   * index {@code idx_case_documents_case_id} covers this query.
   */
  List<CaseDocumentEntity> findByCaseIdOrderByUploadedAtDesc(UUID caseId);
}
