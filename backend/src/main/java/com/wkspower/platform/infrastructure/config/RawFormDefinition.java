package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Transport-shaped mirror of one form definition inside a CaseType YAML {@code forms[]} entry.
 * Captures the three-axis vocabulary (AC1):
 *
 * <ul>
 *   <li>{@code topology} — structural shape of the form session (Phase-0: {@code single})
 *   <li>{@code dataModel} — how form data is partitioned ({@code monolithic | sectioned})
 *   <li>{@code rendering} — how the UI renders the form ({@code single-page | multi-section})
 * </ul>
 *
 * <p>Unknown properties are silently ignored via {@link JsonIgnoreProperties} for forward-compat
 * with future axis additions (mirror of {@link RawCaseTypeConfig} pattern).
 *
 * <p>Story 5.1 — schema-only; no runtime or persistence concern.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawFormDefinition(
    @JsonProperty("id") String id,
    @JsonProperty("topology") String topology,
    @JsonProperty("dataModel") String dataModel,
    @JsonProperty("rendering") String rendering,
    @JsonProperty("fields") List<Map<String, Object>> fields,
    /**
     * Story 5.3 — sections declared for {@code dataModel: sectioned} forms. Absent (null) for
     * monolithic forms; validated by {@link FormValidator} when {@code dataModel} is {@code
     * sectioned}.
     */
    @JsonProperty("sections") List<RawFormSection> sections,
    /**
     * Story 6.1 — optional archetype from the closed catalog ({@code draft_section}, {@code
     * submit_for_processing}, {@code business_final}). Validated by {@link ConfigValidator} which
     * emits {@code WKS-ARCH-001} for unknown values.
     */
    @JsonProperty("archetype") String archetype) {

  /**
   * Backward-compat constructor for callers (mostly tests) that pre-date Story 5.3's {@code
   * sections} slot. Defaults {@code sections} and {@code archetype} to {@code null}.
   */
  public RawFormDefinition(
      String id,
      String topology,
      String dataModel,
      String rendering,
      List<Map<String, Object>> fields) {
    this(id, topology, dataModel, rendering, fields, null, null);
  }

  /**
   * Backward-compat constructor for callers that pre-date Story 6.1's {@code archetype} slot.
   * Defaults {@code archetype} to {@code null}.
   */
  public RawFormDefinition(
      String id,
      String topology,
      String dataModel,
      String rendering,
      List<Map<String, Object>> fields,
      List<RawFormSection> sections) {
    this(id, topology, dataModel, rendering, fields, sections, null);
  }
}
