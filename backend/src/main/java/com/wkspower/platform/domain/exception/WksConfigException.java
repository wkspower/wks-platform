package com.wkspower.platform.domain.exception;

import java.util.List;
import java.util.Objects;

/**
 * Thrown for multi-error configuration / deploy failures. Unlike {@link WksValidationException}
 * (single field error), this carries an aggregate of {@link ErrorDetail} entries — config-deploy
 * validators never fail on first, so the transport must surface every violation.
 *
 * <p>Maps to HTTP 422 with umbrella code {@link ErrorCode#WKS_CFG_000} ({@code "WKS-CFG-000" —
 * "Configuration invalid"}). The inner list is serialised into {@code error.errors[]} on the wire.
 *
 * <p>Story 1.4 ships only the plumbing and a test-profile probe; the first real producer lands in
 * Story 2.2 (YAML case-type validation + BPMN deploy).
 */
public class WksConfigException extends WksException {

  private final List<ErrorDetail> errors;

  public WksConfigException(List<ErrorDetail> errors) {
    super(ErrorCode.WKS_CFG_000, "Configuration invalid");
    Objects.requireNonNull(errors, "errors");
    if (errors.isEmpty()) {
      throw new IllegalArgumentException(
          "WksConfigException requires at least one ErrorDetail — "
              + "aggregate path is for multi-error failures, never an empty list.");
    }
    this.errors = List.copyOf(errors);
  }

  public List<ErrorDetail> getErrors() {
    return errors;
  }
}
