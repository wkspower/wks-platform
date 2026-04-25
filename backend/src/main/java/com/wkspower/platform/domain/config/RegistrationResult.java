package com.wkspower.platform.domain.config;

import com.wkspower.platform.domain.exception.ErrorDetail;
import java.util.List;
import java.util.Optional;

/**
 * Outcome of a {@code register} / {@code replace} call. The registry uses version semantics: same
 * version → no-op idempotent re-registration; higher version → swap; lower version → rejected with
 * a {@code WKS-CFG-011} detail.
 */
public record RegistrationResult(Outcome outcome, Optional<ErrorDetail> error) {

  public enum Outcome {
    REGISTERED,
    IDEMPOTENT,
    REPLACED,
    REJECTED_OLDER_VERSION
  }

  public static RegistrationResult registered() {
    return new RegistrationResult(Outcome.REGISTERED, Optional.empty());
  }

  public static RegistrationResult idempotent() {
    return new RegistrationResult(Outcome.IDEMPOTENT, Optional.empty());
  }

  public static RegistrationResult replaced() {
    return new RegistrationResult(Outcome.REPLACED, Optional.empty());
  }

  public static RegistrationResult rejectedOlderVersion(ErrorDetail error) {
    return new RegistrationResult(Outcome.REJECTED_OLDER_VERSION, Optional.of(error));
  }

  public List<ErrorDetail> errors() {
    return error.map(List::of).orElse(List.of());
  }
}
