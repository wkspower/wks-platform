package com.wkspower.platform.api;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.ErrorPayload;
import com.wkspower.platform.domain.exception.ErrorCode;
import com.wkspower.platform.domain.exception.WksAuthenticationException;
import com.wkspower.platform.domain.exception.WksAuthorizationException;
import com.wkspower.platform.domain.exception.WksConfigException;
import com.wkspower.platform.domain.exception.WksConflictException;
import com.wkspower.platform.domain.exception.WksException;
import com.wkspower.platform.domain.exception.WksNotFoundException;
import com.wkspower.platform.domain.exception.WksStageException;
import com.wkspower.platform.domain.exception.WksValidationAggregateException;
import com.wkspower.platform.domain.exception.WksValidationException;
import com.wkspower.platform.domain.exception.WksWorkflowEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps exceptions into the WKS error envelope with stable {@code WKS-*} codes.
 *
 * <p>The {@code wksErrorCode} MDC key is set for the duration of each handler so structured log
 * lines emitted during response building carry the code. It is cleared in a {@code finally}.
 *
 * <p>Stack traces are logged at ERROR for 5xx and never placed in the response body. Authentication
 * and validation failures log at WARN.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  static final String MDC_KEY = "wksErrorCode";
  static final String CODE_INTERNAL = ErrorCode.WKS_RTM_500.wire();
  static final String CODE_JSON = ErrorCode.WKS_API_002.wire();

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(WksAuthenticationException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuth(WksAuthenticationException ex) {
    MDC.put(MDC_KEY, ex.getCode());
    try {
      log.warn("Authentication failed: {}", ex.getCode());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.error(ErrorPayload.of(ex.getCode(), ex.getMessage())));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  @ExceptionHandler(WksAuthorizationException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuthz(WksAuthorizationException ex) {
    MDC.put(MDC_KEY, ex.getCode());
    try {
      log.warn("Authorization failed: {}", ex.getCode());
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(ErrorPayload.of(ex.getCode(), ex.getMessage())));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleSpringAccessDenied(AccessDeniedException ex) {
    String code = ErrorCode.WKS_API_403.wire();
    MDC.put(MDC_KEY, code);
    try {
      log.warn("Authorization failed (Spring access denied): {}", code);
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(ErrorPayload.of(code, "Access denied")));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  @ExceptionHandler(WksConfigException.class)
  public ResponseEntity<ApiResponse<Void>> handleConfig(WksConfigException ex) {
    MDC.put(MDC_KEY, ex.getCode());
    try {
      log.warn("Configuration invalid: {} ({} error(s))", ex.getCode(), ex.getErrors().size());
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
          .body(
              ApiResponse.error(
                  ErrorPayload.ofAggregate(ex.getCode(), ex.getMessage(), ex.getErrors())));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  @ExceptionHandler(WksNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNotFound(WksNotFoundException ex) {
    MDC.put(MDC_KEY, ex.getCode());
    try {
      log.warn("Resource not found: {}", ex.getCode());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error(ErrorPayload.of(ex.getCode(), ex.getMessage())));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  @Override
  protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException ex,
      org.springframework.http.HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String code = ErrorCode.WKS_API_413.wire();
    MDC.put(MDC_KEY, code);
    try {
      log.warn("Multipart upload too large: {}", code);
      return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
          .body(
              ApiResponse.error(
                  ErrorPayload.of(
                      code, "Multipart upload exceeds the configured size cap (1 MB per part)")));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  @ExceptionHandler(WksValidationAggregateException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationAggregate(
      WksValidationAggregateException ex) {
    MDC.put(MDC_KEY, ex.getCode());
    try {
      log.warn("Validation aggregate: {} ({} error(s))", ex.getCode(), ex.getErrors().size());
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
          .body(
              ApiResponse.error(
                  ErrorPayload.ofAggregate(ex.getCode(), ex.getMessage(), ex.getErrors())));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  @ExceptionHandler(WksConflictException.class)
  public ResponseEntity<ApiResponse<Void>> handleConflict(WksConflictException ex) {
    MDC.put(MDC_KEY, ex.getCode());
    try {
      log.warn("Optimistic-lock conflict: {}", ex.getCode());
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(ApiResponse.error(ErrorPayload.of(ex.getCode(), ex.getMessage())));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  /**
   * Pinned debt from Story 1.4 chunk-3 deferred — surface {@link
   * ObjectOptimisticLockingFailureException} as {@code WKS-RTM-409} for any persistence adapter
   * that hasn't pre-translated to {@link WksConflictException}. {@code AdminUserSeeder}'s catch
   * broaden (Task 9.4) closes the seeder-side path; this handler covers anything else.
   */
  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(
      ObjectOptimisticLockingFailureException ex) {
    String code = ErrorCode.WKS_RTM_409.wire();
    MDC.put(MDC_KEY, code);
    try {
      log.warn("Optimistic-lock conflict (Spring): {}", code);
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              ApiResponse.error(
                  ErrorPayload.of(
                      code, "Resource was modified by another transaction; reload and retry")));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  /**
   * Story 3.1 AC10 — map stage-lifecycle exceptions per AC9: {@code WKS-STG-001} → 409 (already
   * complete), {@code WKS-STG-002} → 422 (backward skip), {@code WKS-STG-003} → 409 (concurrent
   * transition; reload and retry), {@code WKS-STG-004} → 404 (unknown caseId).
   */
  @ExceptionHandler(WksStageException.class)
  public ResponseEntity<ApiResponse<Void>> handleStage(WksStageException ex) {
    MDC.put(MDC_KEY, ex.getCode());
    try {
      log.warn("Stage lifecycle error: {}", ex.getCode());
      HttpStatus status =
          switch (ex.getCode()) {
            case "WKS-STG-002", "WKS-STG-008", "WKS-STG-010", "WKS-STG-011" ->
                HttpStatus.UNPROCESSABLE_ENTITY;
            case "WKS-STG-004", "WKS-STG-012", "WKS-STG-013" -> HttpStatus.NOT_FOUND;
              // Story 3.7 — WKS-STG-007 = duplicate append (409); WKS-STG-009 = mutate-class (405).
            case "WKS-STG-007" -> HttpStatus.CONFLICT;
            case "WKS-STG-009" -> HttpStatus.METHOD_NOT_ALLOWED;
            default -> HttpStatus.CONFLICT; // WKS-STG-001, WKS-STG-003
          };
      return ResponseEntity.status(status)
          .body(ApiResponse.error(ErrorPayload.of(ex.getCode(), ex.getMessage())));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  /**
   * Story 3.4 / Decision 20 — CaseType version-registry exceptions. Story 3.4.1 AC4 (finding I6)
   * flips the HTTP semantic from {@code 409 Conflict} (misleading — it is not a client conflict) to
   * {@code 503 Service Unavailable} with a {@code Retry-After: 5} header: the registry-not- primed
   * condition is recoverable (startup race, polling redeploy window) and "transient, retry safe" is
   * the most actionable signal for SI operators.
   */
  @ExceptionHandler(com.wkspower.platform.domain.exception.WksVersionException.class)
  public ResponseEntity<ApiResponse<Void>> handleVersion(
      com.wkspower.platform.domain.exception.WksVersionException ex) {
    MDC.put(MDC_KEY, ex.getCode());
    try {
      log.warn("CaseType version-registry error: {}", ex.getCode());
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
          .header(HttpHeaders.RETRY_AFTER, "5")
          .body(ApiResponse.error(ErrorPayload.of(ex.getCode(), ex.getMessage())));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  /**
   * P10 — WksWorkflowEngineException maps to HTTP 502 Bad Gateway. Engine-side deploy failures
   * (WKS-CFG-025) are surfaced through this exception via AdminController; the 502 signals to
   * callers that the upstream engine is unhealthy and the request is safely retryable once the
   * engine recovers. Note: the wire code is WKS-RTM-500 (carried by WksWorkflowEngineException) not
   * WKS-CFG-025 — the CFG-025 code is visible in the ConfigService log at ERROR level.
   */
  @ExceptionHandler(WksWorkflowEngineException.class)
  public ResponseEntity<ApiResponse<Void>> handleEngineFailure(WksWorkflowEngineException ex) {
    MDC.put(MDC_KEY, ex.getCode());
    try {
      log.error("Workflow engine failure: {}", ex.getCode(), ex);
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
          .body(ApiResponse.error(ErrorPayload.of(ex.getCode(), "Workflow engine unavailable")));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  @ExceptionHandler(WksValidationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDomainValidation(WksValidationException ex) {
    MDC.put(MDC_KEY, ex.getCode());
    try {
      log.warn("Validation failed: {} (field={})", ex.getCode(), ex.getField());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.error(
                  ErrorPayload.ofField(ex.getCode(), ex.getMessage(), ex.getField())));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      org.springframework.http.HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    String code = ErrorCode.WKS_API_001.wire();
    MDC.put(MDC_KEY, code);
    try {
      String field =
          ex.getBindingResult().getFieldError() != null
              ? ex.getBindingResult().getFieldError().getField()
              : null;
      String message =
          ex.getBindingResult().getFieldError() != null
              ? ex.getBindingResult().getFieldError().getDefaultMessage()
              : "Validation failed";
      log.warn("Bean-validation failed: {} (field={})", code, field);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(ErrorPayload.ofField(code, message, field)));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      org.springframework.http.HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    MDC.put(MDC_KEY, CODE_JSON);
    try {
      log.warn("Malformed JSON body: {}", CODE_JSON);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(ErrorPayload.of(CODE_JSON, "Malformed JSON body")));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  /**
   * Fallback for any {@link WksException} subclass without a dedicated handler. Preserves the
   * exception's wire code (never collapses to {@code WKS-RTM-500}) and defaults the status to 500 —
   * any new subclass that needs a different status must add a more specific
   * {@code @ExceptionHandler} above this one.
   */
  @ExceptionHandler(WksException.class)
  public ResponseEntity<ApiResponse<Void>> handleWks(WksException ex) {
    MDC.put(MDC_KEY, ex.getCode());
    try {
      log.error("Unhandled WksException subtype (defaulting to 500): {}", ex.getCode(), ex);
      return ResponseEntity.internalServerError()
          .body(ApiResponse.error(ErrorPayload.of(ex.getCode(), ex.getMessage())));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
    MDC.put(MDC_KEY, CODE_INTERNAL);
    try {
      log.error("Unhandled exception", ex);
      return ResponseEntity.internalServerError()
          .body(ApiResponse.error(ErrorPayload.of(CODE_INTERNAL, "An unexpected error occurred")));
    } finally {
      MDC.remove(MDC_KEY);
    }
  }
}
