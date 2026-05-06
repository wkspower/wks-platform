package com.wkspower.platform.domain.exception;

/**
 * CaseType version-registry runtime exception (Story 3.4 / Decision 20). Carries an error code in
 * the {@code WKS-VER-NNN} band.
 *
 * <p>HTTP mapping (lifted by {@code GlobalExceptionHandler}):
 *
 * <ul>
 *   <li>{@code WKS-VER-001} (CaseType has no published version yet — partial-failure recovery
 *       state) — 503 + {@code Retry-After: 5} (Story 3.4.1 AC4 / finding I6 flipped this from 409,
 *       which misled SI debugging as "client conflict"; 503 + Retry-After accurately signals
 *       "transient, retry safe" for startup races and polling redeploy windows).
 * </ul>
 */
public class WksVersionException extends WksException {

  public WksVersionException(ErrorCode code, String message) {
    super(code, message);
  }
}
