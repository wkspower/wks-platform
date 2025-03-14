DROP ROLE IF EXISTS camunda;
DROP ROLE IF EXISTS keycloak;
DROP ROLE IF EXISTS wks;

CREATE ROLE camunda LOGIN PASSWORD 'camunda00';
CREATE ROLE keycloak LOGIN PASSWORD 'keycloak00';
CREATE ROLE wks LOGIN PASSWORD 'wks00';

CREATE DATABASE camunda WITH OWNER camunda;
CREATE DATABASE keycloak WITH OWNER keycloak;
CREATE DATABASE wks WITH OWNER wks;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE case_definition (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id text UNIQUE NOT NULL,
    name TEXT NOT NULL,
    form_key TEXT,
    stages_lifecycle_process_key TEXT,
    deployed BOOLEAN NOT NULL DEFAULT FALSE,
    stages text,
    case_hooks TEXT,
    kanban_config TEXT
);

CREATE TABLE record_type (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id TEXT UNIQUE NOT NULL,
    fields TEXT NOT NULL
);

CREATE TABLE record_type_instance (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    record_type_id TEXT NOT NULL,
    content TEXT NOT NULL
);

CREATE TABLE form (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    key TEXT UNIQUE NOT NULL,
    title TEXT,
    tool_tip TEXT,
    structure TEXT NOT NULL
);

CREATE TABLE queue (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    description TEXT
);

CREATE TABLE case_instance (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_key TEXT UNIQUE NOT NULL,
    queue_id TEXT,
    status TEXT,
    stage TEXT,
    attributes TEXT,
    documents TEXT,
    comments TEXT,
    case_definition_id TEXT,
    owner TEXT
);

CREATE TABLE case_email (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    case_instance_business_key VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    body TEXT,
    sender VARCHAR(255),
    recipient VARCHAR(255),
    received_date_time TIMESTAMP,
    has_attachments BOOLEAN,
    to_email VARCHAR(255),
    from_email VARCHAR(255),
    body_preview TEXT,
    importance VARCHAR(50),
    case_definition_id VARCHAR(255),
    outbound BOOLEAN,
    status VARCHAR(50)
);
