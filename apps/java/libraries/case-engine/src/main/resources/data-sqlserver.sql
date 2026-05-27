-- Case Definitions
INSERT INTO dbo.case_definition (uid, id, name, form_key, stages_lifecycle_process_key, deployed, stages, case_hooks, kanban_config) VALUES (NEWID(), 'customer-support', 'Customer Support Case', 'customer-support-form', 'customer-support', 1, '[{"id": "0", "index": 0, "name": "Ticket Creation"}, {"id": "1", "index": 1, "name": "Issue Investigation"}, {"id": "2", "index": 2, "name": "Resolution"}]', '[{"actions": [{"actionType": "CASE_STAGE_UPDATE_ACTION", "newStage": "Issue Investigation"}], "eventType": "TASK_COMPLETE_EVENT_TYPE", "processDefKey": "customer-support", "taskDefKey": "create-ticket"}]', '{}');
INSERT INTO dbo.case_definition (uid, id, name, form_key, stages_lifecycle_process_key, deployed, stages, case_hooks, kanban_config) VALUES (NEWID(), 'employee-onboarding', 'Employee Onboarding Case', 'employee-onboarding-form', 'employee-onboarding', 1, '[{"id": "0", "index": 0, "name": "Registration"}, {"id": "1", "index": 1, "name": "Approval"}]', '[]', '{}');

-- Forms
INSERT INTO dbo.form (uid, form_key, title, tool_tip, structure) VALUES (NEWID(), 'customer-support-form', 'Customer Support', 'Customer Support', '{"components": [{"label": "Ticket Title", "key": "ticketTitle", "type": "textfield"}]}');
INSERT INTO dbo.form (uid, form_key, title, tool_tip, structure) VALUES (NEWID(), 'employee-onboarding-form', 'Employee Onboarding', 'Employee Onboarding', '{"components": [{"label": "Employee Name", "key": "employeeName", "type": "textfield"}]}');

-- Record Types
INSERT INTO dbo.record_type (uid, id, fields) VALUES (NEWID(), 'customer', '{"fields": [{"name": "name", "label": "Name", "type": "text"}]}');
INSERT INTO dbo.record_type (uid, id, fields) VALUES (NEWID(), 'product', '{"fields": [{"name": "sku", "label": "SKU", "type": "text"}]}');

-- Queues
INSERT INTO dbo.queue (uid, id, name, description) VALUES (NEWID(), 'support-queue', 'Support Queue', 'Queue for support tickets');
INSERT INTO dbo.queue (uid, id, name, description) VALUES (NEWID(), 'hr-queue', 'HR Queue', 'Queue for HR requests');
