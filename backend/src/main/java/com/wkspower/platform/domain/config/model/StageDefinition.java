package com.wkspower.platform.domain.config.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
 *     - id: underwriting
 *       displayName: "Underwriting"
 *       statuses: [...]                          # Story 3.6 — stage-scoped status set
 *       initialStatus: pending-docs              # Story 3.6 — defaults to first declared
 * </pre>
 *
 * <p>Story 3.6 AC2 — adds two slots:
 *
 * <ul>
 *   <li>{@code statuses} — nullable. {@code null} means "key omitted; resolver falls back to flat
 *       case-type-level set". Empty list ({@code []} declared in YAML) is rejected with {@code
 *       WKS-STG-008}.
 *   <li>{@code initialStatus} — {@link Optional}, resolved at parse time to the explicit YAML value
 *       or the first declared status's id when omitted. Empty when {@code statuses} is also null
 *       (the flat fallback owns initial-status resolution in that case).
 * </ul>
 *
 * <p>{@code displayName} defaults to a Title-cased {@code id} when the YAML omits it (e.g. {@code
 * "loan-decision"} → {@code "Loan Decision"}). The {@code ordinal} is the 0-based position in the
 * declared list — never reordered after parse.
 *
 * @param id stage id (matches {@code [a-z][a-z0-9-]{0,62}}, not a reserved word)
 * @param displayName human-readable label, never blank
 * @param ordinal 0-based position in the declared {@code stages} list
 * @param statuses stage-scoped status list; {@code null} ⇒ "use flat fallback"
 * @param initialStatus resolved initial status id ({@code Optional.empty()} when {@code statuses}
 *     is {@code null})
 */
public record StageDefinition(
    String id,
    String displayName,
    int ordinal,
    List<StatusDefinition> statuses,
    Optional<String> initialStatus,
    /** Story 6.1 — optional archetype from the closed catalog. {@code null} means omitted. */
    String archetype) {

  public StageDefinition {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(displayName, "displayName");
    if (ordinal < 0) {
      throw new IllegalArgumentException("ordinal must be >= 0");
    }
    Objects.requireNonNull(initialStatus, "initialStatus");
    // Defensive copy — null distinguishes "omitted" (fall back to flat) from empty list (rejected
    // by validator with WKS-STG-008 before we ever get here).
    statuses = statuses == null ? null : List.copyOf(statuses);
  }

  /**
   * Backward-compat constructor for Story 3.1 callers (and tests) authored before Story 3.6
   * introduced stage-scoped statuses. Defaults both new slots to "absent" — equivalent to a YAML
   * stage with no {@code statuses:} or {@code initialStatus:} keys.
   */
  public StageDefinition(String id, String displayName, int ordinal) {
    this(id, displayName, ordinal, null, Optional.empty(), null);
  }

  /**
   * Backward-compat constructor for Story 3.6 callers that pre-date Story 6.1's {@code archetype}
   * slot. Defaults {@code archetype} to {@code null}.
   */
  public StageDefinition(
      String id,
      String displayName,
      int ordinal,
      List<StatusDefinition> statuses,
      Optional<String> initialStatus) {
    this(id, displayName, ordinal, statuses, initialStatus, null);
  }

  /**
   * Convenience: returns the stage-scoped status list as an {@link Optional}. {@link
   * Optional#empty()} means "fall back to the flat case-type-level set" (per Story 3.6 AC2).
   */
  public Optional<List<StatusDefinition>> statusesOpt() {
    return Optional.ofNullable(statuses);
  }
}
