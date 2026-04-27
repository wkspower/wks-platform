import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import type { CaseDto } from '@/types/case';
import type { CaseTypeView } from '@/types/caseType';

import { PropertiesTab } from './PropertiesTab';

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
    ],
    statuses: [],
    listColumns: [],
    ...over,
  };
}

function caseDto(data: Record<string, unknown>): CaseDto {
  return {
    id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
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
    version: 1,
    caseType: ctView(),
  };
}

function renderTab(view: CaseTypeView, data: Record<string, unknown>) {
  return render(
    <TooltipProvider delayDuration={0}>
      <PropertiesTab caseDto={caseDto(data)} caseTypeView={view} />
    </TooltipProvider>,
  );
}

describe('PropertiesTab', () => {
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
    expect(dashes).toHaveLength(3);
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
