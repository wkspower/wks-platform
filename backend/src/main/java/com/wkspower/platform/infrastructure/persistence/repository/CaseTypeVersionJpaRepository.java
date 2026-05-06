package com.wkspower.platform.infrastructure.persistence.repository;

import com.wkspower.platform.infrastructure.persistence.entity.CaseTypeVersionEntity;
import com.wkspower.platform.infrastructure.persistence.entity.CaseTypeVersionId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link CaseTypeVersionEntity}. Story 3.4 / Decision 20 — the
 * append-only CaseType version registry.
 *
 * <p>Derived-name finders use the @IdClass field names on the entity ({@code caseTypeId}, {@code
 * version}) directly — Spring Data resolves these against the composite-key entity.
 */
public interface CaseTypeVersionJpaRepository
    extends JpaRepository<CaseTypeVersionEntity, CaseTypeVersionId> {

  Optional<CaseTypeVersionEntity> findByCaseTypeIdAndVersion(String caseTypeId, int version);

  Optional<CaseTypeVersionEntity> findFirstByCaseTypeIdOrderByVersionDesc(String caseTypeId);

  Optional<CaseTypeVersionEntity> findByCaseTypeIdAndDefinitionHash(
      String caseTypeId, String definitionHash);
}
