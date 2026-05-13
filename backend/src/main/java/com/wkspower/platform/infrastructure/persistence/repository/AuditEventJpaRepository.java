package com.wkspower.platform.infrastructure.persistence.repository;

import com.wkspower.platform.infrastructure.persistence.entity.AuditEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Story 9-3 — Spring Data interface for {@link AuditEventEntity}. Conventionally lives in {@code
 * infrastructure.persistence.repository} alongside the other JPA repositories.
 *
 * <p>This interface inherits the {@code JpaRepository} mutation surface ({@code save}, {@code
 * delete}, {@code deleteAll}, ...). Per the Story 9-3 AC2 append-only contract, ONLY {@code
 * com.wkspower.platform.audit.AuditEventRepository} is allowed to call into it — that public-facing
 * handle exposes a narrowed surface (insert + read by case) and the AC2 surface-guard test asserts
 * its API shape via reflection.
 *
 * <p>Method names follow Spring Data derivation. {@code findByCaseIdOrderByOccurredAtDesc(UUID,
 * Pageable)} hits the {@code idx_audit_events_case_id_occurred_at} index for the 9-2 feed read
 * path.
 */
public interface AuditEventJpaRepository extends JpaRepository<AuditEventEntity, UUID> {

  List<AuditEventEntity> findByCaseIdOrderByOccurredAtDesc(UUID caseId, Pageable page);
}
