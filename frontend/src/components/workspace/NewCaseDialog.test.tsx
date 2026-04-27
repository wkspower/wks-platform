import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import type { ReactElement } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import { loanApplicationCaseTypeView } from '@/test/fixtures/buildCaseListFixture';
import { server } from '@/test/server';

import { NewCaseDialog } from './NewCaseDialog';

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

describe('NewCaseDialog — render shape (AC2/AC3)', () => {
  it('renders one FormField per requiredOnCreate field, in order', () => {
    const view = loanApplicationCaseTypeView();
    renderWithProviders(<NewCaseDialog open caseType={view} onOpenChange={() => {}} />);
    // Two requiredOnCreate fields in fixture: applicant_name (text), amount (number)
    expect(screen.getByLabelText(/Applicant/)).toBeInTheDocument();
    expect(screen.getByLabelText(/Amount/)).toBeInTheDocument();
  });

  it('shows the empty-state copy when the case type has zero requiredOnCreate fields', () => {
    const view = loanApplicationCaseTypeView();
    view.fields = view.fields.map((f) => ({ ...f, requiredOnCreate: false }));
    renderWithProviders(<NewCaseDialog open caseType={view} onOpenChange={() => {}} />);
    expect(screen.getByText(/no fields are required/i)).toBeInTheDocument();
  });

  it('returns null and does not mount when caseType is null', () => {
    renderWithProviders(<NewCaseDialog open caseType={null} onOpenChange={() => {}} />);
    expect(screen.queryByRole('dialog')).toBeNull();
  });
});

describe('NewCaseDialog — submit success path (AC7)', () => {
  it('on success: closes the dialog and primes the navigation', async () => {
    const user = userEvent.setup();
    const view = loanApplicationCaseTypeView();
    let openState = true;
    server.use(
      http.post('/api/cases', () =>
        HttpResponse.json({
          data: {
            id: '11111111-2222-3333-4444-555555555555',
            caseTypeId: view.id,
            caseTypeVersion: view.version,
            status: 'open',
            assignee: null,
            data: { applicant_name: 'Asha', amount: 5000 },
            processInstanceId: null,
            documentCount: 0,
            createdAt: '2026-04-01T00:00:00Z',
            createdBy: null,
            updatedAt: '2026-04-01T00:00:00Z',
            version: 1,
            caseType: view,
          },
          meta: {},
        }),
      ),
    );
    renderWithProviders(
      <NewCaseDialog
        open
        caseType={view}
        onOpenChange={(next) => {
          openState = next;
        }}
      />,
    );
    await user.type(screen.getByLabelText(/Applicant/), 'Asha');
    await user.type(screen.getByLabelText(/Amount/), '5000');
    await user.click(screen.getByRole('button', { name: /create case/i }));
    await waitFor(() => expect(openState).toBe(false));
  });
});

describe('NewCaseDialog — submit failure paths (AC5/P2/P4)', () => {
  it('422 with field error maps the message onto the matching FormField', async () => {
    const user = userEvent.setup();
    const view = loanApplicationCaseTypeView();
    server.use(
      http.post('/api/cases', () =>
        HttpResponse.json(
          {
            error: {
              code: 'WKS-API-001',
              message: 'Validation failed',
              errors: [{ code: 'WKS-API-001', message: 'Amount too high', field: 'amount' }],
            },
          },
          { status: 422 },
        ),
      ),
    );
    renderWithProviders(<NewCaseDialog open caseType={view} onOpenChange={() => {}} />);
    await user.type(screen.getByLabelText(/Applicant/), 'Asha');
    await user.type(screen.getByLabelText(/Amount/), '999999');
    await user.click(screen.getByRole('button', { name: /create case/i }));
    await waitFor(() => expect(screen.getByText('Amount too high')).toBeInTheDocument());
  });

  it('renders the multi-field errors banner with anchor links after submit (Story 2.8 AC9)', async () => {
    const user = userEvent.setup();
    const view = loanApplicationCaseTypeView();
    renderWithProviders(<NewCaseDialog open caseType={view} onOpenChange={() => {}} />);
    // Submit empty — both required fields fail at once.
    await user.click(screen.getByRole('button', { name: /create case/i }));
    await waitFor(() => expect(screen.getByText(/2 fields need attention/i)).toBeInTheDocument());
    // Anchor links exist for each failing field, with role=button (FormErrorsBanner uses buttons).
    expect(screen.getAllByRole('button', { name: /Applicant/i }).length).toBeGreaterThan(0);
    expect(screen.getAllByRole('button', { name: /Amount/i }).length).toBeGreaterThan(0);
  });

  it('403 surfaces auth-specific banner copy (P4)', async () => {
    const user = userEvent.setup();
    const view = loanApplicationCaseTypeView();
    server.use(
      http.post('/api/cases', () =>
        HttpResponse.json({ error: { code: 'WKS-API-403', message: 'denied' } }, { status: 403 }),
      ),
    );
    renderWithProviders(<NewCaseDialog open caseType={view} onOpenChange={() => {}} />);
    await user.type(screen.getByLabelText(/Applicant/), 'Asha');
    await user.type(screen.getByLabelText(/Amount/), '500');
    await user.click(screen.getByRole('button', { name: /create case/i }));
    await waitFor(() => expect(screen.getByText(/no longer have permission/i)).toBeInTheDocument());
  });
});
