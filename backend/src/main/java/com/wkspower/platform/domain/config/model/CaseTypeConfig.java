package com.wkspower.platform.domain.config.model;

import java.util.List;
import java.util.Optional;

/**
 * Validated, immutable case-type configuration. Every collection is defensively copied so the
 * registry cannot be mutated through a reference leaked to callers. Only {@code description} is
 * optional.
 *
 * <p>{@code stages} is the post-pivot foundational primitive (Story 3.1 / Decision 19). When the
 * YAML omits {@code stages:} or declares an empty list, this field is {@link List#of()} and every
 * downstream service treats the absence as a no-op (Decision 19: "stage-less paths must remain
 * unbranched").
 */
public record CaseTypeConfig(
    String id,
    String displayName,
    int version,
    String description,
    WorkflowRef workflow,
    List<FieldDefinition> fields,
    List<StatusDefinition> statuses,
    List<String> listColumns,
    List<RoleDefinition> roles,
    List<StageDefinition> stages) {

  /**
   * Backward-compat constructor for callers (and tests) that predate Story 3.1's {@code stages}
   * slot — defaults to {@link List#of()}.
   */
  public CaseTypeConfig(
      String id,
      String displayName,
      int version,
      String description,
      WorkflowRef workflow,
      List<FieldDefinition> fields,
      List<StatusDefinition> statuses,
      List<String> listColumns,
      List<RoleDefinition> roles) {
    this(
        id,
        displayName,
        version,
        description,
        workflow,
        fields,
        statuses,
        listColumns,
        roles,
        List.of());
  }

  public CaseTypeConfig {
    fields = List.copyOf(fields);
    statuses = List.copyOf(statuses);
    listColumns = List.copyOf(listColumns);
    roles = List.copyOf(roles);
    stages = stages == null ? List.of() : List.copyOf(stages);
  }

  /** Convenience lookup used by the JSON Schema generator and (future) case-data validation. */
  public Optional<FieldDefinition> field(String fieldId) {
    return fields.stream().filter(f -> f.id().equals(fieldId)).findFirst();
  }
}
