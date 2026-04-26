-- Story 2.3 — Case CRUD operations & domain model.
-- Creates the `cases` table that the Case domain record persists to. The `data` column carries
-- dynamic case-data fields whose ids come from the case-type YAML schema (Story 2.1) — Hibernate 6
-- maps it via @JdbcTypeCode(SqlTypes.JSON), which resolves to JSON natively on H2 and to a
-- starting JSON column on Postgres (later upgraded to JSONB by V202604260002 in postgresql/).
-- All identifiers lowercase; TIMESTAMP WITH TIME ZONE matches the users / roles convention from
-- 1.2. No GIN indexes here — server-side JSON queries are Phase 1 (architecture.md §Decision 4).

CREATE TABLE cases (
    id                  UUID                     PRIMARY KEY,
    case_type_id        VARCHAR(64)              NOT NULL,
    case_type_version   INTEGER                  NOT NULL,
    status              VARCHAR(64)              NOT NULL,
    assignee            UUID                     REFERENCES users(id) ON DELETE SET NULL,
    data                JSON                     NOT NULL DEFAULT '{}',
    process_instance_id VARCHAR(64),
    created_by          UUID                     NOT NULL REFERENCES users(id),
    -- created_by FK uses default ON DELETE NO ACTION: blocking user-delete preserves audit trail.
    version             BIGINT                   NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_cases_case_type_id ON cases (case_type_id);
CREATE INDEX idx_cases_status       ON cases (status);
CREATE INDEX idx_cases_assignee     ON cases (assignee);
CREATE INDEX idx_cases_updated_at   ON cases (updated_at DESC);
