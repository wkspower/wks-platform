package com.wkspower.platform.domain.exception;

/**
 * Thrown when the embedded workflow engine fails on a deploy or query call. Always wraps the
 * underlying engine exception so the cause stack survives into logs.
 *
 * <p>Maps to HTTP 500 + {@link ErrorCode#WKS_RTM_500} via {@code GlobalExceptionHandler}. By the
 * time this is thrown the BPMN validator has already accepted the input, so this is genuinely an
 * engine-side runtime failure (DB, classpath, schema-mismatch) — not a user-input quality problem.
 */
public class WksWorkflowEngineException extends WksException {

  public WksWorkflowEngineException(String message, Throwable cause) {
    super(ErrorCode.WKS_RTM_500, message, cause);
  }

  public WksWorkflowEngineException(String message) {
    super(ErrorCode.WKS_RTM_500, message, null);
  }
}
