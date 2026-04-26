package com.wkspower.platform.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain record for a single business case. Plain Java — no Spring, no JPA, no Jackson annotations.
 * The ArchUnit rule {@code caseDomainHasNoSpringOrJpaImports} (Story 2.3 AC9) enforces this.
 *
 * <p>The {@code data} map carries dynamic fields whose ids come from the case-type YAML schema
 * (Story 2.1). The map is owned by the case — callers are expected to treat it as immutable; the
 * record's compact constructor makes a defensive copy.
 *
 * @param id case identifier (UUID)
 * @param caseTypeId case type id from YAML
 * @param caseTypeVersion snapshot of the case type version when the case was created
 * @param status current status id (initial value comes from {@code statuses[0].id} of the case
 *     type; later mutated by Story 2.4's BPMN execution listener)
 * @param assignee user id assigned to the case, or {@code null} when unassigned (queue)
 * @param data dynamic case data — keys are field ids declared in the case type's schema
 * @param processInstanceId BPMN engine-assigned process instance id; populated after engine start
 * @param createdAt creation timestamp
 * @param createdBy user id of the actor who created the case
 * @param updatedAt last-update timestamp
 * @param version optimistic-locking version (from {@code @Version} on the JPA row)
 */
public record Case(
    UUID id,
    String caseTypeId,
    int caseTypeVersion,
    String status,
    UUID assignee,
    Map<String, Object> data,
    String processInstanceId,
    Instant createdAt,
    UUID createdBy,
    Instant updatedAt,
    long version) {

  public Case {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(caseTypeId, "caseTypeId");
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(createdAt, "createdAt");
    Objects.requireNonNull(createdBy, "createdBy");
    Objects.requireNonNull(updatedAt, "updatedAt");
    Map<String, Object> source = data == null ? Map.of() : data;
    data = Collections.unmodifiableMap(new HashMap<>(source));
  }
}
