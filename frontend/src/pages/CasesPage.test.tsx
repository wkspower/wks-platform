import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { HttpResponse, http } from 'msw';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, beforeEach } from 'vitest';

import { useUiStore } from '@/stores/uiStore';
import {
  buildCaseListFixture,
  loanApplicationCaseTypeView,
} from '@/test/fixtures/buildCaseListFixture';
import { renderWithProviders } from '@/test/renderWithProviders';
import { server } from '@/test/server';

import { CasesPage } from './CasesPage';

function setViewport(width: number) {
  Object.defineProperty(window, 'innerWidth', { value: width, configurable: true });
  Object.defineProperty(window, 'innerHeight', { value: 800, configurable: true });
}

const DEEP_LINK_ID = '00000000-0000-0000-0000-000000000001';

function envelope<T>(data: T) {
  return HttpResponse.json({ data, meta: {} });
}

beforeEach(() => {
  useUiStore.setState({
    caseListFilters: { caseTypeIds: [], statusIds: [], priorities: [] },
  });
  server.use(
    http.get('/api/case-types', () =>
      envelope([
        {
          id: 'loan-application',
          displayName: 'Loan Application',
          version: 1,
          statusCount: 4,
          fieldCount: 2,
        },
      ]),
    ),
    http.get('/api/case-types/:id', () => envelope(loanApplicationCaseTypeView())),
    http.get('/api/cases', ({ request }) => {
      const url = new URL(request.url);
      const ct = url.searchParams.get('caseType');
      if (ct !== 'loan-application') return envelope([]);
      return envelope(buildCaseListFixture(3));
    }),
  );
});

describe('CasesPage', () => {
  it('loads case-types, fetches cases, and renders the table', async () => {
    renderWithProviders(<CasesPage />, {
      initialAuth: {
        user: { id: '1', email: 'admin@wkspower.local', roles: ['admin'] },
      },
    });

    await waitFor(() => {
      expect(screen.getByRole('table', { name: /Cases table/i })).toBeInTheDocument();
    });
    // Wait for at least one data row beyond the header.
    await waitFor(() => {
      const rows = screen.getAllByRole('row');
      expect(rows.length).toBeGreaterThan(1);
    });
  });

  it('opens detail panel on /cases/:caseId deep link', async () => {
    setViewport(1400);
    server.use(
      http.get(`/api/cases/${DEEP_LINK_ID}`, () =>
        envelope({
          id: DEEP_LINK_ID,
          caseTypeId: 'loan-application',
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
          caseType: loanApplicationCaseTypeView(),
        }),
      ),
    );
    renderWithProviders(
      <Routes>
        <Route path="/cases" element={<CasesPage />} />
        <Route path="/cases/:caseId" element={<CasesPage />} />
      </Routes>,
      {
        initialPath: `/cases/${DEEP_LINK_ID}`,
        initialAuth: {
          user: { id: '1', email: 'admin@wkspower.local', roles: ['admin'] },
        },
      },
    );

    await waitFor(() => expect(screen.getByRole('heading', { name: /Case / })).toBeInTheDocument());
  });

  it('shows not-found state when deep-linked id does not resolve', async () => {
    setViewport(1400);
    server.use(
      http.get(`/api/cases/${DEEP_LINK_ID}`, () =>
        HttpResponse.json(
          { error: { code: 'WKS-API-404', message: 'nope', field: null }, meta: {} },
          { status: 404 },
        ),
      ),
    );
    renderWithProviders(
      <Routes>
        <Route path="/cases" element={<CasesPage />} />
        <Route path="/cases/:caseId" element={<CasesPage />} />
      </Routes>,
      {
        initialPath: `/cases/${DEEP_LINK_ID}`,
        initialAuth: {
          user: { id: '1', email: 'admin@wkspower.local', roles: ['admin'] },
        },
      },
    );

    await waitFor(() => expect(screen.getByText('Case not found')).toBeInTheDocument());
  });

  it('debounces search input and switches empty state to filtered when no rows match', async () => {
    const user = userEvent.setup();
    renderWithProviders(<CasesPage />, {
      initialAuth: {
        user: { id: '1', email: 'admin@wkspower.local', roles: ['admin'] },
      },
    });

    const search = await screen.findByRole('searchbox', { name: /Search cases/ });
    await user.type(search, 'zzz-no-match-zzz');

    await waitFor(
      () => {
        expect(screen.getByText(/No cases match current filters/)).toBeInTheDocument();
      },
      { timeout: 2000 },
    );
  });
});
