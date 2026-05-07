-- Story 4.5 AC3 — Deployment fingerprint columns on case_type_versions (Decision 22).
-- bpmn_content_hash: SHA-256 hex of raw BPMN bytes at deploy time. NULL for zero-attachment deploys.
-- mapping_hash: SHA-256 hex of canonical MappingDefinition string representation. NULL for zero-attachment deploys.
-- Both are forensic and integrity columns only — not used as cache keys or routing keys.
-- Zero-tenant invariant (D25) preserved — no per-client column added.
ALTER TABLE case_type_versions ADD COLUMN bpmn_content_hash VARCHAR(64) NULL;
ALTER TABLE case_type_versions ADD COLUMN mapping_hash      VARCHAR(64) NULL;
