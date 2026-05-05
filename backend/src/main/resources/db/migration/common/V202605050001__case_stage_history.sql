-- Story 3.1 — Stage Entity & Lifecycle (post-pivot Decision 19).
-- Append-only stage history table + denormalised cache columns on `cases`. The history table is
-- authoritative; `cases.current_stage_id` / `current_stage_ordinal` are rebuildable from it at any
-- time. H2 + Postgres compatible (no JSONB, no Postgres-specific syntax) — sits in
-- db/migration/common/ per Story 3.1 AC3.
--
-- Concurrency: AC8's optimistic-lock surface is a conditional UPDATE on (case_id, stage_id, state)
-- in StageRepositoryAdapter.appendTransition — no @Version column needed on this table. The
-- adapter pivots on the previous state; rowcount = 0 raises WKS-STG-003.
--
-- Zero-tenant invariant (D25) — Story 3.0 lint scans this file; no per-customer column appears.

CREATE TABLE case_stage_history (
    id           UUID                     PRIMARY KEY,
    case_id      UUID                     NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    stage_id     VARCHAR(64)              NOT NULL,
    ordinal      INTEGER                  NOT NULL,
    state        VARCHAR(16)              NOT NULL,
    entered_at   TIMESTAMP WITH TIME ZONE,
    exited_at    TIMESTAMP WITH TIME ZONE,
    source       VARCHAR(32),
    source_ref   VARCHAR(128),
    -- BaseJpaEntity contributes id / version / created_at / updated_at — keep the column shape
    -- in sync so Hibernate schema-validation (run by the IT lane) matches the JPA mapping.
    version      BIGINT                   NOT NULL DEFAULT 0,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    -- Natural-key uniqueness — `entered_at` is NULLable for PENDING / SKIPPED rows that never
    -- went active; the (case_id, stage_id) pair is unique within a case, so the simpler form
    -- below is sufficient and avoids surprises with NULL semantics across H2 / Postgres.
    UNIQUE (case_id, stage_id)
);

CREATE INDEX idx_case_stage_history_case_id    ON case_stage_history (case_id);
CREATE INDEX idx_case_stage_history_case_state ON case_stage_history (case_id, state);

ALTER TABLE cases ADD COLUMN current_stage_id      VARCHAR(64);
ALTER TABLE cases ADD COLUMN current_stage_ordinal INTEGER;
