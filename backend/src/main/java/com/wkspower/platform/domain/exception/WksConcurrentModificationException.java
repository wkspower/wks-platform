package com.wkspower.platform.domain.exception;

/**
 * Story 3.9 review remediation — thrown by {@code CaseRebaseService.apply} when the version-checked
 * JPQL UPDATE matches zero rows, meaning the case's optimistic-lock {@code version} column was
 * bumped by another transaction between this service's read and write. The case row is unchanged;
 * the caller should reload and retry.
 *
 * <p>Distinct from {@link WksConflictException} because the trigger surface is the rebase apply
 * path (operator tooling), not a generic Hibernate {@code ObjectOptimisticLockingFailureException}.
 * Maps to HTTP 409 + {@link ErrorCode#WKS_CFG_035} via {@code GlobalExceptionHandler}.
 */
public class WksConcurrentModificationException extends WksException {

  public WksConcurrentModificationException(ErrorCode code, String message) {
    super(code, message);
  }

  public WksConcurrentModificationException(ErrorCode code, String message, Throwable cause) {
    super(code, message, cause);
  }
}
