package com.wkspower.platform.domain.exception;

/** Thrown when an authenticated user lacks authority for the requested operation. */
public class WksAuthorizationException extends WksException {

  public static final String CODE = "WKS-API-403";
  public static final String DEFAULT_MESSAGE = "Forbidden";

  public WksAuthorizationException() {
    super(CODE, DEFAULT_MESSAGE);
  }

  public WksAuthorizationException(String message) {
    super(CODE, message);
  }
}
