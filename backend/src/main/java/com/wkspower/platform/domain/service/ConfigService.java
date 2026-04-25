package com.wkspower.platform.domain.service;

import com.wkspower.platform.domain.config.RegistrationResult;
import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.port.CaseTypeRegistrar;
import com.wkspower.platform.domain.port.CaseTypeSource;
import java.nio.file.Path;

/**
 * Orchestrates load + validate + register for case-type YAML. Pure Java — no Spring, no Jackson, no
 * SnakeYAML. The infrastructure adapters ({@code CaseTypeSourceAdapter}, {@code CaseTypeRegistry})
 * wire this into the Spring context.
 *
 * <p>First real callers: {@code CaseTypeStartupLoader} (Story 2.1) and the admin deploy endpoint
 * (Story 2.2). Both drive the same {@link #validateAndRegister(Path)} method — the HTTP layer is a
 * thin wrapper around a file read.
 */
public class ConfigService {

  private final CaseTypeSource source;
  private final CaseTypeRegistrar registrar;

  public ConfigService(CaseTypeSource source, CaseTypeRegistrar registrar) {
    this.source = source;
    this.registrar = registrar;
  }

  /**
   * Load {@code file}, validate it, and on success register with the registry. Returns the {@link
   * ValidationResult} so callers can react to errors (startup loader logs WARNs; admin endpoint
   * raises {@code WksConfigException}). Registry-side version conflicts ({@code WKS-CFG-011}) are
   * appended to the returned errors list so the caller sees a single aggregate.
   */
  public ValidationResult validateAndRegister(Path file) {
    ValidationResult result = source.load(file);
    if (result.isInvalid() || result.config().isEmpty()) {
      return result;
    }
    RegistrationResult reg = registrar.register(result.config().get());
    if (reg.outcome() == RegistrationResult.Outcome.REJECTED_OLDER_VERSION) {
      return ValidationResult.invalid(reg.errors());
    }
    return result;
  }

  /** Byte-driven variant — used by the admin deploy endpoint (Story 2.2). */
  public ValidationResult validateAndRegister(String sourceName, byte[] bytes) {
    ValidationResult result = this.source.loadBytes(sourceName, bytes);
    if (result.isInvalid() || result.config().isEmpty()) {
      return result;
    }
    RegistrationResult reg = registrar.register(result.config().get());
    if (reg.outcome() == RegistrationResult.Outcome.REJECTED_OLDER_VERSION) {
      return ValidationResult.invalid(reg.errors());
    }
    return result;
  }
}
