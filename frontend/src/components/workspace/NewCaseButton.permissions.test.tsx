import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import type { ReactElement } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import { caseTypeSummaryFixture } from '@/test/fixtures/buildCaseListFixture';
import { server } from '@/test/server';

import { NewCaseButton } from './NewCaseButton';

function renderWithProviders(ui: ReactElement) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <TooltipProvider delayDuration={0}>{ui}</TooltipProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('NewCaseButton — permission filter (AC9)', () => {
  it('only shows case types in the dropdown where the caller holds the `create` verb', async () => {
    const user = userEvent.setup();
    server.use(
      http.get('/api/case-types', () =>
        HttpResponse.json({
          data: [
            caseTypeSummaryFixture({
              id: 'a',
              displayName: 'Alpha',
              permissions: ['view', 'create'],
            }),
            caseTypeSummaryFixture({ id: 'b', displayName: 'Bravo', permissions: ['view'] }),
            caseTypeSummaryFixture({
              id: 'c',
              displayName: 'Charlie',
              permissions: ['view', 'create'],
            }),
          ],
          meta: {},
        }),
      ),
    );
    renderWithProviders(<NewCaseButton />);
    await user.click(await screen.findByRole('button', { name: /new case/i }));

    // Bravo (no create) must not appear in the dropdown.
    await waitFor(() => {
      expect(screen.getByRole('menuitem', { name: 'Alpha' })).toBeInTheDocument();
      expect(screen.getByRole('menuitem', { name: 'Charlie' })).toBeInTheDocument();
      expect(screen.queryByRole('menuitem', { name: 'Bravo' })).toBeNull();
    });
  });

  it('treats missing permissions array as no create access (defensive)', async () => {
    server.use(
      http.get('/api/case-types', () =>
        HttpResponse.json({
          data: [{ ...caseTypeSummaryFixture(), permissions: undefined }],
          meta: {},
        }),
      ),
    );
    renderWithProviders(<NewCaseButton />);
    await waitFor(() => {
      const btn = screen.getByRole('button');
      expect(btn).toBeDisabled();
    });
  });
});
