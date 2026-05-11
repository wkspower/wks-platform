/**
 * 1:1 mirror of the backend `CaseTypeViewDto` (Story 2.3 → exposed by Story 2.5 on
 * `GET /api/case-types/{id}`, widened in Story 2.7). The frontend never invents derived fields
 * here — derived state (e.g. resolved status colors, accessor functions) lives in `lib/`.
 *
 * Story 2.7 widens `FieldDefinition` to the flattened `FieldView` wire shape: per-type validation
 * slots (`minLength`, `maxLength`, `min`, `max`, `step`, etc.) are top-level + nullable, and
 * `requiredOnCreate` controls whether the create-form dialog asks for the field at case creation.
 *
 * `CaseTypeSummary` gains `permissions: string[]` — the verbs the caller holds on this case-type,
 * used to filter the Create-Case selector dropdown without an extra round-trip.
 */

import type { FieldType } from './fieldType';
import type { StatusColor } from './statusColor';

export interface FieldOption {
  value: string;
  label: string;
}

export interface FieldDefinition {
  id: string;
  displayName: string;
  type: FieldType;
  required: boolean;
  /**
   * Story 2.7 — controls whether the create-form dialog asks for this field. Optional in TS for
   * backward-compat with pre-2.7 fixtures; the wire shape always includes it.
   */
  requiredOnCreate?: boolean;
  order: number;
  options: FieldOption[];
  // Per-type validation slots — only the slots relevant to `type` are populated.
  minLength?: number | null;
  maxLength?: number | null;
  min?: number | null;
  max?: number | null;
  step?: number | null;
  dateMin?: string | null;
  dateMax?: string | null;
  maxBytes?: number | null;
  allowedMimeTypes?: string[] | null;
  /**
   * Story 5.6 AC1 — role-id allow-list for write authorization. Wire entries follow the
   * {@code role:<id>} format. Empty/undefined means "no editableBy declared" — the case-type's
   * {@code defaultFieldEditability} (AC4) decides the runtime default.
   */
  editableBy?: string[];
}

export interface StatusDefinition {
  id: string;
  displayName: string;
  color: StatusColor;
}

export interface CaseTypeSummary {
  id: string;
  displayName: string;
  version: number;
  statusCount: number;
  fieldCount: number;
  /**
   * Story 2.7 — verbs the caller holds on this case-type. Subset of declared role verbs.
   * Optional in TS for backward-compat with pre-2.7 fixtures; the wire shape always includes it.
   */
  permissions?: string[];
}

/**
 * Story 3.3 — one entry per stage declared in the YAML, in declared order. The frontend reads
 * `stages.length` to gate the `StageTimeline` component (zero-stage CaseTypes do not render the
 * timeline at all per AC2 / Decision 19). `displayName` mirrors the YAML — Title-cased fallback
 * applied server-side per Story 3.1 AC1, so the frontend never derives display names client-side.
 */
export interface StageDefinitionView {
  id: string;
  displayName: string;
  ordinal: number;
  /** Story 6.1 — optional archetype from the closed catalog. */
  archetype?: string | null;
}

/**
 * Story 5.3 — one section in a {@code dataModel: sectioned} form definition. Groups a set of
 * fields under a labelled expandable panel rendered by {@code MultiSectionFormRenderer}.
 */
export interface FormSectionView {
  id: string;
  label: string;
  fields: FieldDefinition[];
}

/**
 * Story 5.2 — wire-shape projection of one form definition from the case-type YAML {@code forms[]}
 * block. Carries the three-axis vocabulary and the form's field list so the frontend renderer has
 * everything it needs from the {@code GET /api/case-types/{id}} response without a second
 * round-trip.
 *
 * Story 5.3 — adds {@code sections?} for {@code dataModel: sectioned} forms.
 */
export interface FormDefinitionView {
  id: string;
  topology: string;
  dataModel: string;
  rendering: string;
  fields: FieldDefinition[];
  /** Story 5.3 — sections declared for {@code dataModel: sectioned} forms. */
  sections?: FormSectionView[];
  /** Story 6.1 — optional archetype from the closed catalog. */
  archetype?: string | null;
}

export interface CaseTypeView {
  id: string;
  displayName: string;
  version: number;
  fields: FieldDefinition[];
  statuses: StatusDefinition[];
  listColumns: string[];
  /**
   * Story 3.3 — the declared stage schema (display names, ordinals). Optional in TS for
   * backward-compat with pre-3.3 fixtures; the wire shape always includes it. The
   * `StageTimeline` component treats `undefined` and `[]` identically (component returns null).
   */
  stages?: StageDefinitionView[];
  /**
   * Story 5.2 — form definitions declared in the YAML {@code forms[]} block. Optional for
   * backward-compat with pre-5.2 fixtures; the wire shape always includes it. Empty array when
   * no forms block is declared.
   */
  forms?: FormDefinitionView[];
  /**
   * Story 5.6 AC4 — top-level case-type setting controlling default field editability. Wire
   * values: {@code editable-by-default} (default — preserves pre-5.6 behavior) or
   * {@code locked-by-default}. Optional for backward-compat; absence means default.
   */
  defaultFieldEditability?: 'editable-by-default' | 'locked-by-default' | null;
  /**
   * Story 5.6 AC3 — declared roles surface (id + displayName) used to translate role ids to
   * display names in disabled-field tooltips. Server may omit (current backend does not echo
   * roles); when absent, tooltips fall back to the role id. Optional.
   */
  roles?: { id: string; displayName?: string }[];
  /**
   * Story 6.2 AC1 — outcome-key → stageTransition map resolved from the first attachment's
   * outcomeMappings block. Present only when the case type declares outcome mappings in its YAML.
   * When present and non-empty, the task completion UI renders a multi-button outcome picker
   * instead of the single "Complete task" CTA. Absent/empty → single-CTA path (no regression).
   */
  outcomeMappings?: Record<string, string>;
}
