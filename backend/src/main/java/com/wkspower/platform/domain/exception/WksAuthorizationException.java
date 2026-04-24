package com.wkspower.platform.domain.exception;

/** Thrown when an authenticated user lacks authority for the requested operation. */
public class WksAuthorizationException extends WksException {

  /**
   * @deprecated prefer {@link ErrorCode#WKS_API_403}; kept for downstream call-sites.
   */
  @Deprecated public static final String CODE = ErrorCode.WKS_API_403.wire();

  public static final String DEFAULT_MESSAGE = "Forbidden";

  public WksAuthorizationException() {
    super(ErrorCode.WKS_API_403, DEFAULT_MESSAGE);
  }

  public WksAuthorizationException(String message) {
    super(ErrorCode.WKS_API_403, message);
  }
}
