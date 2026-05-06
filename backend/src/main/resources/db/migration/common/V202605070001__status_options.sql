-- Story 3.7 — Append-Class Status Add (Live, with Lazy Propagation).
-- Stores the per-(case_type_id, version, stage_id) status options edited via the admin REST API
-- AFTER initial deploy. This is the durable backing store for Decision 21's "live append" promise:
-- the deploy-time YAML defines the seed status set; runtime appends/renames land here without a
-- CaseType version bump, and case reads overlay these rows on the frozen-on-version base.
--
-- The version column is BOUND to the row in case_type_versions (Story 3.4). Append-class writes
-- always target the current version per CaseTypeVersionRegistry — never bumping it. A future
-- mutate-class flow (Story 3.8) would write rows for a freshly-bumped version under its own
-- envelope.
--
-- stage_id sentinel: '__flat__' represents case-type-level (non-stage-scoped) statuses — Story 3.6's
-- flat fallback. Using a sentinel rather than NULL keeps the primary key non-nullable and lets the
-- standard composite-PK lookup (case_type_id, version, stage_id, status_id) hit the index uniformly
-- regardless of whether the stage is declared.
--
-- H2 + Postgres compatible — VARCHAR / INTEGER / BOOLEAN are portable; no JSONB, no GENERATED, no
-- Postgres-specific syntax. Zero-tenant invariant (D25, Story 3.0 lint) — no per-customer column.
--
-- Wire codes referenced by writers:
--   WKS-STG-007 — duplicate (case_type_id, version, stage_id, status_id) on append (HTTP 409)
--   WKS-STG-009 — mutate-class rejection: DELETE + PATCH terminal (HTTP 405)
--   WKS-STG-012 — unknown caseTypeId/stageId on admin path (HTTP 404)
--   WKS-STG-013 — unknown statusId on PATCH rename (HTTP 404)

CREATE TABLE status_options (
    case_type_id      VARCHAR(64)  NOT NULL,
    version           INTEGER      NOT NULL,
    stage_id          VARCHAR(64)  NOT NULL,    -- '__flat__' sentinel for case-type-level statuses
    status_id         VARCHAR(64)  NOT NULL,
    display_name      VARCHAR(128) NOT NULL,
    color             VARCHAR(32)  NOT NULL,
    terminal          BOOLEAN      NOT NULL DEFAULT FALSE,
    ordinal           INTEGER      NOT NULL,
    PRIMARY KEY (case_type_id, version, stage_id, status_id)
);

CREATE INDEX idx_status_options_case_type_stage
    ON status_options (case_type_id, version, stage_id, ordinal);
