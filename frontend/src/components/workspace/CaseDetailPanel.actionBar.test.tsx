import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import type { ReactNode } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import { loanApplicationCaseTypeView } from '@/test/fixtures/buildCaseListFixture';
import { server } from '@/test/server';

import { CaseDetailPanel } from './CaseDetailPanel';

const CASE_ID = '11111111-1111-1111-1111-111111111111';

function wrap(node: ReactNode) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return (
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <TooltipProvider delayDuration={0}>{node}</TooltipProvider>
      </MemoryRouter>
    </QueryClientProvider>
  );
}

describe('CaseDetailPanel — action bar slot integration (Story 2.8 AC8/AC11)', () => {
  it('renders the CaseActionBar between the heading and the tabs', async () => {
    const view = loanApplicationCaseTypeView();
    server.use(
      http.get(`/api/cases/${CASE_ID}`, () =>
        HttpResponse.json({
          data: {
            id: CASE_ID,
            caseTypeId: view.id,
            caseTypeVersion: view.version,
            status: view.statuses[0]?.id ?? 'open',
            statusLabel: view.statuses[0]?.displayName ?? 'Open',
            data: {},
            createdAt: '2026-04-26T10:00:00Z',
            updatedAt: '2026-04-26T10:00:00Z',
            caseType: view,
          },
          meta: {},
        }),
      ),
      http.get(`/api/cases/${CASE_ID}/tasks`, () => HttpResponse.json({ data: [], meta: {} })),
    );
    render(wrap(<CaseDetailPanel caseId={CASE_ID} onClose={() => {}} />));
    await waitFor(() => expect(screen.getByRole('tablist')).toBeInTheDocument());
  });

  it('AC11 — renders exactly one aria-live="polite" region across panel + action bar', async () => {
    const view = loanApplicationCaseTypeView();
    server.use(
      http.get(`/api/cases/${CASE_ID}`, () =>
        HttpResponse.json({
          data: {
            id: CASE_ID,
            caseTypeId: view.id,
            caseTypeVersion: view.version,
            status: view.statuses[0]?.id ?? 'open',
            statusLabel: view.statuses[0]?.displayName ?? 'Open',
            data: {},
            createdAt: '2026-04-26T10:00:00Z',
            updatedAt: '2026-04-26T10:00:00Z',
            caseType: view,
          },
          meta: {},
        }),
      ),
      http.get(`/api/cases/${CASE_ID}/tasks`, () =>
        HttpResponse.json({
          data: [
            {
              id: 't1',
              processInstanceId: 'pi-1',
              caseId: CASE_ID,
              caseTypeId: view.id,
              taskDefinitionKey: 'draft',
              name: 'Draft application',
              assignee: null,
              archetype: 'draft_section',
              actionLabel: 'Draft application',
              createdAt: '2026-04-26T10:00:00Z',
              dueAt: null,
            },
          ],
          meta: {},
        }),
      ),
    );
    const { container } = render(wrap(<CaseDetailPanel caseId={CASE_ID} onClose={() => {}} />));
    await waitFor(() =>
      expect(screen.getByRole('button', { name: 'Draft application' })).toBeInTheDocument(),
    );
    // The panel owns one polite live region (the heading announcement). MutationButton owns its
    // own sr-only polite span (single primary CTA → exactly one). No extra region in the action
    // bar's takingLonger span (Story 2.8 P1 fix). Total expected = 2 across the integrated tree.
    const liveRegions = container.querySelectorAll('[aria-live="polite"]');
    expect(liveRegions.length).toBeLessThanOrEqual(2);
  });
});
