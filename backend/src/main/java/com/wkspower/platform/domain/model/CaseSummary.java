package com.wkspower.platform.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Lightweight projection of a case for list views. Carries only the fields the case-type's {@code
 * listColumns} declares (in {@code fields}) plus the system columns. The full {@code data} JSON
 * column is intentionally omitted — list-path SQL never fetches it (Story 2.3 AC7).
 *
 * @param id case id
 * @param caseTypeId case type id
 * @param status current status id
 * @param assignee assignee user id, or {@code null}
 * @param createdAt creation timestamp
 * @param updatedAt last-update timestamp
 * @param fields field-level projections — only the YAML-declared {@code listColumns} field values
 */
public record CaseSummary(
    UUID id,
    String caseTypeId,
    String status,
    UUID assignee,
    Instant createdAt,
    Instant updatedAt,
    Map<String, Object> fields) {

  public CaseSummary {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(caseTypeId, "caseTypeId");
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(createdAt, "createdAt");
    Objects.requireNonNull(updatedAt, "updatedAt");
    fields = Map.copyOf(Objects.requireNonNullElseGet(fields, Map::of));
  }
}
