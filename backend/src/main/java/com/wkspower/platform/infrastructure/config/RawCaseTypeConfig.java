package com.wkspower.platform.infrastructure.config;

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
    List<RawRole> roles) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RawWorkflow(String bpmn) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record RawField(
      String id,
      String displayName,
      String type,
      Boolean required,
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
}
