package com.wkspower.platform.domain.exception;

/**
 * Thrown on optimistic-locking conflict — the row was modified by another transaction between read
 * and write. Maps to HTTP 409 + {@link ErrorCode#WKS_RTM_409} via {@code GlobalExceptionHandler}.
 *
 * <p>Story 2.3 introduces this exception together with {@code WKS_RTM_409}. Phase 0 surfaces only
 * the wire code; Phase 1 will surface a "case was modified by another user" UX message.
 */
public class WksConflictException extends WksException {

  public WksConflictException(String message) {
    super(ErrorCode.WKS_RTM_409, message);
  }

  public WksConflictException(String message, Throwable cause) {
    super(ErrorCode.WKS_RTM_409, message, cause);
  }
}
