import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { MemoryRouter, Route, Routes, useNavigate, useParams } from 'react-router-dom';
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

function HostedDetail() {
  const { caseId } = useParams<{ caseId: string }>();
  const navigate = useNavigate();
  if (!caseId) return null;
  return <CaseDetailPanel caseId={caseId} onClose={() => navigate('/cases')} />;
}

function ListPage() {
  return <div data-testid="list-page">cases list</div>;
}

function setup(initialPath: string) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <QueryClientProvider client={client}>
        <TooltipProvider delayDuration={0}>
          <Routes>
            <Route path="/cases" element={<ListPage />} />
            <Route path="/cases/:caseId" element={<HostedDetail />} />
          </Routes>
        </TooltipProvider>
      </QueryClientProvider>
    </MemoryRouter>,
  );
}

describe('CaseDetailPanel routing', () => {
  it('mounts directly from /cases/:caseId deep link and renders heading', async () => {
    server.use(http.get(`/api/cases/${ID}`, () => HttpResponse.json({ data: dto, meta: {} })));
    setup(`/cases/${ID}`);
    expect(await screen.findByRole('heading', { name: /Case / })).toBeInTheDocument();
  });

  it('shows not-found on bad id deep link without redirecting', async () => {
    server.use(
      http.get(`/api/cases/${ID}`, () =>
        HttpResponse.json(
          { error: { code: 'WKS-API-404', message: 'nope', field: null }, meta: {} },
          { status: 404 },
        ),
      ),
    );
    setup(`/cases/${ID}`);
    expect(await screen.findByText('Case not found')).toBeInTheDocument();
    expect(screen.queryByTestId('list-page')).not.toBeInTheDocument();
  });
});
