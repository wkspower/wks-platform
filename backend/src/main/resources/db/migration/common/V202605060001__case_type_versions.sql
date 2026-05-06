-- Story 3.4 — CaseType Version Registry & Bind-on-Create (Decision 20).
-- Append-only registry of CaseType deployments. Every config-deploy writes one row; byte-canonical
-- re-deploys are idempotent (short-circuited in the adapter and defended by the unique-by-hash
-- index below). In-flight cases bind to (case_type_id, version) at create time and read their
-- schema from this table for the lifetime of the case — frozen-on-version per Decision 20.
--
-- H2 + Postgres compatible — TIMESTAMP WITH TIME ZONE / TEXT / VARCHAR / INTEGER / CHAR are all
-- portable. No JSONB, no BYTEA, no Postgres-specific syntax. The author YAML is stored as TEXT
-- (raw bytes UTF-8-decoded) so operators can `SELECT definition_yaml FROM case_type_versions
-- WHERE …` in psql / H2 console and recover what the author wrote.
--
-- Zero-tenant invariant (D25) — Story 3.0 lint scans this file; no per-customer column appears.
-- Versions identify config, not customers: a CaseType row in this table is the same shape across
-- every per-client database.

CREATE TABLE case_type_versions (
    case_type_id      VARCHAR(64)              NOT NULL,
    version           INTEGER                  NOT NULL,
    definition_hash   VARCHAR(64)              NOT NULL,            -- SHA-256 hex over canonical YAML
    definition_yaml   TEXT                     NOT NULL,            -- raw author-supplied YAML
    published_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    published_by      VARCHAR(128)             NOT NULL,            -- actorId or "system:startup"
    PRIMARY KEY (case_type_id, version),
    CONSTRAINT case_type_versions_version_positive
        CHECK (version >= 1)
);

-- Defence-in-depth idempotence: the adapter short-circuits on hash match before insert, but the
-- DB enforces "no two rows for the same (id, hash)" so a concurrent first-deploy race cannot
-- produce duplicate rows.
CREATE UNIQUE INDEX idx_case_type_versions_id_hash
    ON case_type_versions (case_type_id, definition_hash);
