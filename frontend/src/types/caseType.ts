/**
 * 1:1 mirror of the backend `CaseTypeViewDto` (Story 2.3 → exposed by Story 2.5 on
 * `GET /api/case-types/{id}`). The frontend never invents derived fields here — derived state
 * (e.g. resolved status colors, accessor functions) lives in `lib/`.
 *
 * The `CaseTypeSummary` mirrors `CaseTypeSummaryDto` for the list endpoint.
 */

import type { FieldType } from './fieldType';
import type { StatusColor } from './statusColor';

export interface FieldOption {
  value: string;
  label: string;
}

export interface FieldTypeSlots {
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

export interface FieldDefinition {
  id: string;
  displayName: string;
  type: FieldType;
  required: boolean;
  order: number;
  options: FieldOption[];
  slots: FieldTypeSlots | null;
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
}

export interface CaseTypeView {
  id: string;
  displayName: string;
  version: number;
  fields: FieldDefinition[];
  statuses: StatusDefinition[];
  listColumns: string[];
}
