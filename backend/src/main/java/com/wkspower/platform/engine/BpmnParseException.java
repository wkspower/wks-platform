package com.wkspower.platform.engine;

/**
 * Thrown by {@link BpmnParser} when the bytes are not a parseable BPMN 2.0 document. Caught by
 * {@link BpmnValidator} and converted into a {@code WKS-CFG-010} {@code ErrorDetail} — never
 * propagates into application code.
 */
class BpmnParseException extends RuntimeException {

  BpmnParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
