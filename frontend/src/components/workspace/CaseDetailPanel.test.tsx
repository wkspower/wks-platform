import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import type { ReactNode } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';

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
  data: { applicant_name: 'Asha' },
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
    fields: [
      {
        id: 'applicant_name',
        displayName: 'Applicant',
        type: 'text',
        required: true,
        order: 0,
        options: [],
      },
    ],
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

describe('CaseDetailPanel', () => {
  it('renders loading skeleton while fetching', () => {
    server.use(
      http.get(`/api/cases/${ID}`, async () => {
        await new Promise((r) => setTimeout(r, 100));
        return HttpResponse.json({ data: dto, meta: {} });
      }),
    );
    const { container } = wrap(<CaseDetailPanel caseId={ID} onClose={() => undefined} />);
    expect(container.querySelectorAll('[class*="animate-pulse"]').length).toBeGreaterThan(0);
  });

  it('renders not-found state on 404 with back-to-list link', async () => {
    server.use(
      http.get(`/api/cases/${ID}`, () =>
        HttpResponse.json(
          { error: { code: 'WKS-API-404', message: 'not found', field: null }, meta: {} },
          { status: 404 },
        ),
      ),
    );
    wrap(<CaseDetailPanel caseId={ID} onClose={() => undefined} />);
    expect(await screen.findByText('Case not found')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Back to all cases' })).toHaveAttribute(
      'href',
      '/cases',
    );
  });

  it('renders forbidden state on 403', async () => {
    server.use(
      http.get(`/api/cases/${ID}`, () =>
        HttpResponse.json(
          { error: { code: 'WKS-API-403', message: 'forbidden', field: null }, meta: {} },
          { status: 403 },
        ),
      ),
    );
    wrap(<CaseDetailPanel caseId={ID} onClose={() => undefined} />);
    expect(await screen.findByText("You don't have access to this case")).toBeInTheDocument();
  });

  it('renders generic error with retry on 5xx', async () => {
    server.use(
      http.get(`/api/cases/${ID}`, () =>
        HttpResponse.json(
          { error: { code: 'WKS-API-500', message: 'boom', field: null }, meta: {} },
          { status: 500 },
        ),
      ),
    );
    wrap(<CaseDetailPanel caseId={ID} onClose={() => undefined} />);
    expect(await screen.findByText("Couldn't load case detail")).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Retry' })).toBeInTheDocument();
  });

  it('close button calls onClose', async () => {
    server.use(http.get(`/api/cases/${ID}`, () => HttpResponse.json({ data: dto, meta: {} })));
    const user = userEvent.setup();
    const onClose = vi.fn();
    wrap(<CaseDetailPanel caseId={ID} onClose={onClose} />);
    const closeBtn = await screen.findByRole('button', { name: 'Close case detail' });
    await user.click(closeBtn);
    expect(onClose).toHaveBeenCalledOnce();
  });

  it('preserves close button on error states', async () => {
    server.use(
      http.get(`/api/cases/${ID}`, () =>
        HttpResponse.json(
          { error: { code: 'WKS-API-500', message: 'boom', field: null }, meta: {} },
          { status: 500 },
        ),
      ),
    );
    wrap(<CaseDetailPanel caseId={ID} onClose={() => undefined} />);
    await waitFor(() =>
      expect(screen.getByRole('button', { name: 'Close case detail' })).toBeInTheDocument(),
    );
  });
});
