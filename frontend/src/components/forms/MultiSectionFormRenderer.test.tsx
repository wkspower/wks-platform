/**
 * Story 5.3 — MultiSectionFormRenderer unit tests.
 *
 * AC1: sections render as expandable panels with labels and status indicators.
 * AC2: submit blocked and focuses first error section.
 * AC4: progress indicator shows "X of Y sections complete".
 */
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import type { ReactElement } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { beforeAll, describe, expect, it, vi } from 'vitest';

// ResizeObserver stub for Radix UI Select internals
beforeAll(() => {
  if (typeof window !== 'undefined' && !window.ResizeObserver) {
    window.ResizeObserver = class ResizeObserver {
      observe() {}
      unobserve() {}
      disconnect() {}
    };
  }
});

import { TooltipProvider } from '@/components/ui/Tooltip';
import { server } from '@/test/server';
import type { FormDefinitionView } from '@/types/caseType';

import { MultiSectionFormRenderer } from './MultiSectionFormRenderer';

function wrap(ui: ReactElement) {
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

/**
 * Two-section sectioned form for testing.
 * Section "personal": firstName (text, required), lastName (text, required)
 * Section "employment": employer (text, not required)
 */
function twoSectionForm(): FormDefinitionView {
  return {
    id: 'bank-opening-form',
    topology: 'single',
    dataModel: 'sectioned',
    rendering: 'multi-section',
    fields: [],
    sections: [
      {
        id: 'personal',
        label: 'Personal Information',
        fields: [
          {
            id: 'firstName',
            displayName: 'First Name',
            type: 'text',
            required: true,
            requiredOnCreate: true,
            order: 1,
            options: [],
          },
          {
            id: 'lastName',
            displayName: 'Last Name',
            type: 'text',
            required: true,
            requiredOnCreate: true,
            order: 2,
            options: [],
          },
        ],
      },
      {
        id: 'employment',
        label: 'Employment Details',
        fields: [
          {
            id: 'employer',
            displayName: 'Employer',
            type: 'text',
            required: false,
            requiredOnCreate: false,
            order: 1,
            options: [],
          },
        ],
      },
    ],
  };
}

// ---------------------------------------------------------------------------
// AC1 — Sections render as expandable panels
// ---------------------------------------------------------------------------

describe('MultiSectionFormRenderer — AC1: sections render as expandable panels', () => {
  it('renders both section headings', () => {
    wrap(
      <MultiSectionFormRenderer
        formDefinition={twoSectionForm()}
        caseId="00000000-0000-0000-0000-000000000001"
      />,
    );
    expect(screen.getByText('Personal Information')).toBeInTheDocument();
    expect(screen.getByText('Employment Details')).toBeInTheDocument();
  });

  it('renders all fields inside sections by default (sections start expanded)', () => {
    wrap(
      <MultiSectionFormRenderer
        formDefinition={twoSectionForm()}
        caseId="00000000-0000-0000-0000-000000000001"
      />,
    );
    expect(screen.getByLabelText(/First Name/)).toBeInTheDocument();
    expect(screen.getByLabelText(/Last Name/)).toBeInTheDocument();
    expect(screen.getByLabelText(/Employer/)).toBeInTheDocument();
  });

  it('collapses a section when the section header is clicked', async () => {
    const user = userEvent.setup();
    wrap(
      <MultiSectionFormRenderer
        formDefinition={twoSectionForm()}
        caseId="00000000-0000-0000-0000-000000000001"
      />,
    );
    // Click the "Personal Information" section header to collapse it
    await user.click(screen.getByRole('button', { name: /Personal Information/i }));
    // Fields inside that section should no longer be visible
    expect(screen.queryByLabelText(/First Name/)).not.toBeInTheDocument();
    expect(screen.queryByLabelText(/Last Name/)).not.toBeInTheDocument();
    // Employment section remains open
    expect(screen.getByLabelText(/Employer/)).toBeInTheDocument();
  });

  it('renders a single submit button', () => {
    wrap(
      <MultiSectionFormRenderer
        formDefinition={twoSectionForm()}
        caseId="00000000-0000-0000-0000-000000000001"
      />,
    );
    expect(screen.getByRole('button', { name: /submit/i })).toBeInTheDocument();
  });

  it('renders initial progress indicator "0 of 2 sections complete"', () => {
    wrap(
      <MultiSectionFormRenderer
        formDefinition={twoSectionForm()}
        caseId="00000000-0000-0000-0000-000000000001"
      />,
    );
    expect(screen.getByText(/0 of 2 sections complete/i)).toBeInTheDocument();
  });
});

// ---------------------------------------------------------------------------
// AC2 — Submit blocked on validation errors + error indicator
// ---------------------------------------------------------------------------

describe('MultiSectionFormRenderer — AC2: submit blocked when required fields empty', () => {
  it('shows FormErrorsBanner with required field names after submitting empty form', async () => {
    const user = userEvent.setup();
    wrap(
      <MultiSectionFormRenderer
        formDefinition={twoSectionForm()}
        caseId="00000000-0000-0000-0000-000000000001"
      />,
    );
    await user.click(screen.getByRole('button', { name: /submit/i }));
    // Both required fields (firstName, lastName) should appear in the banner
    const bannerButtons = await screen.findAllByRole('button', { name: /First Name|Last Name/ });
    expect(bannerButtons.length).toBeGreaterThanOrEqual(1);
  });

  it('shows error indicator (✗) on section with errors after submit', async () => {
    const user = userEvent.setup();
    wrap(
      <MultiSectionFormRenderer
        formDefinition={twoSectionForm()}
        caseId="00000000-0000-0000-0000-000000000001"
      />,
    );
    await user.click(screen.getByRole('button', { name: /submit/i }));
    // Wait for validation to run
    await screen.findAllByRole('button', { name: /First Name|Last Name/ });
    // Error indicator should be present for "personal" section
    const errorIndicators = screen.getAllByLabelText('error');
    expect(errorIndicators.length).toBeGreaterThan(0);
  });

  it('HTTP call is NOT fired when validation fails', async () => {
    const user = userEvent.setup();
    let called = false;
    server.use(
      http.post('/api/cases/:caseId/forms/:formId/submit', () => {
        called = true;
        return HttpResponse.json({ data: {} });
      }),
    );
    wrap(
      <MultiSectionFormRenderer
        formDefinition={twoSectionForm()}
        caseId="00000000-0000-0000-0000-000000000001"
      />,
    );
    await user.click(screen.getByRole('button', { name: /submit/i }));
    await new Promise((r) => setTimeout(r, 50));
    expect(called).toBe(false);
  });
});

// ---------------------------------------------------------------------------
// AC4 — Progress indicator updates on successful submit
// ---------------------------------------------------------------------------

describe('MultiSectionFormRenderer — AC4: progress indicator', () => {
  it('calls onSuccess after a valid submission and shows confirmed state', async () => {
    const user = userEvent.setup();
    let successCalled = false;
    vi.useFakeTimers({ shouldAdvanceTime: true });

    server.use(
      http.post('/api/cases/:caseId/forms/:formId/submit', () =>
        HttpResponse.json({
          data: {
            id: '00000000-0000-0000-0000-000000000001',
            caseTypeId: 'test-ct',
            caseTypeVersion: 1,
            status: 'open',
            assignee: null,
            data: { firstName: 'Alice', lastName: 'Smith' },
            processInstanceId: null,
            documentCount: 0,
            createdAt: new Date().toISOString(),
            createdBy: '00000000-0000-0000-0000-000000000099',
            updatedAt: new Date().toISOString(),
            version: 1,
            caseType: {
              id: 'test-ct',
              displayName: 'Test CT',
              version: 1,
              fields: [],
              statuses: [],
              listColumns: [],
              stages: [],
              forms: [],
            },
            currentStageId: null,
            currentStageOrdinal: null,
            stages: [],
            availableStatuses: [],
          },
          meta: {},
        }),
      ),
    );

    wrap(
      <MultiSectionFormRenderer
        formDefinition={twoSectionForm()}
        caseId="00000000-0000-0000-0000-000000000001"
        onSuccess={() => {
          successCalled = true;
        }}
      />,
    );

    // Fill required fields
    await user.type(screen.getByLabelText(/First Name/), 'Alice');
    await user.type(screen.getByLabelText(/Last Name/), 'Smith');
    await user.click(screen.getByRole('button', { name: /submit/i }));

    // MutationButton transitions to 'confirmed' state
    await screen.findByRole('button', { name: /confirmed/i });

    // onSuccess is NOT called immediately (CF1 delay)
    expect(successCalled).toBe(false);

    // Advance timers past 1200ms delay
    vi.advanceTimersByTime(1300);
    expect(successCalled).toBe(true);

    vi.useRealTimers();
  });
});
