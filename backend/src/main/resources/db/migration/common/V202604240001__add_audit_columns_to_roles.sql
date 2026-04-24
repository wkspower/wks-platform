-- Story 1.4 — BaseJpaEntity rollout.
-- Adds the shared audit columns (version, created_at, updated_at) to the roles table so every
-- @Entity extending BaseJpaEntity can boot under ddl-auto=validate. The users table already
-- declares these columns in V202604170001 — nothing to add there. ADD COLUMN IF NOT EXISTS keeps
-- the script idempotent and portable across H2 (>= 1.4.200) and PostgreSQL (>= 9.6). The
-- CURRENT_TIMESTAMP default fills any existing roles row with the migration-execution time
-- (there is no original-insertion timestamp to preserve — retroactive audit is best-effort);
-- version defaults to 0, matching @Version's initial state.

ALTER TABLE roles
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE roles
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE roles
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
