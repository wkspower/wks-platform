/**
 * Story 2.8 — wire types for `/api/cases/{id}/tasks` and `/api/tasks/{id}/{complete,claim}`.
 * Mirrors backend `TaskDto` (Story 2.8 AC1) and `TaskActionResponse` (Story 2.4).
 */

export interface TaskDto {
  id: string;
  processInstanceId: string;
  caseId: string;
  caseTypeId: string;
  taskDefinitionKey: string;
  name: string;
  assignee: string | null;
  archetype: string | null;
  /** BPMN-author-supplied CTA copy with userTask.name fallback (Story 2.8 AC1). */
  actionLabel: string | null;
  /**
   * Story 2-6-1 — form id bound to this userTask via `attachments[].userTaskMappings[].form` in
   * the case-type YAML; `null` when no mapping declares a form. Frontend renders an "Open form"
   * affordance on the task row when non-null.
   */
  formId: string | null;
  createdAt: string;
  dueAt: string | null;
}

export interface TaskActionResponse {
  taskId: string;
  processInstanceId: string;
  caseId: string;
  archetype: string | null;
  assignee: string | null;
  at: string;
}

/**
 * Discriminator for the `failed` lifecycle state when the engine returned a 409 conflict. The
 * `unknown` arm is the safe fallback when the envelope code is `WKS-RTM-409` but the message
 * does not match a known shape — UI still shows "already completed" copy.
 */
export type ConflictReason = 'already_completed' | 'reassigned' | 'engine_unavailable' | 'unknown';
