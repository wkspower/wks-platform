package com.wkspower.platform.api;

import com.wkspower.platform.api.dto.ApiResponse;
import com.wkspower.platform.api.dto.ErrorPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Central exception → {@link ApiResponse} mapper. All unhandled exceptions surface here and are
 * wrapped in the standard WKS error envelope before reaching the caller.
 *
 * <p>Story 1.1 scaffold only — typed handlers for domain exceptions (WKS error codes, validation
 * failures, 404s) arrive in Stories 1.4 and 2.x when real domain operations land.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
    return ResponseEntity.internalServerError()
        .body(
            ApiResponse.error(
                ErrorPayload.of("WKS-INTERNAL-ERROR", "An unexpected error occurred")));
  }
}
