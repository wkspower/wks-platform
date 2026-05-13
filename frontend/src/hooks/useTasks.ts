import {
  skipToken,
  useMutation,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import { completeTask, listAllTasks, listTasksByCase, type CrossCaseTaskList } from '@/api/tasks';
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
    // Story 2.8 — task list invalidation is driven by `useCompleteTask` (on error) and by the
    // lifecycle button after the confirmed fade. A focus-refetch here would silently hide the
    // CTA in the conflict-replay scenario (J6) before the second tab can fire the duplicate
    // request that surfaces the 409 + [Refresh case] recovery — defeating AC5.
    refetchOnWindowFocus: false,
  });
}

/**
 * Story 13-1 AC1 — fetch the cross-case task list for the Tasks screen. Server caps at 500;
 * {@code data.truncated === true} signals the banner copy.
 */
export function useAllTasks(): UseQueryResult<CrossCaseTaskList, Error> {
  return useQuery<CrossCaseTaskList, Error>({
    queryKey: taskQueryKeys.allCases(),
    queryFn: listAllTasks,
    staleTime: STALE_TIME_MS,
  });
}

export interface CompleteTaskArgs {
  taskId: string;
  caseId: string;
  variables?: Record<string, unknown>;
  /** Story 6.2 AC2 — outcome key to pass as process variable for OUTCOME signal routing. */
  outcome?: string;
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
    mutationFn: ({ taskId, variables, outcome }) => completeTask(taskId, variables ?? {}, outcome),
    onSettled: (_data, _error, { caseId }) => {
      // Refresh case detail so the user sees the engine's truth (status / data). Task list
      // invalidation is intentionally NOT done here — on success it is handled by
      // `TaskLifecycleButton` after the 2s confirmed fade so the green chip survives, and on
      // error (especially 409 conflict) invalidating would unmount the button before the user
      // sees the failed state and the [Refresh case] / [Retry] recovery actions. The user-driven
      // actions invalidate the task list themselves when invoked.
      queryClient.invalidateQueries({ queryKey: caseQueryKeys.detail(caseId) });
    },
    retry: false,
  });
}
