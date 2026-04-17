package com.wkspower.platform.api;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.ErrorPayload;
import com.wkspower.platform.domain.exception.WksAuthenticationException;
import com.wkspower.platform.domain.exception.WksAuthorizationException;
import com.wkspower.platform.domain.exception.WksValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
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
  static final String CODE_INTERNAL = "WKS-RTM-500";
  static final String CODE_JSON = "WKS-API-002";

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
    String code = WksValidationException.CODE;
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
