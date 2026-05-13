-- Story 9-3 — append-only audit_events table.
--
-- Persistence backing for the existing CaseDataEdited AFTER_COMMIT audit channel
-- (Story 6.3 AC-6). The slf4j log line emitted by EditAuditEmitter remains the
-- SI-runbook grep wire contract; this table is enhancement, not replacement.
--
-- Schema notes:
--  - event_type is a discriminator column so future audit surfaces
--    (RebaseAuditListener, lifecycle events) can fold in via natural-toucher
--    per feedback_fold_debt_into_stories (single audit_events table, not one
--    table per event class).
--  - source_type + source_payload encode the sealed AuditSource interface:
--    source_type matches the permit list verbatim (USER / AUTO_RULE / BACKEND /
--    EXECUTION_UNMAPPED) per feedback_error_codes_are_wire_contract (stable
--    wire strings, do NOT rename). source_payload carries variant-specific
--    fields as JSON so adding fields to one variant does not require a new
--    Flyway slot.
--  - result is BOUND to CaseDataEdited.Result (APPLIED / BLOCKED / REJECTED);
--    when other event_types land they may use other result strings — column is
--    deliberately VARCHAR not an ENUM.
--  - FK on case_id is ON DELETE RESTRICT (never CASCADE) — deleting a case must
--    never silently erase its audit trail. If a case-delete path trips on this
--    FK it is a pre-existing bug surfaced, not 9-3 scope creep.
--  - Append-only invariant is enforced primarily at the Java surface (no
--    save/update/delete methods in AuditEventRepository). DB-level revokes
--    (DENY UPDATE/DELETE to app role) are Phase-1 deepening, not Sprint 12.
--  - JSON column type is portable (H2 2.x + Postgres both support it). Postgres
--    upgrade JSON→JSONB lives in postgresql/V202605140001a__audit_events_jsonb.sql
--    so production gets the indexable JSONB without forcing H2 to learn JSONB
--    syntax. Same split pattern as cases.data (V202604260001 + V202604260002).

CREATE TABLE audit_events (
    id              UUID                     PRIMARY KEY,
    case_id         UUID                     NOT NULL REFERENCES cases(id) ON DELETE RESTRICT,
    event_type      VARCHAR(64)              NOT NULL,
    source_type     VARCHAR(32)              NOT NULL,
    source_payload  JSON                     NOT NULL,
    result          VARCHAR(16)              NOT NULL,
    field_id        VARCHAR(128),
    open_task_id    VARCHAR(128),
    form_id         VARCHAR(128),
    occurred_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Feed read path (Story 9-2): "give me the last N audit rows for case X, newest first".
CREATE INDEX idx_audit_events_case_id_occurred_at
    ON audit_events (case_id, occurred_at DESC);
