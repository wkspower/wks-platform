package com.wkspower.platform.domain.config;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.ErrorDetail;
import java.util.List;
import java.util.Optional;

/**
 * Outcome of running a case-type YAML through the loader + validator pipeline. An invariant holds:
 * exactly one of {@code errors.isEmpty()} and {@code config.isPresent()} is true — enforced at
 * construction so a caller can never encounter "no errors, no config".
 *
 * <p>Story 2.7 introduces {@link #warnings()} — non-blocking findings that the loader logs at WARN
 * level but do not prevent the case-type from loading. Today only {@code WKS-CFG-013} (file field
 * marked {@code requiredOnCreate}) populates this list. Existing callers that read {@link
 * #errors()} see no behavior change.
 */
public record ValidationResult(
    List<ErrorDetail> errors, List<ErrorDetail> warnings, Optional<CaseTypeConfig> config) {

  public ValidationResult {
    errors = List.copyOf(errors);
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
    if (errors.isEmpty() == config.isEmpty()) {
      throw new IllegalStateException(
          "ValidationResult invariant: exactly one of errors-empty and config-present must hold "
              + "(errors="
              + errors.size()
              + ", config="
              + (config.isPresent() ? "present" : "absent")
              + ")");
    }
  }

  public static ValidationResult ok(CaseTypeConfig config) {
    return new ValidationResult(List.of(), List.of(), Optional.of(config));
  }

  public static ValidationResult ok(CaseTypeConfig config, List<ErrorDetail> warnings) {
    return new ValidationResult(List.of(), warnings, Optional.of(config));
  }

  public static ValidationResult invalid(List<ErrorDetail> errors) {
    return new ValidationResult(errors, List.of(), Optional.empty());
  }

  public boolean isInvalid() {
    return !errors.isEmpty();
  }

  public boolean hasWarnings() {
    return !warnings.isEmpty();
  }
}
