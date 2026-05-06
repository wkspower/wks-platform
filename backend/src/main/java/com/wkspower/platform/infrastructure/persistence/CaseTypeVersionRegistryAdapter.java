package com.wkspower.platform.infrastructure.persistence;

import com.wkspower.platform.domain.config.CaseTypeVersionRecord;
import com.wkspower.platform.domain.config.CaseTypeVersionRegistration;
import com.wkspower.platform.domain.port.CaseTypeVersionRegistry;
import com.wkspower.platform.infrastructure.config.CaseTypeContentHasher;
import com.wkspower.platform.infrastructure.persistence.entity.CaseTypeVersionEntity;
import com.wkspower.platform.infrastructure.persistence.repository.CaseTypeVersionJpaRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link CaseTypeVersionRegistry} (Story 3.4 / Decision 20). Append-only —
 * every {@code register} call either inserts a row or short-circuits as idempotent. Never updates
 * an existing row.
 *
 * <p>Concurrency: {@link #register(String, byte[], String)} runs at {@link Isolation#SERIALIZABLE}
 * so two threads racing the same first-deploy produce exactly one row. The unique-by-hash index
 * (Story 3.4 AC1) is defence-in-depth — a {@link DataIntegrityViolationException} on insert is
 * translated to an idempotent return so the loser of the race sees the winner's row.
 */
@Component
public class CaseTypeVersionRegistryAdapter implements CaseTypeVersionRegistry {

  private final CaseTypeVersionJpaRepository repository;
  private final CaseTypeContentHasher hasher;

  public CaseTypeVersionRegistryAdapter(
      CaseTypeVersionJpaRepository repository, CaseTypeContentHasher hasher) {
    this.repository = repository;
    this.hasher = hasher;
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public CaseTypeVersionRegistration register(
      String caseTypeId, byte[] rawYamlBytes, String publishedBy) {
    if (caseTypeId == null || caseTypeId.isBlank()) {
      throw new IllegalArgumentException("caseTypeId must be non-blank");
    }
    if (rawYamlBytes == null || rawYamlBytes.length == 0) {
      throw new IllegalArgumentException("rawYamlBytes must be non-null and non-empty");
    }
    String resolvedPublishedBy =
        (publishedBy == null || publishedBy.isBlank()) ? "system:startup" : publishedBy;

    String hash = hasher.hash(rawYamlBytes);

    Optional<CaseTypeVersionEntity> existing =
        repository.findByCaseTypeIdAndDefinitionHash(caseTypeId, hash);
    if (existing.isPresent()) {
      return CaseTypeVersionRegistration.idempotent(existing.get().getVersion(), hash);
    }

    int nextVersion =
        repository
            .findFirstByCaseTypeIdOrderByVersionDesc(caseTypeId)
            .map(e -> e.getVersion() + 1)
            .orElse(1);

    String yamlAsText = new String(rawYamlBytes, StandardCharsets.UTF_8);
    CaseTypeVersionEntity entity =
        new CaseTypeVersionEntity(
            caseTypeId, nextVersion, hash, yamlAsText, Instant.now(), resolvedPublishedBy);
    try {
      repository.saveAndFlush(entity);
    } catch (DataIntegrityViolationException race) {
      // Concurrent first-deploy raced past the SERIALIZABLE check (e.g. H2 promoted a
      // serialisation conflict to integrity violation, or a Postgres unique-violation): re-read
      // by hash; if present, the other writer won — return idempotent. Otherwise rethrow.
      Optional<CaseTypeVersionEntity> winner =
          repository.findByCaseTypeIdAndDefinitionHash(caseTypeId, hash);
      if (winner.isPresent()) {
        return CaseTypeVersionRegistration.idempotent(winner.get().getVersion(), hash);
      }
      throw race;
    }
    return CaseTypeVersionRegistration.registered(nextVersion, hash);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Integer> currentVersion(String caseTypeId) {
    return repository
        .findFirstByCaseTypeIdOrderByVersionDesc(caseTypeId)
        .map(CaseTypeVersionEntity::getVersion);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CaseTypeVersionRecord> findVersion(String caseTypeId, int version) {
    return repository
        .findByCaseTypeIdAndVersion(caseTypeId, version)
        .map(CaseTypeVersionRegistryAdapter::toRecord);
  }

  private static CaseTypeVersionRecord toRecord(CaseTypeVersionEntity e) {
    return new CaseTypeVersionRecord(
        e.getCaseTypeId(),
        e.getVersion(),
        e.getDefinitionHash(),
        e.getDefinitionYaml().getBytes(StandardCharsets.UTF_8),
        e.getPublishedAt(),
        e.getPublishedBy());
  }
}
