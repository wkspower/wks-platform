package com.wkspower.platform.domain.exception;

/** Thrown for domain-level validation failures outside Jakarta Validation's reach. */
public class WksValidationException extends WksException {

  public static final String CODE = "WKS-API-001";

  private final String field;

  public WksValidationException(String message) {
    this(message, null);
  }

  public WksValidationException(String message, String field) {
    super(CODE, message);
    this.field = field;
  }

  public String getField() {
    return field;
  }
}
