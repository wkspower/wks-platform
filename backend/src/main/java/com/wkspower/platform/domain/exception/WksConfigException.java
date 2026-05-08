package com.wkspower.platform.domain.exception;

import java.util.List;
import java.util.Map;
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
 *
 * <p>Story 3.8 — adds an optional {@code meta} map that the {@code GlobalExceptionHandler} includes
 * in the {@link com.wkspower.platform.api.dto.ApiResponse#meta()} field. Used to carry the
 * {@code blastRadius} report in blast-radius rejection responses (AC2).
 */
public class WksConfigException extends WksException {

  private final List<ErrorDetail> errors;

  /**
   * Optional metadata attached to the error response envelope. Null means "no meta" — {@link
   * com.wkspower.platform.api.GlobalExceptionHandler} will use {@link Map#of()} as the default.
   */
  private final Map<String, Object> meta;

  public WksConfigException(List<ErrorDetail> errors) {
    this(errors, null);
  }

  /**
   * Story 3.8 — constructor that carries response-level metadata (e.g. {@code blastRadius} report).
   *
   * @param errors non-empty aggregate of error details
   * @param meta optional metadata to include in {@code ApiResponse.meta}; may be {@code null}
   */
  public WksConfigException(List<ErrorDetail> errors, Map<String, Object> meta) {
    super(ErrorCode.WKS_CFG_000, "Configuration invalid");
    Objects.requireNonNull(errors, "errors");
    if (errors.isEmpty()) {
      throw new IllegalArgumentException(
          "WksConfigException requires at least one ErrorDetail — "
              + "aggregate path is for multi-error failures, never an empty list.");
    }
    this.errors = List.copyOf(errors);
    this.meta = (meta == null) ? null : Map.copyOf(meta);
  }

  public List<ErrorDetail> getErrors() {
    return errors;
  }

  /**
   * Returns the response-level meta map, or {@code null} when none was provided. The exception
   * handler uses {@link Map#of()} when this returns {@code null}.
   */
  public Map<String, Object> getMeta() {
    return meta;
  }
}
