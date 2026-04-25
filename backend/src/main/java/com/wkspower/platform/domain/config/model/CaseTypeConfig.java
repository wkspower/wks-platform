package com.wkspower.platform.domain.config.model;

import java.util.List;
import java.util.Optional;

/**
 * Validated, immutable case-type configuration. Every collection is defensively copied so the
 * registry cannot be mutated through a reference leaked to callers. Only {@code description} is
 * optional.
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
    List<RoleDefinition> roles) {

  public CaseTypeConfig {
    fields = List.copyOf(fields);
    statuses = List.copyOf(statuses);
    listColumns = List.copyOf(listColumns);
    roles = List.copyOf(roles);
  }

  /** Convenience lookup used by the JSON Schema generator and (future) case-data validation. */
  public Optional<FieldDefinition> field(String fieldId) {
    return fields.stream().filter(f -> f.id().equals(fieldId)).findFirst();
  }
}
