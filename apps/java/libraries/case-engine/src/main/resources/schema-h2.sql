CREATE TABLE case_definition (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
    id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    form_key VARCHAR(255),
    stages_lifecycle_process_key VARCHAR(255),
    deployed BOOLEAN NOT NULL DEFAULT FALSE,
    stages TEXT,
    case_hooks TEXT,
    kanban_config TEXT
);

CREATE TABLE record_type (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
    id VARCHAR(255) UNIQUE NOT NULL,
    fields TEXT NOT NULL
);

CREATE TABLE record_type_instance (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
    record_type_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL
);

CREATE TABLE form (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
    form_key VARCHAR(255) UNIQUE NOT NULL,
    title VARCHAR(255),
    tool_tip VARCHAR(255),
    structure TEXT NOT NULL
);

CREATE TABLE queue (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
    id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE case_instance (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
    business_key VARCHAR(255) UNIQUE NOT NULL,
    queue_id VARCHAR(255),
    status VARCHAR(255),
    stage VARCHAR(255),
    attributes TEXT,
    documents TEXT,
    comments TEXT,
    case_definition_id VARCHAR(255),
    owner VARCHAR(255)
);

CREATE TABLE case_email (
    uid VARCHAR(36) DEFAULT RANDOM_UUID() PRIMARY KEY,
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
