package com.wkspower.platform.audit;

import java.util.List;
import java.util.UUID;

/**
 * Story 9-3 — append-only port for the {@code audit_events} table. {@link EditAuditEmitter} depends
 * on this interface rather than the concrete JPA-backed repository so the audit package stays
 * outside the {@code ..infrastructure..} layer (per {@code ArchitectureTest.hexagonalLayering}).
 *
 * <p>Surface is deliberately narrow: {@link #insert} (write, in a new transaction) + {@link
 * #findByCaseId} (read). NO {@code save}, {@code update}, {@code delete}, {@code deleteAll}, {@code
 * findAll}, or any other mutation entry-point exists or will be added. The append-only invariant is
 * enforced primarily at this Java surface; the {@code AuditEventRepository} reflective surface
 * guard (AC2 fallback in {@code EditAuditPersistencePostgresIT}) asserts the public method set.
 *
 * <p>The single implementation lives in {@code
 * com.wkspower.platform.infrastructure.persistence.AuditEventRepository}.
 */
public interface AuditEventWriter {

  /**
   * Insert one audit event in a fresh transaction (REQUIRES_NEW). Implementations MUST guarantee
   * that a failure here does not affect the caller's surrounding transaction.
   */
  void insert(AuditEvent event);

  /**
   * Return the most recent {@code limit} audit events for a given case, newest first. Returns an
   * empty list if no rows exist or {@code limit <= 0}.
   */
  List<AuditEvent> findByCaseId(UUID caseId, int limit);
}
