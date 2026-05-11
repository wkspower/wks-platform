package com.wkspower.platform.audit;

import com.wkspower.platform.domain.event.RebaseApplied;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Story 3.9 review remediation — emits the {@code event=admin.case.rebase} audit log line strictly
 * AFTER the surrounding transaction commits, replacing the prior {@code log.info} that fired inside
 * {@link com.wkspower.platform.api.controller.AdminController#rebaseApply}'s {@code @Transactional}
 * scope. Rollback ⇒ no event fires ⇒ no audit line.
 *
 * <p>The wire string is preserved verbatim from the prior implementation so SI runbook greps and
 * log-shipping rules continue to match:
 *
 * <pre>
 * event=admin.case.rebase outcome=ACCEPTED priorVersion=N newVersion=M caseId=X forceOverrideReason=Y
 * </pre>
 *
 * <p>Per memory {@code feedback_transactional_db_exception_postgres.md}: never log audit inside a
 * {@code @Transactional} scope, because a subsequent DB exception aborts the transaction on
 * Postgres yet the audit line already fired.
 */
@Component
public class RebaseAuditListener {

  private static final Logger log = LoggerFactory.getLogger(RebaseAuditListener.class);

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onRebaseApplied(RebaseApplied event) {
    log.info(
        "event=admin.case.rebase outcome=ACCEPTED priorVersion={} newVersion={} caseId={}"
            + " forceOverrideReason={}",
        event.fromVersion(),
        event.toVersion(),
        event.caseId(),
        event.forceOverrideReason());
  }
}
