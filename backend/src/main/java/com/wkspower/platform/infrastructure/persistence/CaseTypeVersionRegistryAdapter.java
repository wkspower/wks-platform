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
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * JPA adapter implementing {@link CaseTypeVersionRegistry} (Story 3.4 / Decision 20). Append-only —
 * every {@code register} call either inserts a row or short-circuits as idempotent. Never updates
 * an existing row.
 *
 * <p>Concurrency (Story 3.4.1 AC1, finding C3): the insert runs at the datasource default isolation
 * — typically {@code READ COMMITTED} on Postgres. Correctness is enforced by the database, not the
 * isolation level: the primary key {@code (case_type_id, version)} plus the unique-by-hash index
 * {@code (case_type_id, definition_hash)} guarantee "exactly one row" for both byte-canonical
 * re-deploys (idempotent return) and racing first-deploys (loser catches the integrity violation
 * and re-reads the winner's row in a fresh transaction).
 *
 * <p>Story 3.4 ran the whole method at {@code SERIALIZABLE} and caught {@link
 * DataIntegrityViolationException} only. Two flaws surfaced in finding C3 / the Postgres-IT (Story
 * 3.4.1 AC2):
 *
 * <ol>
 *   <li>{@link ConcurrencyFailureException} is a sibling, not subclass, of {@link
 *       DataIntegrityViolationException}; Postgres SSI {@code 40001 serialization_failure} would
 *       propagate uncaught. H2 SERIALIZABLE serialises rather than aborts, hiding the divergence.
 *   <li>Once Postgres aborts a transaction with {@code duplicate key}, every subsequent statement
 *       in that same transaction errors out with {@code current transaction is aborted, commands
 *       ignored}. Catching the exception inside a single {@code @Transactional} boundary means the
 *       recovery {@code findByCaseTypeIdAndDefinitionHash} read fails too — the loser thread never
 *       sees the winner's row and the {@link DataIntegrityViolationException} re-throws.
 * </ol>
 *
 * <p>The fix: split the method. The outer {@link #register} is NOT {@code @Transactional}; it
 * delegates the insert to {@link #attemptInsert} (a {@code REQUIRES_NEW} transaction that commits
 * or rolls back independently), and on conflict performs the recovery read in a fresh {@link
 * TransactionTemplate}-managed transaction. Both the unique-by-hash and PK-violation races are
 * caught and translated to an idempotent return; any non-recoverable exception still propagates.
 */
@Component
public class CaseTypeVersionRegistryAdapter implements CaseTypeVersionRegistry {

  private final CaseTypeVersionJpaRepository repository;
  private final CaseTypeContentHasher hasher;
  private final TransactionTemplate insertTx;
  private final TransactionTemplate readTx;

  public CaseTypeVersionRegistryAdapter(
      CaseTypeVersionJpaRepository repository,
      CaseTypeContentHasher hasher,
      PlatformTransactionManager txManager) {
    this.repository = repository;
    this.hasher = hasher;
    this.insertTx = new TransactionTemplate(txManager);
    this.insertTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    this.readTx = new TransactionTemplate(txManager);
    this.readTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    this.readTx.setReadOnly(true);
  }

  @Override
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

    // Idempotent short-circuit: byte-canonical re-deploy returns the existing row's version.
    Optional<CaseTypeVersionEntity> existing = readByHash(caseTypeId, hash);
    if (existing.isPresent()) {
      return CaseTypeVersionRegistration.idempotent(existing.get().getVersion(), hash);
    }

    try {
      Integer assignedVersion = attemptInsert(caseTypeId, hash, rawYamlBytes, resolvedPublishedBy);
      return CaseTypeVersionRegistration.registered(assignedVersion, hash);
    } catch (DataIntegrityViolationException | ConcurrencyFailureException race) {
      // Concurrent first-deploy: another thread won the (case_type_id, version) PK or the
      // unique-by-hash index, or a Postgres SSI 40001 serialization_failure surfaced as
      // ConcurrencyFailureException. Re-read by hash in a FRESH transaction (the original was
      // aborted and is not usable on Postgres) — if present, the other writer won (return
      // idempotent). Otherwise the loser hashed differently and lost a version-slot race; rethrow.
      Optional<CaseTypeVersionEntity> winner = readByHash(caseTypeId, hash);
      if (winner.isPresent()) {
        return CaseTypeVersionRegistration.idempotent(winner.get().getVersion(), hash);
      }
      throw race;
    }
  }

  /**
   * Inserts a new row in its own transaction (REQUIRES_NEW). Returns the assigned version on
   * commit; throws {@link DataIntegrityViolationException} or {@link ConcurrencyFailureException}
   * on race. The caller MUST treat both exception types identically.
   */
  private int attemptInsert(
      String caseTypeId, String hash, byte[] rawYamlBytes, String publishedBy) {
    Integer assigned =
        insertTx.execute(
            status -> {
              int nextVersion =
                  repository
                      .findFirstByCaseTypeIdOrderByVersionDesc(caseTypeId)
                      .map(e -> e.getVersion() + 1)
                      .orElse(1);
              String yamlAsText = new String(rawYamlBytes, StandardCharsets.UTF_8);
              CaseTypeVersionEntity entity =
                  new CaseTypeVersionEntity(
                      caseTypeId, nextVersion, hash, yamlAsText, Instant.now(), publishedBy);
              repository.saveAndFlush(entity);
              return nextVersion;
            });
    return assigned == null ? -1 : assigned;
  }

  /** Read the row by hash in a FRESH transaction (the caller's may be aborted). */
  private Optional<CaseTypeVersionEntity> readByHash(String caseTypeId, String hash) {
    return readTx.execute(status -> repository.findByCaseTypeIdAndDefinitionHash(caseTypeId, hash));
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
