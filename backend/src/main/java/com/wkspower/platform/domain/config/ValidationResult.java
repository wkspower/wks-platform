package com.wkspower.platform.domain.config;

import com.wkspower.platform.domain.config.model.CaseTypeConfig;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import com.wkspower.platform.domain.exception.ErrorDetail;
import java.util.List;
import java.util.Map;
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
 *
 * <p>Story 4.3 introduces {@link #mappingDefinition()} — the validated {@link MappingDefinition}
 * produced by Story 4.2's {@code MappingValidator} when the YAML carries an {@code attachments:}
 * block (or {@link MappingDefinition#empty()} when it does not). {@code ConfigService} threads this
 * value into {@code MappingRegistry} after a successful registry write so the runtime router (Story
 * 4.3) can resolve the active mapping by {@code (caseTypeId, version)}. The slot is optional —
 * older callers and validation-failure paths continue to construct without it.
 *
 * <p>Story 3.8 introduces {@link #responseMeta()} — optional metadata to include in the HTTP
 * response envelope's {@code meta} field. Currently used to carry the {@code blastRadius} report
 * when the blast-radius gate rejects a mutate-class deploy (AC2). Defaults to {@link Map#of()} for
 * all callers that don't supply it.
 */
public record ValidationResult(
    List<ErrorDetail> errors,
    List<ErrorDetail> warnings,
    Optional<CaseTypeConfig> config,
    Optional<MappingDefinition> mappingDefinition,
    Map<String, Object> responseMeta) {

  public ValidationResult {
    errors = List.copyOf(errors);
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
    mappingDefinition = mappingDefinition == null ? Optional.empty() : mappingDefinition;
    responseMeta = responseMeta == null ? Map.of() : Map.copyOf(responseMeta);
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

  /** Backward-compat 4-arg constructor — defaults {@link #responseMeta()} to empty map. */
  public ValidationResult(
      List<ErrorDetail> errors,
      List<ErrorDetail> warnings,
      Optional<CaseTypeConfig> config,
      Optional<MappingDefinition> mappingDefinition) {
    this(errors, warnings, config, mappingDefinition, null);
  }

  /** Backward-compat 3-arg constructor — defaults {@link #mappingDefinition()} and meta to empty. */
  public ValidationResult(
      List<ErrorDetail> errors, List<ErrorDetail> warnings, Optional<CaseTypeConfig> config) {
    this(errors, warnings, config, Optional.empty(), null);
  }

  public static ValidationResult ok(CaseTypeConfig config) {
    return new ValidationResult(List.of(), List.of(), Optional.of(config), Optional.empty());
  }

  public static ValidationResult ok(CaseTypeConfig config, List<ErrorDetail> warnings) {
    return new ValidationResult(List.of(), warnings, Optional.of(config), Optional.empty());
  }

  /**
   * Story 4.3 — variant carrying the validated {@link MappingDefinition} for registry threading.
   */
  public static ValidationResult ok(
      CaseTypeConfig config, List<ErrorDetail> warnings, MappingDefinition mappingDefinition) {
    return new ValidationResult(
        List.of(), warnings, Optional.of(config), Optional.ofNullable(mappingDefinition));
  }

  public static ValidationResult invalid(List<ErrorDetail> errors) {
    return new ValidationResult(errors, List.of(), Optional.empty(), Optional.empty());
  }

  /**
   * Story 3.8 — invalid result that also carries response-level metadata (e.g. {@code
   * blastRadius} report). The metadata is forwarded by the controller to {@link
   * com.wkspower.platform.domain.exception.WksConfigException#WksConfigException(List, Map)} so
   * the {@code GlobalExceptionHandler} can include it in {@code ApiResponse.meta}.
   */
  public static ValidationResult invalidWithMeta(
      List<ErrorDetail> errors, Map<String, Object> meta) {
    return new ValidationResult(errors, List.of(), Optional.empty(), Optional.empty(), meta);
  }

  public boolean isInvalid() {
    return !errors.isEmpty();
  }

  public boolean hasWarnings() {
    return !warnings.isEmpty();
  }
}
