package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.config.ValidationResult;
import java.nio.file.Path;

/**
 * Domain-side loader port. The implementation is the infrastructure YAML loader + validator chained
 * together, so {@code ConfigService} can stay framework-free while still driving the real
 * parse/validate pipeline.
 */
public interface CaseTypeSource {

  /** Load and validate a case-type YAML from disk. Never throws for parse/validation failures. */
  ValidationResult load(Path file);

  /** Load from raw bytes — used by the (future) admin deploy endpoint. */
  ValidationResult loadBytes(String source, byte[] bytes);
}
