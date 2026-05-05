/**
 * Mirrors the backend `CaseSummaryDto` from Story 2.3 (`GET /api/cases`). The frontend table row
 * type (`CaseRow`) extends the wire shape with two derived fields plumbed from elsewhere:
 *
 * - `hasUnreadActivity` — Phase 0 always `false`; lit by Story 4.4 (sidebar badges) once the
 *   SSE bridge from Story 4.3 is in place. The visual `border-l-3 border-primary` row treatment
 *   ships now so the rendering path doesn't have to change when the feature flag flips.
 * - `slaBreached` — Phase 0 always `false`; SLA semantics arrive in Phase 1. The default-sort
 *   composite already tiers on this so the comparator does not branch on Phase 0 vs Phase 1.
 */

export interface CaseSummary {
  id: string;
  caseTypeId: string;
  status: string;
  assignee: string | null;
  createdAt: string;
  updatedAt: string;
  fields: Record<string, unknown>;
}

import type { CaseTypeView } from './caseType';

/**
 * Story 3.3 — wire enum for `StageView.state`. UPPERCASE strings, mirroring how `Status` is
 * serialised today via `StatusDefinition.id`. Frontend maps to a lowercase CSS-class-name
 * client-side. Renaming any of these is a multi-PR cascade — they are a wire contract per
 * `feedback_error_codes_are_wire_contract.md`.
 */
export type StageState = 'PENDING' | 'ACTIVE' | 'COMPLETED' | 'SKIPPED';

/**
 * Story 3.3 — one entry per stage declared on the bound CaseType@version. Skipped stages stay in
 * the list at their declared ordinal; the list is always ordered by `ordinal` ASC. Backend is the
 * sole source of truth for `state` — the frontend never infers `ACTIVE` or `SKIPPED` from any
 * other field.
 */
export interface StageView {
  stageId: string;
  displayName: string;
  ordinal: number;
  state: StageState;
  /** ISO-8601 wire format. `null` for `PENDING`, and for `SKIPPED` rows that never went active. */
  enteredAt: string | null;
  /** ISO-8601 wire format. `null` while `PENDING` or `ACTIVE`. */
  exitedAt: string | null;
  source: 'wks-auto-rule' | 'manual' | 'backend-signal' | null;
  sourceRef: string | null;
}

export interface CaseDto {
  id: string;
  caseTypeId: string;
  caseTypeVersion: number;
  status: string;
  assignee: string | null;
  data: Record<string, unknown>;
  processInstanceId: string | null;
  documentCount: number;
  createdAt: string;
  createdBy: string | null;
  updatedAt: string;
  version: number;
  caseType: CaseTypeView;
  /**
   * Story 3.3 — full stage history for the timeline UI. Empty for zero-stage CaseTypes; never
   * null. Owned by Story 3.3. The two scalar fields `currentStageId` / `currentStageOrdinal` are
   * owned by Story 3.2 and added under that PR per the locked Sprint 2 split.
   */
  stages: StageView[];
}

export interface CaseRow extends CaseSummary {
  hasUnreadActivity: boolean;
  slaBreached: boolean;
}

export function toCaseRow(summary: CaseSummary): CaseRow {
  return {
    ...summary,
    hasUnreadActivity: false,
    slaBreached: false,
  };
}
