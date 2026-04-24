package com.wkspower.platform.domain.exception;

/**
 * Thrown when a requested resource does not exist. Maps to HTTP 404 with code {@link
 * ErrorCode#WKS_API_404}.
 */
public class WksNotFoundException extends WksException {

  public WksNotFoundException(String message) {
    super(ErrorCode.WKS_API_404, message);
  }
}
