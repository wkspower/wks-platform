package com.wkspower.platform.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wkspower.platform.domain.exception.ErrorDetail;
import java.util.List;
import java.util.Objects;

/**
 * Error details carried inside {@link ApiResponse#error()}. Field semantics:
 *
 * <ul>
 *   <li>{@code code} — stable WKS code (e.g. {@code WKS-API-003}).
 *   <li>{@code message} — human-readable description, safe for end users.
 *   <li>{@code field} — populated only for single-field validation errors; omitted otherwise.
 *   <li>{@code errors} — populated only for multi-error aggregates (see {@code
 *       WksConfigException}); omitted for single-error responses.
 * </ul>
 *
 * <p>Single-error vs multi-error is a property of the producing handler, not the transport — {@link
 * JsonInclude.Include#NON_EMPTY} hides whichever slot is unused (null field or empty errors list)
 * so the wire shape stays minimal.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorPayload(String code, String message, String field, List<ErrorDetail> errors) {

  public static ErrorPayload of(String code, String message) {
    return new ErrorPayload(code, message, null, null);
  }

  public static ErrorPayload ofField(String code, String message, String field) {
    return new ErrorPayload(code, message, field, null);
  }

  public static ErrorPayload ofAggregate(String code, String message, List<ErrorDetail> errors) {
    Objects.requireNonNull(errors, "errors");
    return new ErrorPayload(code, message, null, List.copyOf(errors));
  }
}
