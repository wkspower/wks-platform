-- Story 14.2 — Document metadata table for case-attached files.
-- Storage key points to LocalDocumentStore path or MinIO object key.
-- Checksum is SHA-256 hex of the file bytes at upload time.
CREATE TABLE case_documents (
    id           UUID         NOT NULL,
    case_id      UUID         NOT NULL REFERENCES cases(id) ON DELETE CASCADE,
    file_name    VARCHAR(512) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    size_bytes   BIGINT       NOT NULL,
    storage_key  VARCHAR(768) NOT NULL,
    checksum     VARCHAR(64)  NOT NULL,
    uploaded_by  UUID         NOT NULL REFERENCES users(id),
    uploaded_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_case_documents PRIMARY KEY (id)
);
CREATE INDEX idx_case_documents_case_id ON case_documents (case_id, uploaded_at DESC);
