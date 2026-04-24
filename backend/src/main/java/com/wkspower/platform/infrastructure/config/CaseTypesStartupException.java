package com.wkspower.platform.infrastructure.config;

/**
 * Thrown by {@link CaseTypeStartupLoader} only when {@code wks.case-types.fail-on-invalid=true} AND
 * at least one case-type YAML failed validation. The message lists every offending file and the
 * distinct error codes — Spring Boot surfaces this as a context-failure and exits non-zero.
 */
public class CaseTypesStartupException extends IllegalStateException {

  public CaseTypesStartupException(String message) {
    super(message);
  }
}
