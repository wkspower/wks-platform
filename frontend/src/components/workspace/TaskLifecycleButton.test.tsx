import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import type { ReactNode } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { server } from '@/test/server';
import type { TaskDto } from '@/types/task';

import { TaskLifecycleButton } from './TaskLifecycleButton';

/** Task with draft_section archetype — Story 6.1 AC3: label = "Save section" */
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
  formId: null,
  createdAt: '2026-04-26T10:00:00Z',
  dueAt: null,
};

/** Task with no archetype — pre-6.1 backward-compat path uses actionLabel / name */
const TASK_NO_ARCHETYPE: TaskDto = {
  ...TASK,
  archetype: null,
};

function wrap(node: ReactNode) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return <QueryClientProvider client={client}>{node}</QueryClientProvider>;
}

describe('TaskLifecycleButton (Story 2.8 AC2/AC3/AC5)', () => {
  // Story 6.1 AC3 — archetype-driven label overrides actionLabel.
  it('renders idle state with the archetype-driven label for draft_section', () => {
    render(wrap(<TaskLifecycleButton task={TASK} />));
    expect(screen.getByRole('button', { name: 'Save section' })).toBeInTheDocument();
  });

  it('falls back to task.actionLabel when archetype is null', () => {
    render(
      wrap(
        <TaskLifecycleButton task={{ ...TASK_NO_ARCHETYPE, actionLabel: 'Draft application' }} />,
      ),
    );
    expect(screen.getByRole('button', { name: 'Draft application' })).toBeInTheDocument();
  });

  it('falls back to task.name when archetype is null and actionLabel is null', () => {
    render(
      wrap(
        <TaskLifecycleButton task={{ ...TASK_NO_ARCHETYPE, actionLabel: null, name: 'Review' }} />,
      ),
    );
    expect(screen.getByRole('button', { name: 'Review' })).toBeInTheDocument();
  });

  it('transitions idle → confirming → confirmed on REST 2xx (draft_section — inline flow)', async () => {
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
    // draft_section: inline flow — no dialog; click fires directly
    fireEvent.click(screen.getByRole('button', { name: 'Save section' }));
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
    fireEvent.click(screen.getByRole('button', { name: 'Save section' }));
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
    fireEvent.click(screen.getByRole('button', { name: 'Save section' }));
    await waitFor(() => expect(screen.getByText('Retry')).toBeInTheDocument());
    expect(screen.queryByText('Refresh case')).not.toBeInTheDocument();
  });
});

// Story 6.1 AC3 — three archetype scenarios with distinct affordances.
describe('TaskLifecycleButton — Story 6.1 AC3 archetype scenarios', () => {
  it('draft_section: renders "Save section" with secondary (ghost) tone', () => {
    const task: TaskDto = { ...TASK, archetype: 'draft_section' };
    render(wrap(<TaskLifecycleButton task={task} />));
    const btn = screen.getByRole('button', { name: 'Save section' });
    expect(btn).toBeInTheDocument();
    // ghost variant has "hover:bg-[var(--muted)]" class
    expect(btn.className).toMatch(/hover:bg-\[var\(--muted\)\]/);
  });

  it('submit_for_processing: renders "Submit for processing" with primary tone', () => {
    const task: TaskDto = { ...TASK, archetype: 'submit_for_processing' };
    render(wrap(<TaskLifecycleButton task={task} />));
    expect(screen.getByRole('button', { name: 'Submit for processing' })).toBeInTheDocument();
  });

  it('business_final: renders "Submit for final approval" and shows AlertDialog on click', async () => {
    const task: TaskDto = { ...TASK, archetype: 'business_final' };
    render(wrap(<TaskLifecycleButton task={task} />));
    const triggerBtn = screen.getByRole('button', { name: 'Submit for final approval' });
    expect(triggerBtn).toBeInTheDocument();
    // Click the trigger — should open the dialog (confirmation-dialog flow)
    fireEvent.click(triggerBtn);
    // Dialog should appear
    await waitFor(() => expect(screen.getByText('Confirm submission')).toBeInTheDocument());
    expect(screen.getByText(/final action/i)).toBeInTheDocument();
    // Cancel button present
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument();
  });

  it('business_final: cancel dialog returns to idle without firing mutation', async () => {
    const task: TaskDto = { ...TASK, archetype: 'business_final' };
    render(wrap(<TaskLifecycleButton task={task} />));
    fireEvent.click(screen.getByRole('button', { name: 'Submit for final approval' }));
    await waitFor(() => expect(screen.getByText('Confirm submission')).toBeInTheDocument());
    // Cancel
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }));
    // Dialog should close; trigger button still visible
    await waitFor(() => expect(screen.queryByText('Confirm submission')).not.toBeInTheDocument());
    expect(screen.getByRole('button', { name: 'Submit for final approval' })).toBeInTheDocument();
  });

  it('no archetype: renders pre-6.1 default label from actionLabel', () => {
    const task: TaskDto = { ...TASK, archetype: null, actionLabel: 'Process claim' };
    render(wrap(<TaskLifecycleButton task={task} />));
    expect(screen.getByRole('button', { name: 'Process claim' })).toBeInTheDocument();
  });
});

/** Story 2-6-1 AC2/AC3/AC4 — "Open form" affordance keyed off task.formId. */
describe('TaskLifecycleButton — Open form affordance (Story 2-6-1)', () => {
  function wrapWithRouter(node: ReactNode) {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    return (
      <QueryClientProvider client={client}>
        <MemoryRouter>{node}</MemoryRouter>
      </QueryClientProvider>
    );
  }

  it('AC2 — renders the Open form affordance when task.formId is non-null', () => {
    const task: TaskDto = { ...TASK, formId: 'loan-form' };
    render(wrapWithRouter(<TaskLifecycleButton task={task} />));
    const link = screen.getByTestId('task-open-form-link');
    expect(link).toBeInTheDocument();
    expect(link).toHaveTextContent('Open form');
    expect(link).toHaveAttribute('aria-label', 'Open the form for this task');
  });

  it('AC3 — suppresses the affordance when task.formId is null', () => {
    const task: TaskDto = { ...TASK, formId: null };
    render(wrapWithRouter(<TaskLifecycleButton task={task} />));
    expect(screen.queryByTestId('task-open-form-link')).not.toBeInTheDocument();
  });

  it('AC4 — affordance navigates to /cases/{caseId}/forms/{formId}', () => {
    const task: TaskDto = { ...TASK, caseId: 'c1', formId: 'loan-form' };
    render(wrapWithRouter(<TaskLifecycleButton task={task} />));
    const link = screen.getByTestId('task-open-form-link');
    expect(link).toHaveAttribute('href', '/cases/c1/forms/loan-form');
  });

  it('AC5 — existing complete-task CTA still renders alongside the affordance', () => {
    const task: TaskDto = { ...TASK, formId: 'loan-form' };
    render(wrapWithRouter(<TaskLifecycleButton task={task} />));
    // Primary CTA (archetype-driven) is still present.
    expect(screen.getByRole('button', { name: 'Save section' })).toBeInTheDocument();
    // Affordance is its own anchor element (asChild Slot → <a>), not a button.
    expect(screen.getByTestId('task-open-form-link').tagName).toBe('A');
  });
});
