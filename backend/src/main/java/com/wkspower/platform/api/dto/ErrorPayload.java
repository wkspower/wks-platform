package com.wkspower.platform.api.dto;

/**
 * Error details carried inside {@link ApiResponse#error()}. All values are optional — {@code field}
 * is populated only for validation failures.
 *
 * @param code WKS error code (e.g. {@code WKS-CASE-NOT-FOUND})
 * @param message human-readable description; safe to show to end users
 * @param field originating field for validation errors; {@code null} otherwise
 */
public record ErrorPayload(String code, String message, String field) {

  public static ErrorPayload of(String code, String message) {
    return new ErrorPayload(code, message, null);
  }

  public static ErrorPayload ofField(String code, String message, String field) {
    return new ErrorPayload(code, message, field);
  }
}
