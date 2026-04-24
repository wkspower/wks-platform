package com.wkspower.platform.domain.exception;

/**
 * Thrown for domain-level validation failures outside Jakarta Validation's reach. The wire code
 * defaults to {@link ErrorCode#WKS_API_001} (legacy constant {@link #CODE}) — pass an explicit
 * {@link ErrorCode} when a more specific code is applicable (e.g. pagination: {@code WKS_API_003};
 * sort: {@code WKS_API_004}).
 */
public class WksValidationException extends WksException {

  /**
   * @deprecated prefer {@link ErrorCode#WKS_API_001}; kept for existing call-sites.
   */
  @Deprecated public static final String CODE = ErrorCode.WKS_API_001.wire();

  private final String field;

  public WksValidationException(String message) {
    this(ErrorCode.WKS_API_001, message, null);
  }

  public WksValidationException(String message, String field) {
    this(ErrorCode.WKS_API_001, message, field);
  }

  public WksValidationException(ErrorCode code, String message, String field) {
    super(code, message);
    this.field = field;
  }

  public String getField() {
    return field;
  }
}
