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
}
