import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { HttpResponse, http } from 'msw';
import { describe, expect, it, beforeEach } from 'vitest';

import { useUiStore } from '@/stores/uiStore';
import {
  buildCaseListFixture,
  loanApplicationCaseTypeView,
} from '@/test/fixtures/buildCaseListFixture';
import { renderWithProviders } from '@/test/renderWithProviders';
import { server } from '@/test/server';

import { CasesPage } from './CasesPage';

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
