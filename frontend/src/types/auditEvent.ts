/**
 * Story 9-2 — wire types for {@code GET /api/cases/{caseId}/audit-events}.
 *
 * The backend serialises the sealed {@code AuditSource} interface (4 variants) as a discriminated
 * union with a {@code type} tag + a variant-shape {@code payload}. The discriminator strings
 * mirror the {@code audit_events.source_type} column 1:1 — see {@code AuditEventMapper.java}.
 *
 * Frontend code discriminates on {@code source.type} and renders the variant per Story 9-2
 * §Design Decision 3. The four payload keys are:
 *   - USER               → { actorId: UUID }
 *   - AUTO_RULE          → { ruleId: string }
 *   - BACKEND            → { adapterName: string }
 *   - EXECUTION_UNMAPPED → { originAdapter: string }
 */

export type AuditSourceView =
  | { type: 'USER'; payload: { actorId: string } }
  | { type: 'AUTO_RULE'; payload: { ruleId: string } }
  | { type: 'BACKEND'; payload: { adapterName: string } }
  | { type: 'EXECUTION_UNMAPPED'; payload: { originAdapter: string } };

/**
 * Story 9-2 AC2 — wire shape for one persisted {@code audit_events} row. Nullable fields
 * ({@code fieldId} / {@code openTaskId} / {@code formId}) mirror the persistence shape: only
 * populated on {@code case.data.edit} rows; future audit event types may leave them null.
 */
export interface AuditEventView {
  id: string;
  eventType: string;
  source: AuditSourceView;
  /** APPLIED | BLOCKED | REJECTED (free-form at this layer per 9-3 domain model). */
  result: string;
  fieldId: string | null;
  openTaskId: string | null;
  formId: string | null;
  /** ISO-8601 instant. */
  occurredAt: string;
}

/** Story 9-2 AC1 — wire shape returned by the endpoint. */
export interface AuditEventList {
  items: AuditEventView[];
  truncated: boolean;
}
