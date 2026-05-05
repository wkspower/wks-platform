import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import type { ReactNode } from 'react';
import { describe, expect, it } from 'vitest';

import { server } from '@/test/server';
import type { CaseDto } from '@/types/case';

import { useCase } from './useCases';

const FIXTURE_ID = '11111111-2222-3333-4444-555555555555';

const fixture: CaseDto = {
  id: FIXTURE_ID,
  caseTypeId: 'loan_application',
  caseTypeVersion: 3,
  status: 'open',
  assignee: null,
  data: { applicant_name: 'Asha' },
  processInstanceId: null,
  documentCount: 0,
  createdAt: '2026-04-01T00:00:00Z',
  createdBy: null,
  updatedAt: '2026-04-02T00:00:00Z',
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

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

describe('useCase', () => {
  it('is disabled when id is null', () => {
    const { result } = renderHook(() => useCase(null), { wrapper });
    expect(result.current.isLoading).toBe(false);
    expect(result.current.fetchStatus).toBe('idle');
    expect(result.current.data).toBeUndefined();
  });

  it('fetches and unwraps the API envelope', async () => {
    server.use(
      http.get(`/api/cases/${FIXTURE_ID}`, () => HttpResponse.json({ data: fixture, meta: {} })),
    );
    const { result } = renderHook(() => useCase(FIXTURE_ID), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data?.id).toBe(FIXTURE_ID);
    expect(result.current.data?.caseType.displayName).toBe('Loan Application');
  });
});
