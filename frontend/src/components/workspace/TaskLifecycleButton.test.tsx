import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import type { ReactNode } from 'react';
import { describe, expect, it } from 'vitest';

import { server } from '@/test/server';
import type { TaskDto } from '@/types/task';

import { TaskLifecycleButton } from './TaskLifecycleButton';

const TASK: TaskDto = {
  id: 't1',
  processInstanceId: 'pi-1',
  caseId: 'c1',
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

describe('TaskLifecycleButton (Story 2.8 AC2/AC3/AC5)', () => {
  it('renders idle state with the task actionLabel', () => {
    render(wrap(<TaskLifecycleButton task={TASK} />));
    expect(screen.getByRole('button', { name: 'Draft application' })).toBeInTheDocument();
  });

  it('falls back to task.name when actionLabel is null', () => {
    render(wrap(<TaskLifecycleButton task={{ ...TASK, actionLabel: null, name: 'Review' }} />));
    expect(screen.getByRole('button', { name: 'Review' })).toBeInTheDocument();
  });

  it('transitions idle → confirming → confirmed on REST 2xx', async () => {
    server.use(
      http.post('/api/tasks/t1/complete', () =>
        HttpResponse.json(
          {
            data: {
              taskId: 't1',
              processInstanceId: 'pi-1',
              caseId: 'c1',
              archetype: 'draft_section',
              assignee: null,
              at: '2026-04-26T10:00:01Z',
            },
            meta: {},
          },
          { headers: { 'X-Correlation-Id': 'cid' } },
        ),
      ),
    );
    render(wrap(<TaskLifecycleButton task={TASK} />));
    fireEvent.click(screen.getByRole('button', { name: 'Draft application' }));
    await waitFor(() =>
      expect(screen.getByRole('button')).toHaveAttribute('data-state', 'confirmed'),
    );
  });

  it('shows [Refresh case] (not [Retry]) on a 409 conflict', async () => {
    server.use(
      http.post('/api/tasks/t1/complete', () =>
        HttpResponse.json(
          {
            error: {
              code: 'WKS-RTM-409',
              message: 'Task t1 already completed',
              field: null,
            },
            meta: {},
          },
          { status: 409 },
        ),
      ),
    );
    render(wrap(<TaskLifecycleButton task={TASK} />));
    fireEvent.click(screen.getByRole('button', { name: 'Draft application' }));
    await waitFor(() => expect(screen.getByText('Refresh case')).toBeInTheDocument());
    expect(screen.queryByText('Retry')).not.toBeInTheDocument();
  });

  it('shows [Retry] on a 5xx failure', async () => {
    server.use(
      http.post('/api/tasks/t1/complete', () =>
        HttpResponse.json(
          { error: { code: 'WKS-API-500', message: 'boom', field: null }, meta: {} },
          { status: 500 },
        ),
      ),
    );
    render(wrap(<TaskLifecycleButton task={TASK} />));
    fireEvent.click(screen.getByRole('button', { name: 'Draft application' }));
    await waitFor(() => expect(screen.getByText('Retry')).toBeInTheDocument());
    expect(screen.queryByText('Refresh case')).not.toBeInTheDocument();
  });
});
