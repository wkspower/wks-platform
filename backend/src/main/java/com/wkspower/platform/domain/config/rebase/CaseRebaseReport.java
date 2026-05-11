package com.wkspower.platform.domain.config.rebase;

import java.util.List;
import java.util.UUID;

/**
 * Story 3.9 — structured report returned by both the dry-run (GET) and apply (POST) rebase
 * endpoints. When {@link #applied} is {@code false} the report is a preview only; when {@code true}
 * the mutation has already committed atomically.
 *
 * <p>Nested records are Jackson-serialisable by default (record component names as field names).
 * Wire shape is the public API contract — never rename a component without a version bump on the
 * enclosing type.
 */
public record CaseRebaseReport(
    UUID caseId,
    String caseTypeId,
    int fromVersion,
    int toVersion,
    boolean applied,
    List<FieldMapping> fieldMappings,
    List<StatusMapping> statusMappings,
    List<IrreconcilableItem> irreconcilable) {

  public CaseRebaseReport {
    fieldMappings = fieldMappings == null ? List.of() : List.copyOf(fieldMappings);
    statusMappings = statusMappings == null ? List.of() : List.copyOf(statusMappings);
    irreconcilable = irreconcilable == null ? List.of() : List.copyOf(irreconcilable);
  }

  /**
   * Per-field mapping action for the rebase diff surface.
   *
   * @param fieldId field identifier from the CaseType YAML schema
   * @param action what will happen to this field during rebase
   * @param fromType field type in the source version (null when field is new in toVersion)
   * @param toType field type in the target version (null when field is removed)
   * @param defaultApplied the default value that will be applied for DEFAULT actions; null
   *     otherwise
   */
  public record FieldMapping(
      String fieldId, FieldAction action, String fromType, String toType, Object defaultApplied) {}

  /**
   * Per-status mapping action for the rebase diff surface.
   *
   * @param statusId status identifier from the source version
   * @param action what will happen to this status during rebase
   * @param toStatusId the status id in the target version (null when action is not RENAME)
   */
  public record StatusMapping(String statusId, StatusAction action, String toStatusId) {}

  /**
   * An irreconcilable item that blocks the apply path (AC3). The apply is rejected atomically when
   * this list is non-empty; the operator must inspect and manually resolve before retrying.
   *
   * @param kind the kind of irreconcilability
   * @param fieldId field id for REMOVED_FIELD_WITH_DATA kind; null for STATUS_HAS_NO_EQUIVALENT
   * @param statusId status id for STATUS_HAS_NO_EQUIVALENT kind; null for REMOVED_FIELD_WITH_DATA
   * @param currentValue redacted representation of the field's current value (REMOVED_FIELD path)
   */
  public record IrreconcilableItem(
      IrreconcilableKind kind, String fieldId, String statusId, String currentValue) {}

  /** Action taken for a field during rebase. */
  public enum FieldAction {
    /** Field exists in both versions with compatible type — retain current value as-is. */
    KEEP,
    /** Field is new in the target version — apply the schema default (or null if none). */
    DEFAULT,
    /** Field is removed in the target version — drop the value (may trigger irreconcilable). */
    DROP,
    /**
     * Field type changed — the value may need manual transformation; surfaced in the report but
     * does NOT automatically block apply (only REMOVED_FIELD_WITH_DATA blocks apply in Phase-0).
     */
    MANUAL
  }

  /** Action taken for a status during rebase. */
  public enum StatusAction {
    /** Status exists by the same id in both versions — no mapping change needed. */
    KEEP,
    /**
     * Status is renamed in the target version (same semantic meaning, different id). Phase-0 does
     * not auto-detect renames; this value is reserved for future explicit rename declarations.
     */
    RENAME,
    /** Status is removed from the target version — operator must decide case disposition. */
    MANUAL
  }

  /** Discriminant for irreconcilable items. */
  public enum IrreconcilableKind {
    /** A field removed in the target version has non-null data on the case. */
    REMOVED_FIELD_WITH_DATA,
    /** The case's current status has no equivalent id in the target version. */
    STATUS_HAS_NO_EQUIVALENT,
    /**
     * Story 3.9 review remediation — the case currently sits on a stage that exists in the source
     * version but is removed (or renamed) in the target version. Phase-0 blocks the apply; Story
     * 3-9.1 will introduce operator-supplied stage remap JSON. The {@code currentValue} carries the
     * dangling stage id so the operator can identify the case's pinned stage in the report.
     */
    STAGE_REMOVED_WITH_ACTIVE_CASE
  }
}
