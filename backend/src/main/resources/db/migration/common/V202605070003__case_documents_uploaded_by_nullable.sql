-- Story 14.2 review patch (P6): allow uploaded_by to be NULL so user offboarding does not fail.
-- The existing NOT NULL constraint is dropped and replaced with a foreign-key that sets the
-- column to NULL when the referenced user row is deleted, keeping the document record intact.
ALTER TABLE case_documents
    ALTER COLUMN uploaded_by DROP NOT NULL;

ALTER TABLE case_documents
    ADD CONSTRAINT fk_case_documents_uploaded_by
        FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL;
