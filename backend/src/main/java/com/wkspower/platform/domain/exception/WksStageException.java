package com.wkspower.platform.domain.exception;

/**
 * Stage lifecycle runtime exception (Story 3.1 AC9). Carries one of {@link ErrorCode#WKS_STG_001},
 * {@link ErrorCode#WKS_STG_002}, {@link ErrorCode#WKS_STG_003}, {@link ErrorCode#WKS_STG_004}.
 *
 * <p>HTTP mapping (lifted by {@code GlobalExceptionHandler}):
 *
 * <ul>
 *   <li>{@code WKS-STG-001} (already complete / zero-stage) — 409
 *   <li>{@code WKS-STG-002} (backward skip) — 422
 *   <li>{@code WKS-STG-003} (concurrent transition) — 409
 *   <li>{@code WKS-STG-004} (unknown caseId) — 404
 * </ul>
 */
public class WksStageException extends WksException {

  public WksStageException(ErrorCode code, String message) {
    super(code, message);
  }

  public WksStageException(ErrorCode code, String message, Throwable cause) {
    super(code, message, cause);
  }
}
