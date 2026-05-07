package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * List-wrapper for the {@code forms:} block inside a CaseType YAML. Deserialized as a top-level
 * list ({@code forms: [...]}) via {@link JsonCreator}.
 *
 * <p>When a CaseType YAML omits the {@code forms:} key entirely, this slot is {@code null} on
 * {@link RawCaseTypeConfig} — backward-compat guaranteed by the null default on the {@code forms}
 * component.
 *
 * <p>Story 5.1 — schema-only; no runtime or persistence concern.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawFormConfig(@JsonProperty("definitions") List<RawFormDefinition> definitions) {

  /** Jackson DELEGATING creator — accepts the YAML list directly as {@code forms: [...]}. */
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static RawFormConfig fromList(List<RawFormDefinition> list) {
    return new RawFormConfig(list == null ? List.of() : list);
  }

  /** Null-safe accessor so callers never NPE on the inner list. */
  @Override
  public List<RawFormDefinition> definitions() {
    return definitions == null ? List.of() : definitions;
  }
}
