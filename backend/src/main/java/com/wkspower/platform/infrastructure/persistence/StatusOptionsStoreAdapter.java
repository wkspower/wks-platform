package com.wkspower.platform.infrastructure.persistence;

import com.wkspower.platform.domain.config.model.StatusColor;
import com.wkspower.platform.domain.config.model.StatusDefinition;
import com.wkspower.platform.domain.port.StatusOptionsStore;
import com.wkspower.platform.infrastructure.persistence.entity.StatusOptionEntity;
import com.wkspower.platform.infrastructure.persistence.repository.StatusOptionJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link StatusOptionsStore} (Story 3.7). Backs the {@code status_options}
 * table — the durable record of admin-driven append-class status edits per Decision 21.
 *
 * <p>Concurrency: the {@code (case_type_id, version, stage_id, status_id)} primary key enforces "no
 * two rows for the same status id"; concurrent appends serialise on the unique-key insert. The
 * adapter computes the next ordinal by reading {@code MAX(ordinal) + 1}; a parallel append race may
 * result in two rows with the same intended ordinal — but the PK on {@code statusId} prevents
 * silent overwrites, and the ordinal column is monotonic-ish (ties allowed) since the read+write is
 * not in a single SERIALIZABLE transaction. AC3 scenario 11 verifies "two distinct rows" — not
 * "perfectly contiguous ordinals." Phase-0 traffic does not require strict ordinal contiguity; the
 * {@code idx_status_options_case_type_stage} index supports stable ordering on read.
 */
@Component
public class StatusOptionsStoreAdapter implements StatusOptionsStore {

  private final StatusOptionJpaRepository repository;

  public StatusOptionsStoreAdapter(StatusOptionJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<StatusDefinition> listFor(String caseTypeId, int version, String stageId) {
    return repository
        .findByCaseTypeIdAndVersionAndStageIdOrderByOrdinalAsc(caseTypeId, version, stageId)
        .stream()
        .map(StatusOptionsStoreAdapter::toDomain)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<StatusDefinition> findOne(
      String caseTypeId, int version, String stageId, String statusId) {
    return repository
        .findByCaseTypeIdAndVersionAndStageIdAndStatusId(caseTypeId, version, stageId, statusId)
        .map(StatusOptionsStoreAdapter::toDomain);
  }

  @Override
  @Transactional
  public StatusDefinition append(
      String caseTypeId,
      int version,
      String stageId,
      String statusId,
      String displayName,
      String color,
      boolean terminal) {
    // Explicit pre-check — Hibernate's `save()` on an @IdClass entity issues a SELECT then merges
    // when a row already exists (treating the call as an update), which would silently overwrite
    // an admin's previous append rather than surfacing a duplicate. The JPA call cannot be relied
    // on for "INSERT or fail" semantics here; check first, then save. The composite PK still
    // serves as defence-in-depth for concurrent-insert races (caught below).
    if (repository
        .findByCaseTypeIdAndVersionAndStageIdAndStatusId(caseTypeId, version, stageId, statusId)
        .isPresent()) {
      throw new DuplicateStatusException(
          "status '"
              + statusId
              + "' already exists on "
              + caseTypeId
              + "@v"
              + version
              + " stage="
              + stageId);
    }
    int nextOrdinal = repository.findMaxOrdinal(caseTypeId, version, stageId) + 1;
    StatusOptionEntity entity =
        new StatusOptionEntity(
            caseTypeId, version, stageId, statusId, displayName, color, terminal, nextOrdinal);
    try {
      repository.saveAndFlush(entity);
    } catch (DataIntegrityViolationException race) {
      // PK collision on (caseTypeId, version, stageId, statusId) — concurrent append landed first
      // OR the caller didn't pre-check. Surface as DuplicateStatusException; controller maps to
      // WKS-STG-007 / 409.
      throw new DuplicateStatusException(
          "status '"
              + statusId
              + "' already exists on "
              + caseTypeId
              + "@v"
              + version
              + " stage="
              + stageId);
    }
    return toDomain(entity);
  }

  @Override
  @Transactional
  public Optional<StatusDefinition> rename(
      String caseTypeId,
      int version,
      String stageId,
      String statusId,
      String displayName,
      String color) {
    Optional<StatusOptionEntity> existing =
        repository.findByCaseTypeIdAndVersionAndStageIdAndStatusId(
            caseTypeId, version, stageId, statusId);
    if (existing.isEmpty()) {
      return Optional.empty();
    }
    StatusOptionEntity entity = existing.get();
    if (displayName != null) {
      entity.setDisplayName(displayName);
    }
    if (color != null) {
      entity.setColor(color);
    }
    repository.saveAndFlush(entity);
    return Optional.of(toDomain(entity));
  }

  private static StatusDefinition toDomain(StatusOptionEntity e) {
    StatusColor color = StatusColor.fromWire(e.getColor()).orElse(StatusColor.ZINC);
    return new StatusDefinition(e.getStatusId(), e.getDisplayName(), color, e.isTerminal());
  }
}
