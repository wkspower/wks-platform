package com.wkspower.platform.domain.exception;

/**
 * Thrown on any failed authentication: unknown email, wrong password, or inactive user. The same
 * generic message is used for every case to avoid user enumeration.
 */
public class WksAuthenticationException extends WksException {

  /**
   * @deprecated prefer {@link ErrorCode#WKS_API_401}; kept for downstream call-sites.
   */
  @Deprecated public static final String CODE = ErrorCode.WKS_API_401.wire();

  public static final String DEFAULT_MESSAGE = "Invalid email or password";

  public WksAuthenticationException() {
    super(ErrorCode.WKS_API_401, DEFAULT_MESSAGE);
  }

  public WksAuthenticationException(String message) {
    super(ErrorCode.WKS_API_401, message);
  }
}
