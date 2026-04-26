import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import { useUiStore } from '@/stores/uiStore';
import type { CaseTypeSummary, CaseTypeView } from '@/types/caseType';

import { CaseFilterBar } from './CaseFilterBar';

const caseTypes: CaseTypeSummary[] = [
  { id: 'loan', displayName: 'Loan', version: 1, statusCount: 2, fieldCount: 2 },
  { id: 'kyc', displayName: 'KYC', version: 1, statusCount: 2, fieldCount: 2 },
];

const view: CaseTypeView = {
  id: 'loan',
  displayName: 'Loan',
  version: 1,
  fields: [],
  statuses: [
    { id: 'open', displayName: 'Open', color: 'blue' },
    { id: 'closed', displayName: 'Closed', color: 'emerald' },
  ],
  listColumns: [],
};

afterEach(() => {
  useUiStore.getState().clearCaseListFilters();
});

function setup(props?: { variant?: 'full-width' | 'narrowed' }) {
  return render(
    <TooltipProvider delayDuration={0}>
      <CaseFilterBar
        caseTypes={caseTypes}
        selectedCaseTypeViews={[view]}
        searchInput=""
        onSearchInputChange={() => undefined}
        variant={props?.variant ?? 'narrowed'}
      />
    </TooltipProvider>,
  );
}

describe('CaseFilterBar narrowed variant', () => {
  it('renders Add-filter ghost button when no filters are active', () => {
    setup();
    expect(screen.getByRole('button', { name: 'Add filter' })).toBeInTheDocument();
    expect(screen.queryByTestId('narrowed-filter-pill')).not.toBeInTheDocument();
  });

  it('collapses chips to "N filters" pill when filters are active', () => {
    useUiStore.getState().setCaseListFilters({
      caseTypeIds: ['loan'],
      statusIds: ['open', 'closed'],
      priorities: [],
    });
    setup();
    const pill = screen.getByTestId('narrowed-filter-pill');
    expect(pill).toHaveTextContent('3 filters');
  });

  it('opens popover with chips on click and removes a filter', async () => {
    useUiStore.getState().setCaseListFilters({
      caseTypeIds: ['loan'],
      statusIds: [],
      priorities: [],
    });
    setup();
    const user = userEvent.setup();
    await user.click(screen.getByTestId('narrowed-filter-pill'));
    const removeBtn = await screen.findByRole('button', { name: /Remove filter Case type: Loan/ });
    await user.click(removeBtn);
    expect(useUiStore.getState().caseListFilters.caseTypeIds).toEqual([]);
  });

  it('full-width variant still renders chips inline (regression for 2.5)', () => {
    useUiStore.getState().setCaseListFilters({
      caseTypeIds: ['loan'],
      statusIds: [],
      priorities: [],
    });
    setup({ variant: 'full-width' });
    expect(screen.queryByTestId('narrowed-filter-pill')).not.toBeInTheDocument();
    expect(screen.getByText('Loan')).toBeInTheDocument();
  });
});
