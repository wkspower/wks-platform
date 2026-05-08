package com.wkspower.platform.domain.config.diff;

/**
 * Discriminant for a single change unit inside a {@link BlastRadiusReport}. Mutate-class kinds
 * (the ones that require a CaseType version bump per Decision 20) are grouped first; append-class
 * kinds follow.
 *
 * <p><b>Phase-0 scope (Story 3.8):</b> field option-list edits, role-permissions edits,
 * listColumns reordering, and displayName-only changes are intentionally NOT represented here.
 * {@link com.wkspower.platform.domain.config.diff.CaseTypeDiff} classifies these as append-class
 * (no false-positive mutate). Phase-1 may add new kinds to tighten any of these.
 *
 * <p><b>Wire contract:</b> these enum names are serialised by Jackson as-is into the {@code
 * meta.blastRadius} payload (see {@link BlastRadiusReport} javadoc). Never rename an existing
 * constant — that is a breaking wire change.
 */
public enum DeltaKind {

  // ---- mutate-class -------------------------------------------------------

  /** A status present in prev is absent in next (any scope). */
  STATUS_REMOVED,

  /** A status was moved from one stage's scope to a different stage's scope. */
  STATUS_RETARGETED,

  /** The {@code terminal} flag on a status (same id, same stage scope) was changed. */
  STATUS_TERMINAL_FLIP,

  /** A field present in prev is absent in next. */
  FIELD_REMOVED,

  /** A field's {@code type} changed (e.g. TEXT → NUMBER). */
  FIELD_RETYPED,

  /** A field's {@code required} flag changed (same id). */
  FIELD_REQUIRED_FLIPPED,

  /**
   * A stage was removed, or two or more stages were reordered in a non-tail-append pattern. Covers
   * both pure removal and reordering that alters ordinal positions of existing stages.
   */
  STAGE_REORDERED,

  /**
   * A new stage was inserted in the middle of the existing stage list (i.e. the new stage does not
   * appear at the tail). Insertion in the middle shifts ordinals of existing stages which is a
   * mutate-class change because in-flight cases carry a pinned stage ordinal.
   */
  STAGE_INSERTED_MIDDLE,

  /**
   * The mapping layer ({@link com.wkspower.platform.domain.config.diff.MappingDiff}) returned
   * {@code MUTATE_CLASS}. Folded into this report's {@code mutateDeltas} list.
   */
  MAPPING,

  // ---- append-class -------------------------------------------------------

  /** A new status was added (case-type level or stage-scoped). */
  STATUS_ADDED,

  /** A new field was added (regardless of {@code required} value). */
  FIELD_ADDED,

  /** A new stage was appended at the tail of the stage list (safe — no ordinal shift). */
  STAGE_APPENDED,

  /** A new role was added. */
  ROLE_ADDED,

  /** A new form was added. */
  FORM_ADDED,

  /**
   * The mapping layer returned {@code APPEND_CLASS}. Folded into {@code appendDeltas}. Only
   * emitted when the mapping changed at all (empty-to-empty produces no delta).
   */
  MAPPING_APPEND
}
