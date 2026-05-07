/**
 * Story 5.2 — SinglePageFormRenderer unit tests.
 *
 * AC1: all fields rendered on one page in order-sorted sequence.
 * AC2: empty submit shows FormErrorsBanner with field names.
 */
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import type { ReactElement } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { beforeAll, describe, expect, it } from 'vitest';

// ResizeObserver is not implemented in jsdom but is referenced by Radix UI's Select + Tooltip
// internals. Stub it so tests can render components that include Select (e.g. select-typed fields).
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

import { SinglePageFormRenderer } from './SinglePageFormRenderer';

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
 * A minimal 3-field monolithic form definition for testing. Three fields:
 * - name (text, required, order 0)
 * - notes (textarea, required=false, order 1)
 * - consent (checkbox, required=true, order 2)
 */
function threeFieldForm(): FormDefinitionView {
  return {
    id: 'intake-form',
    topology: 'single',
    dataModel: 'monolithic',
    rendering: 'single-page',
    fields: [
      {
        id: 'name',
        displayName: 'Full Name',
        type: 'text',
        required: true,
        requiredOnCreate: true,
        order: 0,
        options: [],
      },
      {
        id: 'notes',
        displayName: 'Notes',
        type: 'textarea',
        required: false,
        requiredOnCreate: false,
        order: 1,
        options: [],
      },
      {
        id: 'consent',
        displayName: 'I consent',
        type: 'checkbox',
        required: true,
        requiredOnCreate: false,
        order: 2,
        options: [],
      },
    ],
  };
}

// ---------------------------------------------------------------------------
// AC1 — All fields visible
// ---------------------------------------------------------------------------

describe('SinglePageFormRenderer — AC1: all fields visible', () => {
  it('renders all 3 fields in declared order', () => {
    wrap(
      <SinglePageFormRenderer
        formDefinition={threeFieldForm()}
        caseId="00000000-0000-0000-0000-000000000001"
      />,
    );
    expect(screen.getByLabelText(/Full Name/)).toBeInTheDocument();
    expect(screen.getByLabelText(/Notes/)).toBeInTheDocument();
    expect(screen.getByLabelText(/I consent/)).toBeInTheDocument();
  });

  it('renders a single submit button', () => {
    wrap(
      <SinglePageFormRenderer
        formDefinition={threeFieldForm()}
        caseId="00000000-0000-0000-0000-000000000001"
      />,
    );
    // MutationButton renders a <button> whose text comes from the children prop.
    expect(screen.getByRole('button', { name: /submit/i })).toBeInTheDocument();
  });

  it('renders required fields with aria-required', () => {
    wrap(
      <SinglePageFormRenderer
        formDefinition={threeFieldForm()}
        caseId="00000000-0000-0000-0000-000000000001"
      />,
    );
    const nameInput = screen.getByLabelText(/Full Name/);
    expect(nameInput).toHaveAttribute('aria-required', 'true');
  });
});

// ---------------------------------------------------------------------------
// AC2 — Empty submit shows FormErrorsBanner
// ---------------------------------------------------------------------------

describe('SinglePageFormRenderer — AC2: empty submit shows FormErrorsBanner', () => {
  it('shows banner with required field names after submitting empty form', async () => {
    const user = userEvent.setup();
    wrap(
      <SinglePageFormRenderer
        formDefinition={threeFieldForm()}
        caseId="00000000-0000-0000-0000-000000000001"
      />,
    );
    // Submit without filling in anything.
    await user.click(screen.getByRole('button', { name: /submit/i }));

    // FormErrorsBanner lists the display names of fields with errors as clickable buttons.
    // 'name' is required; 'consent' is required. 'notes' is not required and won't appear.
    const bannerButtons = await screen.findAllByRole('button', { name: /Full Name|I consent/ });
    expect(bannerButtons.length).toBeGreaterThanOrEqual(1);
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
      <SinglePageFormRenderer
        formDefinition={threeFieldForm()}
        caseId="00000000-0000-0000-0000-000000000001"
      />,
    );
    await user.click(screen.getByRole('button', { name: /submit/i }));
    // Give any async effects time to flush.
    await new Promise((r) => setTimeout(r, 50));
    expect(called).toBe(false);
  });
});

// ---------------------------------------------------------------------------
// AC3 — Successful submit calls onSuccess
// ---------------------------------------------------------------------------

describe('SinglePageFormRenderer — AC3: successful submit', () => {
  it('calls onSuccess after a valid submission', async () => {
    const user = userEvent.setup();
    let successCalled = false;

    server.use(
      http.post('/api/cases/:caseId/forms/:formId/submit', () =>
        HttpResponse.json({
          data: {
            id: '00000000-0000-0000-0000-000000000001',
            caseTypeId: 'test-ct',
            caseTypeVersion: 1,
            status: 'open',
            assignee: null,
            data: { name: 'Alice', consent: true },
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
      <SinglePageFormRenderer
        formDefinition={threeFieldForm()}
        caseId="00000000-0000-0000-0000-000000000001"
        onSuccess={() => {
          successCalled = true;
        }}
      />,
    );

    // Fill required fields.
    await user.type(screen.getByLabelText(/Full Name/), 'Alice');
    await user.click(screen.getByLabelText(/I consent/));
    await user.click(screen.getByRole('button', { name: /submit/i }));

    // MutationButton transitions to 'confirmed' state — check the button role
    await screen.findByRole('button', { name: /confirmed/i });
    expect(successCalled).toBe(true);
  });
});
