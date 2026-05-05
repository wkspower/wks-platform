package com.wkspower.platform.domain.config.model;

import java.util.Objects;

/**
 * A stage declared on a case-type YAML (Story 3.1 AC1). Plain Java — no Spring, no JPA, no Jackson
 * annotations.
 *
 * <p>Two YAML forms parse into this record:
 *
 * <pre>
 *   stages: [intake, underwriting, decision]   # string-list form (ordinal = list index)
 *   stages:
 *     - id: intake
 *       displayName: "Intake"                   # rich form (forward-compat for status-set / archetype slots)
 * </pre>
 *
 * <p>{@code displayName} defaults to a Title-cased {@code id} when the YAML omits it (e.g. {@code
 * "loan-decision"} → {@code "Loan Decision"}). The {@code ordinal} is the 0-based position in the
 * declared list — never reordered after parse.
 *
 * @param id stage id (matches {@code [a-z][a-z0-9-]{0,62}}, not a reserved word)
 * @param displayName human-readable label, never blank
 * @param ordinal 0-based position in the declared {@code stages} list
 */
public record StageDefinition(String id, String displayName, int ordinal) {

  public StageDefinition {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(displayName, "displayName");
    if (ordinal < 0) {
      throw new IllegalArgumentException("ordinal must be >= 0");
    }
  }
}
