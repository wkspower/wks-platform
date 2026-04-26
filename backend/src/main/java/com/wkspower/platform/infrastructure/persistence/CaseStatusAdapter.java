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
    CaseEntity entity = found.get();
    String previous = entity.getStatus();
    entity.setStatus(newStatus);
    entity.setUpdatedAt(clock.now());
    repository.save(entity);
    return Optional.ofNullable(previous);
  }
}
