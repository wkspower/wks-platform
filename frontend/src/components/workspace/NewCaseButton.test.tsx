import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import type { ReactElement } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import {
  caseTypeSummaryFixture,
  loanApplicationCaseTypeView,
} from '@/test/fixtures/buildCaseListFixture';
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

describe('NewCaseButton — render states (AC1)', () => {
  it('renders a skeleton placeholder while case-types are loading (P23)', () => {
    server.use(
      http.get('/api/case-types', () => new Promise(() => {})), // never resolves
    );
    renderWithProviders(<NewCaseButton />);
    // Skeleton renders as an aria-hidden span (no role=button) — the placeholder, not a real
    // disabled button.
    expect(screen.queryByRole('button')).toBeNull();
    expect(document.querySelector('[aria-hidden].animate-pulse')).toBeInTheDocument();
  });

  it('renders a disabled button + tooltip when the user has create on zero case types', async () => {
    server.use(
      http.get('/api/case-types', () =>
        HttpResponse.json({
          data: [caseTypeSummaryFixture({ permissions: ['view'] })],
          meta: {},
        }),
      ),
    );
    renderWithProviders(<NewCaseButton />);
    await waitFor(() => {
      const btn = screen.getByRole('button');
      expect(btn).toBeDisabled();
      expect(btn).toHaveAttribute('aria-disabled', 'true');
    });
  });

  it('opens the dialog directly when there is exactly one creatable case type', async () => {
    const user = userEvent.setup();
    const view = loanApplicationCaseTypeView();
    server.use(
      http.get('/api/case-types', () =>
        HttpResponse.json({ data: [caseTypeSummaryFixture()], meta: {} }),
      ),
      http.get(`/api/case-types/${view.id}`, () => HttpResponse.json({ data: view, meta: {} })),
    );
    renderWithProviders(<NewCaseButton />);
    await user.click(await screen.findByRole('button', { name: /new case/i }));
    await waitFor(() => expect(screen.getByRole('dialog')).toBeInTheDocument());
    expect(screen.getByText(/New Loan Application/i)).toBeInTheDocument();
  });
});
