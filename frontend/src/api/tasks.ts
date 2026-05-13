import type { TaskActionResponse, TaskDto } from '@/types/task';

import { apiFetch } from './client';

/** Story 2.8 AC1 — list pending user tasks for a case. */
export async function listTasksByCase(caseId: string): Promise<TaskDto[]> {
  const result = await apiFetch<TaskDto[]>(`/api/cases/${encodeURIComponent(caseId)}/tasks`);
  return result.data;
}

/** Story 13-1 AC1 — wire shape returned by {@code GET /api/tasks}. */
export interface CrossCaseTaskList {
  items: TaskDto[];
  truncated: boolean;
}

/**
 * Story 13-1 AC1 — list all pending user tasks across every case-type the caller can view.
 * Server caps the response at 500 entries (oldest-first). When the cap is hit, {@code truncated}
 * is {@code true} and the page renders a truncation banner above the list.
 */
export async function listAllTasks(): Promise<CrossCaseTaskList> {
  const result = await apiFetch<CrossCaseTaskList>('/api/tasks');
  return result.data;
}

/**
 * Story 2.4 — complete a user task. Phase 0 (Story 2.8) ships click-to-complete with empty
 * variables; task forms with custom variables are out of scope (story §Out of scope).
 *
 * Story 6.2 AC2 — when {@code outcome} is provided (non-null), it is merged into the Camunda
 * process variables as {@code outcome: <key>}. The {@code CaseStatusListener} reads the variable
 * during the task-end callback and emits an OUTCOME signal that the router routes to the
 * configured stageTransition mapping rule.
 */
export async function completeTask(
  taskId: string,
  variables: Record<string, unknown> = {},
  outcome?: string,
): Promise<TaskActionResponse> {
  const allVariables = outcome !== undefined ? { ...variables, outcome } : variables;
  const result = await apiFetch<TaskActionResponse>(
    `/api/tasks/${encodeURIComponent(taskId)}/complete`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ variables: allVariables }),
    },
  );
  return result.data;
}

/** Story 2.4 — claim an unassigned user task. */
export async function claimTask(taskId: string): Promise<TaskActionResponse> {
  const result = await apiFetch<TaskActionResponse>(
    `/api/tasks/${encodeURIComponent(taskId)}/claim`,
    { method: 'POST' },
  );
  return result.data;
}
