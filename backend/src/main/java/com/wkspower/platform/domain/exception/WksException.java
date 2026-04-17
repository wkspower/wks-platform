package com.wkspower.platform.domain.exception;

/**
 * Base type for every domain/application exception that maps to a WKS error envelope. Each subclass
 * fixes a {@code WKS-*} error code that {@code GlobalExceptionHandler} lifts into the response
 * body.
 */
public abstract class WksException extends RuntimeException {

  private final String code;

  protected WksException(String code, String message) {
    super(message);
    this.code = code;
  }

  protected WksException(String code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
