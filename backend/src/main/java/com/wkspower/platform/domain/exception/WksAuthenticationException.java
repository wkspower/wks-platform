package com.wkspower.platform.domain.exception;

/**
 * Thrown on any failed authentication: unknown email, wrong password, or inactive user. The same
 * generic message is used for every case to avoid user enumeration.
 */
public class WksAuthenticationException extends WksException {

  public static final String CODE = "WKS-API-401";
  public static final String DEFAULT_MESSAGE = "Invalid email or password";

  public WksAuthenticationException() {
    super(CODE, DEFAULT_MESSAGE);
  }

  public WksAuthenticationException(String message) {
    super(CODE, message);
  }
}
