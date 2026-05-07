package com.wkspower.platform.domain.exception;

/**
 * Document upload / storage runtime exception (Story 14.2). Carries one of {@link
 * ErrorCode#WKS_DOC_001} through {@link ErrorCode#WKS_DOC_005}.
 *
 * <p>HTTP mapping (lifted by {@code GlobalExceptionHandler}):
 *
 * <ul>
 *   <li>{@code WKS-DOC-001} (file too large) — 422
 *   <li>{@code WKS-DOC-002} (MIME not in allowlist) — 422
 *   <li>{@code WKS-DOC-003} (filename rejected) — 422
 *   <li>{@code WKS-DOC-004} (document not found) — 404
 *   <li>{@code WKS-DOC-005} (storage backend error) — 502
 * </ul>
 */
public class WksDocumentException extends WksException {

  public WksDocumentException(ErrorCode code, String message) {
    super(code, message);
  }

  public WksDocumentException(ErrorCode code, String message, Throwable cause) {
    super(code, message, cause);
  }
}
