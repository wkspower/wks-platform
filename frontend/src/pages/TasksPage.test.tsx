import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { renderWithProviders } from '@/test/renderWithProviders';
import { server } from '@/test/server';
import type { TaskDto } from '@/types/task';

import { TasksPage } from './TasksPage';

const CASE_A = '11111111-2222-3333-4444-555555555555';
const CASE_B = '99999999-8888-7777-6666-555555555555';

function taskFixture(overrides: Partial<TaskDto>): TaskDto {
  return {
    id: 't1',
    processInstanceId: 'pi-1',
    caseId: CASE_A,
    caseTypeId: 'loan-application',
    taskDefinitionKey: 'draft',
    name: 'Draft application',
    assignee: null,
    archetype: 'draft_section',
    actionLabel: 'Draft application',
    formId: null,
    createdAt: '2026-04-26T10:00:00Z',
    dueAt: null,
    ...overrides,
  };
}

describe('TasksPage', () => {
  it('renders one row per task from GET /api/tasks (AC2)', async () => {
    server.use(
      http.get('/api/tasks', () =>
        HttpResponse.json(
          {
            data: {
              items: [
                taskFixture({
                  id: 't1',
                  caseId: CASE_A,
                  name: 'Review documents',
                  actionLabel: 'Review',
                }),
                taskFixture({
                  id: 't2',
                  caseId: CASE_B,
                  name: 'Approve request',
                  actionLabel: 'Approve',
                }),
              ],
              truncated: false,
            },
            meta: {},
          },
          { headers: { 'X-Correlation-Id': 'cid' } },
        ),
      ),
    );

    renderWithProviders(<TasksPage />);

    await waitFor(() => expect(screen.getByRole('table', { name: /tasks/i })).toBeInTheDocument());
    expect(screen.getByText('Review documents')).toBeInTheDocument();
    expect(screen.getByText('Approve request')).toBeInTheDocument();
    // Banner absent when not truncated
    expect(screen.queryByTestId('tasks-truncated-banner')).not.toBeInTheDocument();
  });

  it('renders the empty-state copy when items is empty (AC5)', async () => {
    server.use(
      http.get('/api/tasks', () =>
        HttpResponse.json({ data: { items: [], truncated: false }, meta: {} }),
      ),
    );

    renderWithProviders(<TasksPage />);

    await waitFor(() => expect(screen.getByTestId('tasks-empty')).toBeInTheDocument());
    expect(screen.getByText(/no tasks for you right now/i)).toBeInTheDocument();
    // No table renders in the empty path.
    expect(screen.queryByRole('table')).not.toBeInTheDocument();
  });

  it('renders the truncation banner when truncated=true (AC5)', async () => {
    server.use(
      http.get('/api/tasks', () =>
        HttpResponse.json({
          data: {
            items: [taskFixture({ id: 't1' })],
            truncated: true,
          },
          meta: {},
        }),
      ),
    );

    renderWithProviders(<TasksPage />);

    await waitFor(() => expect(screen.getByTestId('tasks-truncated-banner')).toBeInTheDocument());
    expect(screen.getByText(/500 oldest open tasks/i)).toBeInTheDocument();
  });

  it('navigates to /cases/{caseId} when the case-id cell is activated (AC3)', async () => {
    const user = userEvent.setup();
    server.use(
      http.get('/api/tasks', () =>
        HttpResponse.json({
          data: { items: [taskFixture({ id: 't1', caseId: CASE_A })], truncated: false },
          meta: {},
        }),
      ),
    );

    renderWithProviders(
      <Routes>
        <Route path="/" element={<TasksPage />} />
        <Route path="/cases/:caseId" element={<div data-testid="case-detail">Case detail</div>} />
      </Routes>,
    );

    const link = await screen.findByRole('link', { name: CASE_A.slice(0, 8) + '…' });
    await user.click(link);

    await waitFor(() => expect(screen.getByTestId('case-detail')).toBeInTheDocument());
  });

  it('shows the error copy when the fetch fails', async () => {
    server.use(http.get('/api/tasks', () => HttpResponse.error()));

    renderWithProviders(<TasksPage />);

    await waitFor(() =>
      expect(screen.getByRole('alert')).toHaveTextContent(/could not load your tasks/i),
    );
  });
});
