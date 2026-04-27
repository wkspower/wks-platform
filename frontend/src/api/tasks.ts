import type { TaskActionResponse, TaskDto } from '@/types/task';

import { apiFetch } from './client';

/** Story 2.8 AC1 — list pending user tasks for a case. */
export async function listTasksByCase(caseId: string): Promise<TaskDto[]> {
  const result = await apiFetch<TaskDto[]>(`/api/cases/${encodeURIComponent(caseId)}/tasks`);
  return result.data;
}

/**
 * Story 2.4 — complete a user task. Phase 0 (Story 2.8) ships click-to-complete with empty
 * variables; task forms with custom variables are out of scope (story §Out of scope).
 */
export async function completeTask(
  taskId: string,
  variables: Record<string, unknown> = {},
): Promise<TaskActionResponse> {
  const result = await apiFetch<TaskActionResponse>(
    `/api/tasks/${encodeURIComponent(taskId)}/complete`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ variables }),
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
