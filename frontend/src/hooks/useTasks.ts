import {
  skipToken,
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import { completeTask, listTasksByCase } from '@/api/tasks';
import { caseQueryKeys, taskQueryKeys } from '@/lib/queryKeys';
import type { TaskActionResponse, TaskDto } from '@/types/task';

const STALE_TIME_MS = 30_000;

/**
 * Story 2.8 AC1 — list pending user tasks for a case. Disabled when {@code caseId} is falsy
 * (e.g. detail panel before a case is selected). The {@code enabled} gate uses {@code skipToken}
 * (per Story 2.5 review) so the queryFn never runs with a null id.
 */
export function useCaseTasks(caseId: string | null): UseQueryResult<TaskDto[], Error> {
  return useQuery<TaskDto[], Error>({
    queryKey: caseId ? taskQueryKeys.byCase(caseId) : taskQueryKeys.byCase('__disabled__'),
    queryFn: caseId ? () => listTasksByCase(caseId) : skipToken,
    staleTime: STALE_TIME_MS,
    refetchOnWindowFocus: true,
  });
}

export interface CompleteTaskArgs {
  taskId: string;
  caseId: string;
  variables?: Record<string, unknown>;
}

/**
 * Story 2.8 AC3 — complete a user task. On 2xx (and on 409 — the conflict resolution UX surfaces
 * stale data immediately) invalidates {@code taskQueryKeys.byCase(caseId)} and {@code
 * caseQueryKeys.detail(caseId)} so the action bar repaints with the engine's truth. List queries
 * are NOT invalidated here — Story 4.3's SSE channel covers the cross-case feed.
 *
 * No automatic retry — `[Retry]` lives in the UI lifecycle so users see exactly when a retry
 * fires, and 409 conflicts MUST not silently re-issue.
 */
export function useCompleteTask(): UseMutationResult<TaskActionResponse, Error, CompleteTaskArgs> {
  const queryClient = useQueryClient();
  return useMutation<TaskActionResponse, Error, CompleteTaskArgs>({
    mutationKey: ['tasks', 'complete'],
    mutationFn: ({ taskId, variables }) => completeTask(taskId, variables ?? {}),
    onSettled: (_data, _error, { caseId }) => {
      // Both success and failure refresh the case + task views — on 409 the truth has moved on,
      // and on 5xx a refetch may still pick up partial engine state for diagnostics.
      queryClient.invalidateQueries({ queryKey: taskQueryKeys.byCase(caseId) });
      queryClient.invalidateQueries({ queryKey: caseQueryKeys.detail(caseId) });
    },
    retry: false,
  });
}
