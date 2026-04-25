package com.wkspower.platform.domain.config;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.exception.ErrorDetail;
import java.util.List;
import java.util.Optional;

/**
 * Outcome of running a case-type YAML through the loader + validator pipeline. An invariant holds:
 * exactly one of {@code errors.isEmpty()} and {@code config.isPresent()} is true — enforced at
 * construction so a caller can never encounter "no errors, no config".
 */
public record ValidationResult(List<ErrorDetail> errors, Optional<CaseTypeConfig> config) {

  public ValidationResult {
    errors = List.copyOf(errors);
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
    return new ValidationResult(List.of(), Optional.of(config));
  }

  public static ValidationResult invalid(List<ErrorDetail> errors) {
    return new ValidationResult(errors, Optional.empty());
  }

  public boolean isInvalid() {
    return !errors.isEmpty();
  }
}
