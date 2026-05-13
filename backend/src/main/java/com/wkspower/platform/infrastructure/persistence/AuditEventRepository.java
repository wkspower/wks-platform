package com.wkspower.platform.infrastructure.persistence;

import com.wkspower.platform.audit.AuditEvent;
import com.wkspower.platform.audit.AuditEventMapper;
import com.wkspower.platform.audit.AuditEventWriter;
import com.wkspower.platform.infrastructure.persistence.entity.AuditEventEntity;
import com.wkspower.platform.infrastructure.persistence.repository.AuditEventJpaRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Story 9-3 — outward-facing repository for {@code audit_events}. Surface is deliberately narrow:
 * {@link #insert} (write) + {@link #findByCaseId} (read). NO {@code save}, {@code update}, {@code
 * delete}, {@code deleteAll}, {@code findAll}, or any other mutation entry-point exists or will be
 * added. The append-only invariant is enforced primarily at this Java surface; DB-level revokes
 * (deny UPDATE/DELETE to app role) are Phase-1 deepening, not Sprint 12.
 *
 * <p>{@link #insert} uses {@link Propagation#REQUIRES_NEW} so:
 *
 * <ul>
 *   <li>An audit insert failure does NOT roll back the caller's transaction (which already
 *       committed by the time {@code EditAuditEmitter} fires AFTER_COMMIT — the new transaction is
 *       a fresh boundary regardless).
 *   <li>Per {@code feedback_transactional_db_exception_postgres}: a caught DB exception inside the
 *       caller's {@code @Transactional} followed by another DB call aborts on Postgres. A fresh
 *       transaction avoids the abort path entirely — relevant for any future caller that might
 *       persist an audit row while still inside an open transaction.
 * </ul>
 *
 * <p>Hand-rolled wrapper over {@link AuditEventJpaRepository} rather than letting that interface be
 * the bean wired into callers, because Spring Data's {@code JpaRepository} contract exposes the
 * full mutation surface ({@code save}, {@code delete}, ...) which would defeat AC2's
 * insert-only-by-construction invariant.
 *
 * <p>Reads do NOT use {@code REQUIRES_NEW} — they should see the caller's view (most callers will
 * be the timeline / feed API, with no surrounding transaction anyway). {@code findByCaseId} is
 * annotated {@code @Transactional(readOnly=true)} to ensure a session is available even when
 * open-session-in-view is disabled.
 *
 * <p>Design-decision deviation note (Story 9-3 spec §"Design decisions (closed in spec)" item 2):
 * the spec prescribed a hand-rolled JdbcTemplate or narrow EntityManager wrapper to avoid the
 * {@code JpaRepository} mutation surface. This implementation instead ships an {@code
 * AuditEventWriter} port + JPA entity + Spring-Data {@code JpaRepository} delegate to align with
 * the project's standard JPA pattern. The append-only invariant is preserved at the {@code
 * AuditEventWriter} surface (only {@code insert} + {@code findByCaseId} reach callers); the {@code
 * AuditEventJpaRepository} delegate is explicitly OFF-CONTRACT for direct autowiring — see its
 * javadoc.
 */
@Repository
public class AuditEventRepository implements AuditEventWriter {

  private final AuditEventJpaRepository delegate;

  public AuditEventRepository(AuditEventJpaRepository delegate) {
    this.delegate = delegate;
  }

  /**
   * Insert one audit event in a fresh transaction. Caller-side failures (e.g. WARN-and-fallthrough
   * in {@code EditAuditEmitter}) catch any {@link RuntimeException} thrown here without affecting
   * their own transaction.
   *
   * <p>{@code AuditEvent.createdAt} is ignored on write — the DB stamps it via the column DEFAULT.
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 5)
  public void insert(AuditEvent event) {
    AuditEventEntity entity =
        new AuditEventEntity(
            event.id(),
            event.caseId(),
            event.eventType(),
            AuditEventMapper.sourceType(event.source()),
            AuditEventMapper.sourcePayload(event.source()),
            event.result(),
            event.fieldId(),
            event.openTaskId(),
            event.formId(),
            event.occurredAt());
    delegate.save(entity);
  }

  /**
   * Return the most recent {@code limit} audit events for a given case, newest first. Hits the
   * {@code (case_id, occurred_at DESC)} index. Returns an empty list if no rows exist or {@code
   * limit <= 0}.
   */
  @Override
  @Transactional(readOnly = true)
  public List<AuditEvent> findByCaseId(UUID caseId, int limit) {
    if (limit <= 0) {
      return List.of();
    }
    return delegate.findByCaseIdOrderByOccurredAtDesc(caseId, PageRequest.of(0, limit)).stream()
        .map(this::toDomain)
        .collect(Collectors.toList());
  }

  private AuditEvent toDomain(AuditEventEntity e) {
    return new AuditEvent(
        e.getId(),
        e.getCaseId(),
        e.getEventType(),
        AuditEventMapper.fromColumns(e.getSourceType(), e.getSourcePayload()),
        e.getResult(),
        e.getFieldId(),
        e.getOpenTaskId(),
        e.getFormId(),
        e.getOccurredAt(),
        e.getCreatedAt());
  }
}
