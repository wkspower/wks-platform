-- Story 5.4 — form draft persistence (auto-save + resume).
-- Scope: (case_id, form_id, user_id) — uniqueness enforced at DB level (AC5).
-- Payload is a JSON column to keep the schema stable across form-shape changes.
-- case_type_version_at_save captures the version the draft was last written against;
-- a mismatch with the case's current case_type_version triggers AC3 discard-prompt.

CREATE TABLE form_drafts (
    id                          UUID                     PRIMARY KEY,
    case_id                     UUID                     NOT NULL,
    form_id                     VARCHAR(255)             NOT NULL,
    user_id                     UUID                     NOT NULL,
    payload                     JSON                     NOT NULL,
    scroll_y                    INTEGER                  NOT NULL DEFAULT 0,
    section_expanded            JSON,
    case_type_version_at_save   INTEGER                  NOT NULL,
    version                     BIGINT                   NOT NULL DEFAULT 0,
    created_at                  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_form_drafts_scope UNIQUE (case_id, form_id, user_id),
    CONSTRAINT fk_form_drafts_case FOREIGN KEY (case_id) REFERENCES cases(id) ON DELETE CASCADE
);

CREATE INDEX idx_form_drafts_updated_at ON form_drafts (updated_at);
