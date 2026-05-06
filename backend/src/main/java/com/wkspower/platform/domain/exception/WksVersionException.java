package com.wkspower.platform.domain.exception;

/**
 * CaseType version-registry runtime exception (Story 3.4 / Decision 20). Carries an error code in
 * the {@code WKS-VER-NNN} band.
 *
 * <p>HTTP mapping (lifted by {@code GlobalExceptionHandler}):
 *
 * <ul>
 *   <li>{@code WKS-VER-001} (CaseType has no published version yet — partial-failure recovery
 *       state) — 409
 * </ul>
 */
public class WksVersionException extends WksException {

  public WksVersionException(ErrorCode code, String message) {
    super(code, message);
  }
}
