package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Transport-shaped mirror of the case-type YAML tree. Every field is a boxed nullable so the
 * validator can observe missing required keys and report them as {@code WKS-CFG-001}. Unknown
 * properties are silently ignored for forward-compatibility — new slots added in future stories
 * must not break older YAML files.
 *
 * <p>This record lives in the infrastructure layer on purpose. Domain records ({@link
 * com.wkspower.platform.domain.config.model.CaseTypeConfig}) are built by a mapper only after
 * validation succeeds.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawCaseTypeConfig(
    String id,
    String displayName,
    Integer version,
    String description,
    RawWorkflow workflow,
    List<RawField> fields,
    List<RawStatus> statuses,
    List<String> listColumns,
    List<RawRole> roles,
    List<RawStage> stages) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RawWorkflow(String bpmn) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RawField(
      String id,
      String displayName,
      String type,
      Boolean required,
      Boolean requiredOnCreate,
      Integer order,
      List<RawOption> options,
      Integer minLength,
      Integer maxLength,
      Double min,
      Double max,
      Double step,
      String dateMin,
      String dateMax,
      Long maxBytes,
      List<String> allowedMimeTypes) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RawOption(String label, String value) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RawStatus(String id, String displayName, String color) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RawRole(String name, List<String> permissions) {}

  /**
   * Story 3.1 — supports both YAML forms:
   *
   * <pre>
   *   stages: [intake, underwriting]            # string-list form (each element parses via fromString)
   *   stages:
   *     - id: intake
   *       displayName: "Intake"                  # rich-object form
   * </pre>
   *
   * Jackson dispatches via {@link JsonCreator} on the matching type — string scalars hit {@link
   * #fromString} and produce a {@code RawStage(id, null)}; mappings hit {@link #fromObject} via
   * Jackson's default record deserialization.
   */
  /**
   * Story 3.1 — wrapper record that supports BOTH YAML forms in a single list:
   *
   * <pre>
   *   stages: [intake, underwriting]            # bare-string form
   *   stages:
   *     - id: intake
   *       displayName: "Intake"                  # rich-object form
   * </pre>
   *
   * <p>The string form lands via {@link #fromString} (single-arg DELEGATING creator); the object
   * form lands via {@link #fromObject} (PROPERTIES creator). Both produce the same record shape so
   * the validator can iterate uniformly.
   */
  public record RawStage(String id, String displayName) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RawStage fromString(String id) {
      return new RawStage(id, null);
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public static RawStage fromObject(
        @com.fasterxml.jackson.annotation.JsonProperty("id") String id,
        @com.fasterxml.jackson.annotation.JsonProperty("displayName") String displayName) {
      return new RawStage(id, displayName);
    }
  }
}
