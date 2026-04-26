-- Story 2.4 (folded debt #1) — Persistent case-type → process-definition mapping.
-- Solves the cold-start window where ProcessDefinitionKeyCache (in-memory only, populated by the
-- ConfigDeployed event stream) lost the mapping after a JVM restart for a case-type that was
-- admin-deployed via HTTP without a YAML file on disk. With this table the cache becomes a
-- write-through layer over a durable mapping, so POST /api/cases never 500s on cold start for
-- admin-deployed case types.

CREATE TABLE case_type_deployments (
    case_type_id           VARCHAR(64)              PRIMARY KEY,
    case_type_version      INTEGER                  NOT NULL,
    process_definition_key VARCHAR(255)             NOT NULL,
    deployment_id          VARCHAR(64)              NOT NULL,
    deployed_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT case_type_deployments_version_positive
        CHECK (case_type_version >= 1)
);

-- Reverse lookup index: occasional ops need "which case type advertises this processDefinitionKey?"
-- Cheap to add now while the table is empty; harder later (Story 2.4 review).
CREATE INDEX idx_case_type_deployments_process_definition_key
    ON case_type_deployments (process_definition_key);
