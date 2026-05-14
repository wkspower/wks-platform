-- Add previous_result to audit_events for delta-typed events.
--
-- case.status.changed (and future from->to events: stage transitions, assignee/priority
-- changes) need to record both the prior and new value. The original audit_events table
-- only had `result` (the new value); previous_result is the symmetric "before" slot.
-- Nullable because case.data.edit and case.created rows have no prior value.
--
-- Also widens `result` from VARCHAR(16) to VARCHAR(64). The 16-char ceiling was sized for
-- CaseDataEdited.Result tokens (APPLIED / BLOCKED / REJECTED) but case.status.changed rows
-- store status ids verbatim, and a status id like `pending_review` or `awaiting_approval`
-- can exceed 16 chars in case-type YAML. Widen previous_result to match.
--
-- Portable across H2 2.x and Postgres via the SQL:2003 ALTER COLUMN SET DATA TYPE form.

ALTER TABLE audit_events ADD COLUMN previous_result VARCHAR(64);
ALTER TABLE audit_events ALTER COLUMN result SET DATA TYPE VARCHAR(64);
