package com.wkspower.platform.infrastructure.persistence;

import com.wkspower.platform.domain.port.CaseStatusUpdater;
import com.wkspower.platform.domain.port.Clock;
import com.wkspower.platform.infrastructure.persistence.entity.CaseEntity;
import com.wkspower.platform.infrastructure.persistence.repository.CaseEntityRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter for {@link CaseStatusUpdater}. Runs in {@link Propagation#MANDATORY} so it joins the
 * existing engine transaction — when invoked from {@code CaseStatusListener} during BPMN execution,
 * the engine and the row update commit (or roll back) atomically.
 */
@Component
public class CaseStatusAdapter implements CaseStatusUpdater {

  private final CaseEntityRepository repository;
  private final Clock clock;

  public CaseStatusAdapter(CaseEntityRepository repository, Clock clock) {
    this.repository = repository;
    this.clock = clock;
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public Optional<String> updateStatus(UUID caseId, String newStatus) {
    Optional<CaseEntity> found = repository.findById(caseId);
    if (found.isEmpty()) {
      return Optional.empty();
    }
    // Story 4.4b AC1/AC3 — use targeted JPQL UPDATE (updateStatusOnly) instead of a full entity
    // save. The full-entity-save path reads the JPA first-level-cache entity which may have a
    // stale currentStageId (set by updateStageCache's JPQL bypass in the same transaction), then
    // overwrites it on save. The targeted JPQL UPDATE mutates only status + updatedAt so stage
    // cache columns are never clobbered by this method.
    String previous = found.get().getStatus();
    repository.updateStatusOnly(caseId, newStatus, clock.now());
    return Optional.ofNullable(previous);
  }
}
