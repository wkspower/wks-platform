package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.config.ValidationResult;
import com.wkspower.platform.domain.port.CaseTypeSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Adapter that chains {@link CaseTypeYamlLoader} and {@link ConfigValidator} behind the domain-side
 * {@link CaseTypeSource} port. Catastrophic parse failures surface as a {@code WKS-CFG-099}-bearing
 * {@link ValidationResult} so callers treat them uniformly with validation errors — no exception
 * path crosses the port.
 *
 * <p>Story 4.2 — when the YAML declares {@code attachments[]}, this adapter reads the sibling files
 * referenced by {@code attachments[].file} from the YAML's parent directory and supplies the bytes
 * to {@link ConfigValidator#validate(RawCaseTypeConfig, YamlLineIndex, java.util.Map)}. Failure to
 * read a sibling file is silently dropped here — {@link MappingValidator} surfaces it as {@code
 * WKS-MAP-005} ("BPMN file not provided"). The byte-supply path for the {@code loadBytes} contract
 * (multipart admin deploy — Story 4.5) extends this adapter with a caller-supplied byte map.
 */
@Component
public class CaseTypeSourceAdapter implements CaseTypeSource {

  /** Hard cap for sibling BPMN reads — matches the MAX_BPMN_BYTES used in CaseTypeStartupLoader. */
  private static final long MAX_SIBLING_BYTES = 1_048_576L;

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
    Map<String, byte[]> bpmnFiles = readSiblingBpmnFiles(file, read.raw());
    return validator.validate(read.raw(), read.lines(), bpmnFiles);
  }

  @Override
  public ValidationResult loadBytes(String source, byte[] bytes, Map<String, byte[]> bpmnByName) {
    var read = loader.readBytes(source, bytes);
    if (!read.isParsed()) {
      return ValidationResult.invalid(read.errors());
    }
    return validator.validate(read.raw(), read.lines(), bpmnByName);
  }

  /**
   * Read sibling BPMN files for any {@code attachments[].file} the YAML declared. Returns an empty
   * map when the YAML has no attachments. Files outside the YAML's parent directory or absolute
   * paths are silently dropped — {@link MappingValidator} surfaces them as {@code WKS-MAP-005}.
   */
  private Map<String, byte[]> readSiblingBpmnFiles(Path yamlFile, RawCaseTypeConfig raw) {
    if (raw == null || raw.attachments() == null || raw.attachments().isEmpty()) {
      return Map.of();
    }
    Path parent = yamlFile.toAbsolutePath().getParent();
    if (parent == null) {
      return Map.of();
    }
    Map<String, byte[]> out = new HashMap<>();
    for (RawCaseTypeConfig.RawAttachment a : raw.attachments()) {
      if (a == null || a.file() == null || a.file().isBlank()) {
        continue;
      }
      String name = a.file();
      if (out.containsKey(name)) {
        continue;
      }
      Path candidate;
      try {
        candidate = Path.of(name);
      } catch (RuntimeException e) {
        continue;
      }
      if (candidate.isAbsolute()) {
        continue;
      }
      Path resolved = parent.resolve(candidate).normalize();
      if (!resolved.startsWith(parent)) {
        continue;
      }
      if (!Files.isRegularFile(resolved)) {
        continue;
      }
      try {
        if (Files.size(resolved) > MAX_SIBLING_BYTES) {
          continue;
        }
        out.put(name, Files.readAllBytes(resolved));
      } catch (IOException e) {
        // drop — MappingValidator surfaces "BPMN file not provided" via WKS-MAP-005
      }
    }
    return out;
  }
}
