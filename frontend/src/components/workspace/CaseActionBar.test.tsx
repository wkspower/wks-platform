import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { act, render, screen, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import type { ReactNode } from 'react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { server } from '@/test/server';
import type { TaskDto } from '@/types/task';

import { CaseActionBar } from './CaseActionBar';

const CASE_ID = 'case-1';

const TASK: TaskDto = {
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

function wrap(node: ReactNode) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{node}</QueryClientProvider>;
}

describe('CaseActionBar (Story 2.8 AC8/AC10)', () => {
  beforeEach(() => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
  });
  afterEach(() => {
    vi.useRealTimers();
  });

  it('renders the primary CTA with task.actionLabel when a task is pending', async () => {
    server.use(
      http.get(`/api/cases/${CASE_ID}/tasks`, () => HttpResponse.json({ data: [TASK], meta: {} })),
    );
    render(wrap(<CaseActionBar caseId={CASE_ID} />));
    await waitFor(() =>
      expect(screen.getByRole('button', { name: 'Draft application' })).toBeInTheDocument(),
    );
  });

  it('shows the "Next case" hint after a 4s delay when no tasks are pending', async () => {
    server.use(
      http.get(`/api/cases/${CASE_ID}/tasks`, () => HttpResponse.json({ data: [], meta: {} })),
    );
    render(wrap(<CaseActionBar caseId={CASE_ID} />));
    // Wait for the empty data to land first
    await waitFor(() => expect(screen.queryByTestId('case-action-bar')).not.toBeInTheDocument());
    expect(screen.queryByTestId('case-action-bar-empty-hint')).not.toBeInTheDocument();
    act(() => {
      vi.advanceTimersByTime(4_500);
    });
    expect(screen.getByTestId('case-action-bar-empty-hint')).toBeInTheDocument();
    expect(screen.getByText('Next case (J)')).toBeInTheDocument();
  });
});
