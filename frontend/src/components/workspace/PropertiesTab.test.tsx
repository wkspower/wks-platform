import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it, vi } from 'vitest';

import { renderWithProviders } from '@/test/renderWithProviders';
import { server } from '@/test/server';
import type { CaseDto } from '@/types/case';
import type { CaseTypeView } from '@/types/caseType';

import { PropertiesTab } from './PropertiesTab';

const CASE_ID = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';

function ctView(over: Partial<CaseTypeView> = {}): CaseTypeView {
  return {
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
      {
        id: 'amount',
        displayName: 'Amount',
        type: 'number',
        required: false,
        order: 1,
        options: [],
      },
      {
        id: 'priority',
        displayName: 'Priority',
        type: 'select',
        required: false,
        order: 2,
        options: [
          { value: 'low', label: 'Low' },
          { value: 'high', label: 'High' },
        ],
      },
      {
        id: 'attachment',
        displayName: 'Attachment',
        type: 'file',
        required: false,
        order: 3,
        options: [],
      },
    ],
    statuses: [],
    listColumns: [],
    ...over,
  };
}

function caseDto(data: Record<string, unknown>, version = 1): CaseDto {
  return {
    id: CASE_ID,
    caseTypeId: 'loan',
    caseTypeVersion: 1,
    status: 'open',
    assignee: null,
    data,
    processInstanceId: null,
    documentCount: 0,
    createdAt: '2026-04-01T00:00:00Z',
    createdBy: null,
    updatedAt: '2026-04-02T00:00:00Z',
    version,
    caseType: ctView(),
    stages: [],
  };
}

function renderTab(view: CaseTypeView, data: Record<string, unknown>, version = 1) {
  return renderWithProviders(
    <PropertiesTab caseDto={caseDto(data, version)} caseTypeView={view} />,
  );
}

describe('PropertiesTab — read', () => {
  it('renders one dt/dd per field', () => {
    renderTab(ctView(), { applicant_name: 'Asha', amount: 1500, priority: 'high' });
    expect(screen.getByText('Applicant')).toBeInTheDocument();
    expect(screen.getByText('Asha')).toBeInTheDocument();
    expect(screen.getByText('Amount')).toBeInTheDocument();
    expect(screen.getByText('High')).toBeInTheDocument();
  });

  it('renders em-dash for null/undefined/empty values', () => {
    const { container } = renderTab(ctView(), {
      applicant_name: null,
      amount: '',
      priority: undefined,
    });
    const dashes = Array.from(container.querySelectorAll('dd')).filter((dd) =>
      dd.textContent?.includes('—'),
    );
    expect(dashes.length).toBeGreaterThanOrEqual(3);
  });

  it('renders the empty-state when fields[] is empty', () => {
    renderTab(ctView({ fields: [] }), {});
    expect(screen.getByText('This case type has no fields defined.')).toBeInTheDocument();
  });

  it('select field uses option.label, not raw value', () => {
    renderTab(ctView(), { priority: 'low' });
    expect(screen.getByText('Low')).toBeInTheDocument();
    expect(screen.queryByText('low')).not.toBeInTheDocument();
  });
});

describe('PropertiesTab — inline edit', () => {
  it('does NOT render an edit affordance for file fields', () => {
    renderTab(ctView(), { applicant_name: 'Asha' });
    expect(screen.queryByLabelText('Edit Attachment')).not.toBeInTheDocument();
    expect(screen.getByLabelText('Edit Applicant')).toBeInTheDocument();
  });

  it('clicking pencil swaps the value cell for an input, then Cancel restores it', async () => {
    const user = userEvent.setup();
    renderTab(ctView(), { applicant_name: 'Asha' });

    await user.click(screen.getByLabelText('Edit Applicant'));
    const input = screen.getByRole('textbox', { name: 'Applicant' });
    expect(input).toHaveValue('Asha');

    await user.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(screen.queryByRole('textbox', { name: 'Applicant' })).not.toBeInTheDocument();
    expect(screen.getByText('Asha')).toBeInTheDocument();
  });

  it('Save sends PUT with merged data + current version and exits edit mode', async () => {
    const seen = vi.fn<(body: unknown) => void>();
    server.use(
      http.put(`/api/cases/${CASE_ID}`, async ({ request }) => {
        const body = await request.json();
        seen(body);
        return HttpResponse.json({
          data: {
            id: CASE_ID,
            caseTypeId: 'loan',
            caseTypeVersion: 1,
            status: 'open',
            assignee: null,
            data: (body as { data: Record<string, unknown> }).data,
            processInstanceId: null,
            documentCount: 0,
            createdAt: '2026-04-01T00:00:00Z',
            createdBy: null,
            updatedAt: '2026-04-02T00:00:00Z',
            version: 2,
            caseType: ctView(),
            stages: [],
          },
          meta: {},
        });
      }),
    );
    const user = userEvent.setup();
    renderTab(ctView(), { applicant_name: 'Asha', amount: 1500 }, 1);

    await user.click(screen.getByLabelText('Edit Applicant'));
    const input = screen.getByRole('textbox', { name: 'Applicant' });
    await user.clear(input);
    await user.type(input, 'Bhavna');
    await user.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() => {
      expect(seen).toHaveBeenCalledWith({
        data: { applicant_name: 'Bhavna', amount: 1500 },
        version: 1,
      });
    });
    await waitFor(() =>
      expect(screen.queryByRole('textbox', { name: 'Applicant' })).not.toBeInTheDocument(),
    );
  });

  it('surfaces the conflict message on 409 and stays in edit mode', async () => {
    server.use(
      http.put(`/api/cases/${CASE_ID}`, () =>
        HttpResponse.json(
          { error: { code: 'WKS-API-409', message: 'stale' }, meta: {} },
          { status: 409 },
        ),
      ),
    );
    const user = userEvent.setup();
    renderTab(ctView(), { applicant_name: 'Asha' });

    await user.click(screen.getByLabelText('Edit Applicant'));
    await user.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() =>
      expect(screen.getByRole('alert')).toHaveTextContent(
        'Case changed elsewhere — refresh to pick up the latest version.',
      ),
    );
    // Editor still mounted.
    expect(screen.getByRole('textbox', { name: 'Applicant' })).toBeInTheDocument();
  });
});
