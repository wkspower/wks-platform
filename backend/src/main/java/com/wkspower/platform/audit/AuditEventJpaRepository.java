package com.wkspower.platform.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Story 9-3 — internal Spring Data interface for {@link AuditEventEntity}. Package-private so the
 * {@code JpaRepository} mutation surface ({@code save}, {@code delete}, {@code deleteAll}, ...)
 * cannot be reached from outside this package. {@link AuditEventRepository} is the only
 * outward-facing handle and exposes a deliberately narrow surface (insert + read by case).
 *
 * <p>Method names follow Spring Data derivation. {@code findByCaseIdOrderByOccurredAtDesc(UUID,
 * Pageable)} hits the {@code idx_audit_events_case_id_occurred_at} index for the 9-2 feed read
 * path.
 */
interface AuditEventJpaRepository extends JpaRepository<AuditEventEntity, UUID> {

  List<AuditEventEntity> findByCaseIdOrderByOccurredAtDesc(UUID caseId, Pageable page);
}
