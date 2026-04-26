package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.config.RegistrationResult;
import com.wkspower.platform.domain.config.model.CaseTypeConfig;

/**
 * Domain-side write port for the case-type registry. Exposed publicly on purpose — the "only {@code
 * ConfigService} should call this" intent from Story 2.1 AC7 is enforced by convention (and
 * ArchUnit rule in a future story), not visibility.
 */
public interface CaseTypeRegistrar {

  /**
   * Register (or version-swap) a validated config. Returns {@link RegistrationResult#outcome()}
   * describing what happened — same-version calls are idempotent, lower-version calls are rejected
   * with {@code WKS-CFG-011}.
   */
  RegistrationResult register(CaseTypeConfig config);

  /**
   * Remove the registered config for {@code id}. Used by {@code ConfigService.deploy} to roll back
   * a registration when the engine deploy fails. Idempotent: removing an unknown id is a no-op.
   */
  void remove(String id);
}
