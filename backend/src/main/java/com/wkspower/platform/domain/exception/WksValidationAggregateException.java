package com.wkspower.platform.domain.exception;

import java.util.List;
import java.util.Objects;

/**
 * Multi-error aggregate for case-data validation failures. Bound to {@link ErrorCode#WKS_API_002}
 * (per code-review D3) so the wire code distinguishes 422-aggregate-validation from the
 * 400-single-field path that uses {@link ErrorCode#WKS_API_001}.
 *
 * <p>Maps to HTTP 422 with the wire envelope shape {@code error.errors[]}. Story 2.3 introduces
 * this exception when {@code CaseService.create} / {@code update} receives a {@code data} map that
 * does not satisfy the case type's JSON Schema.
 */
public class WksValidationAggregateException extends WksException {

  private final List<ErrorDetail> errors;

  public WksValidationAggregateException(String message, List<ErrorDetail> errors) {
    super(ErrorCode.WKS_API_002, message);
    Objects.requireNonNull(errors, "errors");
    if (errors.isEmpty()) {
      throw new IllegalArgumentException(
          "WksValidationAggregateException requires at least one ErrorDetail.");
    }
    this.errors = List.copyOf(errors);
  }

  public List<ErrorDetail> getErrors() {
    return errors;
  }
}
