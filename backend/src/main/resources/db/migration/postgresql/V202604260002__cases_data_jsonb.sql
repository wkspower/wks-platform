-- Story 2.3 (D6 patch) — upgrade `cases.data` from JSON to JSONB on Postgres only.
-- Lives in `postgresql/` so the Flyway dialect-segregated layout runs it only under the production
-- profile (application-production.yml). The H2 profile loads `common/+h2/` and never sees this file.
ALTER TABLE cases ALTER COLUMN data TYPE JSONB USING data::jsonb;
