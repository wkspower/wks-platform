package com.wkspower.platform.infrastructure.persistence.repository;

import com.wkspower.platform.infrastructure.persistence.entity.CaseTypeDeploymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for {@link CaseTypeDeploymentEntity}. Story 2.4 folded debt #1 — used
 * by {@code ProcessDefinitionKeyCache} as a write-through layer over a durable mapping, so
 * admin-deployed case types resolve to a process-definition key after a JVM restart even with no
 * YAML on disk.
 */
public interface CaseTypeDeploymentJpaRepository
    extends JpaRepository<CaseTypeDeploymentEntity, String> {}
