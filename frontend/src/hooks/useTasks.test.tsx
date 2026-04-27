import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import type { ReactNode } from 'react';
import { describe, expect, it } from 'vitest';

import { taskQueryKeys } from '@/lib/queryKeys';
import { server } from '@/test/server';
import type { TaskDto } from '@/types/task';

import { useCaseTasks } from './useTasks';

const CASE_ID = '11111111-2222-3333-4444-555555555555';

const TASK_FIXTURE: TaskDto = {
  id: 't1',
  processInstanceId: 'pi-1',
  caseId: CASE_ID,
  caseTypeId: 'loan-application',
  taskDefinitionKey: 'draft',
  name: 'Draft application',
  assignee: null,
  archetype: 'draft_section',
  actionLabel: 'Draft application',
  createdAt: '2026-04-26T10:00:00Z',
  dueAt: null,
};

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

describe('useCaseTasks', () => {
  it('is disabled when caseId is null', () => {
    const { result } = renderHook(() => useCaseTasks(null), { wrapper });
    expect(result.current.fetchStatus).toBe('idle');
    expect(result.current.data).toBeUndefined();
  });

  it('uses taskQueryKeys.byCase as the query key', () => {
    expect(taskQueryKeys.byCase(CASE_ID)).toEqual(['tasks', 'byCase', CASE_ID]);
  });

  it('fetches and unwraps the envelope', async () => {
    server.use(
      http.get(`/api/cases/${CASE_ID}/tasks`, () =>
        HttpResponse.json(
          { data: [TASK_FIXTURE], meta: {} },
          { headers: { 'X-Correlation-Id': 'cid' } },
        ),
      ),
    );
    const { result } = renderHook(() => useCaseTasks(CASE_ID), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual([TASK_FIXTURE]);
  });
});
