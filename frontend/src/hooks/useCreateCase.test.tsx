import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import type { ReactNode } from 'react';
import { describe, expect, it } from 'vitest';

import { server } from '@/test/server';
import type { CaseDto } from '@/types/case';

import { useCreateCase } from './useCases';

const NEW_ID = '99999999-aaaa-bbbb-cccc-dddddddddddd';

const fixture: CaseDto = {
  id: NEW_ID,
  caseTypeId: 'loan_application',
  caseTypeVersion: 3,
  status: 'open',
  assignee: null,
  data: { applicant_name: 'Asha' },
  processInstanceId: null,
  documentCount: 0,
  createdAt: '2026-04-01T00:00:00Z',
  createdBy: null,
  updatedAt: '2026-04-01T00:00:00Z',
  version: 1,
  caseType: {
    id: 'loan_application',
    displayName: 'Loan Application',
    version: 3,
    fields: [],
    statuses: [],
    listColumns: [],
  },
  stages: [],
};

function setupClient() {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return client;
}

function buildWrapper(client: QueryClient) {
  return function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
  };
}

describe('useCreateCase', () => {
  it('on success: stores the new case in detail cache and invalidates list queries', async () => {
    server.use(http.post('/api/cases', () => HttpResponse.json({ data: fixture, meta: {} })));
    const client = setupClient();
    const wrapper = buildWrapper(client);
    const invalidate = client.invalidateQueries.bind(client);
    let invalidatedListsCount = 0;
    client.invalidateQueries = ((args: Parameters<typeof invalidate>[0]) => {
      const key = args?.queryKey?.[0];
      if (key === 'cases' || key === 'case-list') invalidatedListsCount += 1;
      return invalidate(args);
    }) as typeof invalidate;

    const { result } = renderHook(() => useCreateCase(), { wrapper });
    await result.current.mutateAsync({
      caseTypeId: 'loan_application',
      data: { applicant_name: 'Asha' },
      assignee: null,
    });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data?.id).toBe(NEW_ID);
    expect(invalidatedListsCount).toBeGreaterThan(0);
  });

  it('does NOT auto-retry on 5xx (silent retry would duplicate the case server-side)', async () => {
    let callCount = 0;
    server.use(
      http.post('/api/cases', () => {
        callCount += 1;
        return HttpResponse.json({ error: { code: 'WKS-API-500' } }, { status: 500 });
      }),
    );
    const wrapper = buildWrapper(setupClient());
    const { result } = renderHook(() => useCreateCase(), { wrapper });
    await expect(
      result.current.mutateAsync({
        caseTypeId: 'loan_application',
        data: {},
        assignee: null,
      }),
    ).rejects.toBeTruthy();
    expect(callCount).toBe(1);
  });
});
