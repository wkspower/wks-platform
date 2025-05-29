CREATE TABLE IF NOT EXISTS  case_definition (
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

CREATE TABLE IF NOT EXISTS  record_type (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id TEXT UNIQUE NOT NULL,
    fields TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS  record_type_instance (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    record_type_id TEXT NOT NULL,
    content TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS  form (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    form_key TEXT UNIQUE NOT NULL,
    title TEXT,
    tool_tip TEXT,
    structure TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS  queue (
    uid UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS  case_instance (
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

CREATE TABLE IF NOT EXISTS  case_email (
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
