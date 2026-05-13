import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import type { ReactNode } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import { server } from '@/test/server';
import type { CaseDto } from '@/types/case';

import { CaseDetailPanel } from './CaseDetailPanel';

const ID = '11111111-2222-3333-4444-555555555555';
const dto: CaseDto = {
  id: ID,
  caseTypeId: 'loan',
  caseTypeVersion: 1,
  status: 'open',
  assignee: null,
  data: {},
  processInstanceId: null,
  documentCount: 0,
  createdAt: '2026-04-01T00:00:00Z',
  createdBy: null,
  updatedAt: '2026-04-02T00:00:00Z',
  version: 1,
  caseType: {
    id: 'loan',
    displayName: 'Loan',
    version: 1,
    fields: [],
    statuses: [],
    listColumns: [],
  },
  stages: [],
};

function wrap(ui: ReactNode) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <MemoryRouter>
      <QueryClientProvider client={client}>
        <TooltipProvider delayDuration={0}>{ui}</TooltipProvider>
      </QueryClientProvider>
    </MemoryRouter>,
  );
}

describe('CaseDetailPanel tabs', () => {
  it('defaults to Properties; switches on click', async () => {
    server.use(http.get(`/api/cases/${ID}`, () => HttpResponse.json({ data: dto, meta: {} })));
    const user = userEvent.setup();
    wrap(<CaseDetailPanel caseId={ID} onClose={() => undefined} />);

    const properties = await screen.findByRole('tab', { name: 'Properties' });
    await waitFor(() => expect(properties).toHaveAttribute('data-state', 'active'));

    server.use(
      http.get(`/api/cases/${ID}/documents`, () => HttpResponse.json({ data: [], meta: {} })),
    );
    await user.click(screen.getByRole('tab', { name: 'Documents' }));
    const documents = screen.getByRole('tab', { name: 'Documents' });
    await waitFor(() => expect(documents).toHaveAttribute('data-state', 'active'));
    expect(screen.getByTestId('documents-tab')).toBeInTheDocument();
  });

  it('renders the correct panel per tab', async () => {
    server.use(
      http.get(`/api/cases/${ID}`, () => HttpResponse.json({ data: dto, meta: {} })),
      // Story 9-2 — Activity tab now fetches the audit feed instead of rendering a placeholder.
      http.get(`/api/cases/${ID}/audit-events`, () =>
        HttpResponse.json({ data: { items: [], truncated: false }, meta: {} }),
      ),
    );
    const user = userEvent.setup();
    wrap(<CaseDetailPanel caseId={ID} onClose={() => undefined} />);

    await screen.findByRole('tab', { name: 'Properties' });
    await user.click(screen.getByRole('tab', { name: 'Activity' }));
    // Empty-state assertion: 0 audit events → confidence-frame empty copy renders.
    expect(await screen.findByTestId('activity-empty')).toBeInTheDocument();
  });
});
