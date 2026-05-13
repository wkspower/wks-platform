import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import type { ReactNode } from 'react';
import { describe, expect, it, vi } from 'vitest';

import { server } from '@/test/server';
import type { TaskDto } from '@/types/task';

import { TaskCompletionDialog } from './TaskCompletionDialog';

/** Task with submit_for_processing archetype — outcome picker is the typical attachment surface. */
const TASK: TaskDto = {
  id: 't1',
  processInstanceId: 'pi-1',
  caseId: 'c1',
  caseTypeId: 'loan-application',
  taskDefinitionKey: 'review',
  name: 'Review claim',
  assignee: null,
  archetype: 'submit_for_processing',
  actionLabel: 'Review claim',
  formId: null,
  createdAt: '2026-05-11T10:00:00Z',
  dueAt: null,
};

function wrap(node: ReactNode) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{node}</QueryClientProvider>;
}

function captureCompleteBody(): { value: unknown | null } {
  const captured: { value: unknown | null } = { value: null };
  server.use(
    http.post('/api/tasks/t1/complete', async ({ request }) => {
      captured.value = await request.json();
      return HttpResponse.json(
        {
          data: {
            taskId: 't1',
            processInstanceId: 'pi-1',
            caseId: 'c1',
            archetype: 'submit_for_processing',
            assignee: null,
            at: '2026-05-11T10:00:01Z',
          },
          meta: {},
        },
        { headers: { 'X-Correlation-Id': 'cid' } },
      );
    }),
  );
  return captured;
}

describe('TaskCompletionDialog (Story 6.2 AC1)', () => {
  it('renders N=2 buttons for two outcome keys', () => {
    render(
      wrap(
        <TaskCompletionDialog
          task={TASK}
          outcomeMappings={{
            approve: 'intake -> review',
            reject: 'intake -> closed',
          }}
          open={true}
          onOpenChange={() => {}}
        />,
      ),
    );
    // Title is always present; expect exactly two outcome buttons + the Cancel button.
    expect(screen.getByText('Approve')).toBeInTheDocument();
    expect(screen.getByText('Reject')).toBeInTheDocument();
    const outcomeBtns = screen
      .getAllByRole('button')
      .filter((b) => b.hasAttribute('data-outcome-key'));
    expect(outcomeBtns).toHaveLength(2);
  });

  it('renders N=3 buttons for three outcome keys', () => {
    render(
      wrap(
        <TaskCompletionDialog
          task={TASK}
          outcomeMappings={{
            approve: 'intake -> review',
            reject: 'intake -> closed',
            sendBack: 'intake -> intake',
          }}
          open={true}
          onOpenChange={() => {}}
        />,
      ),
    );
    const outcomeBtns = screen
      .getAllByRole('button')
      .filter((b) => b.hasAttribute('data-outcome-key'));
    expect(outcomeBtns).toHaveLength(3);
  });

  it('renders N=4 buttons for four outcome keys', () => {
    render(
      wrap(
        <TaskCompletionDialog
          task={TASK}
          outcomeMappings={{
            approve: 'intake -> review',
            reject: 'intake -> closed',
            sendBack: 'intake -> intake',
            escalate: 'intake -> escalation',
          }}
          open={true}
          onOpenChange={() => {}}
        />,
      ),
    );
    const outcomeBtns = screen
      .getAllByRole('button')
      .filter((b) => b.hasAttribute('data-outcome-key'));
    expect(outcomeBtns).toHaveLength(4);
  });

  it('clicking an outcome button fires completeTask with {outcome: <key>}', async () => {
    const captured = captureCompleteBody();
    const onChange = vi.fn();
    render(
      wrap(
        <TaskCompletionDialog
          task={TASK}
          outcomeMappings={{
            approve: 'intake -> review',
            reject: 'intake -> closed',
          }}
          open={true}
          onOpenChange={onChange}
        />,
      ),
    );
    fireEvent.click(screen.getByText('Approve'));
    await waitFor(() => expect(captured.value).not.toBeNull());
    expect(captured.value).toEqual({ variables: { outcome: 'approve' } });
  });

  it('returns null (empty-state guard) when outcomeMappings is empty', () => {
    const { container } = render(
      wrap(
        <TaskCompletionDialog
          task={TASK}
          outcomeMappings={{}}
          open={true}
          onOpenChange={() => {}}
        />,
      ),
    );
    // Dialog must not render anything — no AlertDialog body, no buttons, no title.
    expect(container.querySelector('[role="alertdialog"]')).toBeNull();
    expect(screen.queryAllByRole('button')).toHaveLength(0);
  });

  it('filters whitespace-only keys; renders nothing when all keys are blank', () => {
    const { container } = render(
      wrap(
        <TaskCompletionDialog
          task={TASK}
          outcomeMappings={{ '   ': 'intake -> review', '\t': 'intake -> closed' }}
          open={true}
          onOpenChange={() => {}}
        />,
      ),
    );
    expect(container.querySelector('[role="alertdialog"]')).toBeNull();
  });

  it('renders only non-whitespace keys when mixed with valid keys', () => {
    render(
      wrap(
        <TaskCompletionDialog
          task={TASK}
          outcomeMappings={{
            approve: 'intake -> review',
            '   ': 'should-be-filtered',
            reject: 'intake -> closed',
          }}
          open={true}
          onOpenChange={() => {}}
        />,
      ),
    );
    const outcomeBtns = screen
      .getAllByRole('button')
      .filter((b) => b.hasAttribute('data-outcome-key'));
    expect(outcomeBtns).toHaveLength(2);
    expect(outcomeBtns.map((b) => b.getAttribute('data-outcome-key'))).toEqual([
      'approve',
      'reject',
    ]);
  });
});
