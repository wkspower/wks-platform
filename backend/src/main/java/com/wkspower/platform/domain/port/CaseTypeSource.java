package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.config.ValidationResult;
import java.nio.file.Path;
import java.util.Map;

/**
 * Domain-side loader port. The implementation is the infrastructure YAML loader + validator chained
 * together, so {@code ConfigService} can stay framework-free while still driving the real
 * parse/validate pipeline.
 */
public interface CaseTypeSource {

  /** Load and validate a case-type YAML from disk. Never throws for parse/validation failures. */
  ValidationResult load(Path file);

  /** Load from raw bytes — caller supplies a BPMN-by-filename map for mapping validation. */
  ValidationResult loadBytes(String source, byte[] bytes, Map<String, byte[]> bpmnByName);

  /** Load from raw bytes with no BPMN context (YAML-only deploy path). */
  default ValidationResult loadBytes(String source, byte[] bytes) {
    return loadBytes(source, bytes, Map.of());
  }
}
