package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.port.CaseTypeSource;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

/**
 * Adapter that chains {@link CaseTypeYamlLoader} and {@link ConfigValidator} behind the domain-side
 * {@link CaseTypeSource} port. Catastrophic parse failures surface as a {@code WKS-CFG-099}-bearing
 * {@link ValidationResult} so callers treat them uniformly with validation errors — no exception
 * path crosses the port.
 */
@Component
public class CaseTypeSourceAdapter implements CaseTypeSource {

  private final CaseTypeYamlLoader loader;
  private final ConfigValidator validator;

  public CaseTypeSourceAdapter(CaseTypeYamlLoader loader, ConfigValidator validator) {
    this.loader = loader;
    this.validator = validator;
  }

  @Override
  public ValidationResult load(Path file) {
    var read = loader.read(file);
    if (!read.isParsed()) {
      return ValidationResult.invalid(read.errors());
    }
    return validator.validate(read.raw(), read.lines());
  }

  @Override
  public ValidationResult loadBytes(String source, byte[] bytes) {
    var read = loader.readBytes(source, bytes);
    if (!read.isParsed()) {
      return ValidationResult.invalid(read.errors());
    }
    return validator.validate(read.raw(), read.lines());
  }
}
