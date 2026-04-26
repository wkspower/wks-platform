import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, beforeEach, vi } from 'vitest';

import { useUiStore } from '@/stores/uiStore';
import { loanApplicationCaseTypeView } from '@/test/fixtures/buildCaseListFixture';
import type { CaseTypeSummary, CaseTypeView } from '@/types/caseType';

import { CaseFilterBar } from './CaseFilterBar';

const SUMMARY: CaseTypeSummary[] = [
  {
    id: 'loan-application',
    displayName: 'Loan Application',
    version: 1,
    statusCount: 4,
    fieldCount: 2,
  },
  { id: 'hr-onboarding', displayName: 'HR Onboarding', version: 1, statusCount: 2, fieldCount: 1 },
];

const VIEW: CaseTypeView[] = [loanApplicationCaseTypeView()];

describe('CaseFilterBar', () => {
  beforeEach(() => {
    useUiStore.setState({
      caseListFilters: { caseTypeIds: [], statusIds: [], priorities: [] },
    });
  });

  it('toggles a case-type selection through the dropdown and persists to uiStore', async () => {
    const user = userEvent.setup();
    render(
      <CaseFilterBar
        caseTypes={SUMMARY}
        selectedCaseTypeViews={VIEW}
        searchInput=""
        onSearchInputChange={() => undefined}
      />,
    );
    await user.click(screen.getByRole('button', { name: /Case type/ }));
    await user.click(await screen.findByRole('menuitemcheckbox', { name: 'Loan Application' }));
    expect(useUiStore.getState().caseListFilters.caseTypeIds).toContain('loan-application');
  });

  it('renders chips for each active filter and removes them via the dismiss button', async () => {
    useUiStore.setState({
      caseListFilters: { caseTypeIds: ['loan-application'], statusIds: [], priorities: [] },
    });
    const user = userEvent.setup();
    render(
      <CaseFilterBar
        caseTypes={SUMMARY}
        selectedCaseTypeViews={VIEW}
        searchInput=""
        onSearchInputChange={() => undefined}
      />,
    );
    const chipBtn = screen.getByRole('button', {
      name: /Remove filter Case type: Loan Application/,
    });
    await user.click(chipBtn);
    expect(useUiStore.getState().caseListFilters.caseTypeIds).toEqual([]);
  });

  it('clears all filters via the ghost button', async () => {
    useUiStore.setState({
      caseListFilters: {
        caseTypeIds: ['loan-application'],
        statusIds: ['open'],
        priorities: ['high'],
      },
    });
    const user = userEvent.setup();
    render(
      <CaseFilterBar
        caseTypes={SUMMARY}
        selectedCaseTypeViews={VIEW}
        searchInput=""
        onSearchInputChange={() => undefined}
      />,
    );
    await user.click(screen.getByRole('button', { name: /Clear all filters/ }));
    expect(useUiStore.getState().caseListFilters).toEqual({
      caseTypeIds: [],
      statusIds: [],
      priorities: [],
    });
  });

  it("focuses the search input when '/' is pressed outside an editable", async () => {
    const user = userEvent.setup();
    render(
      <CaseFilterBar
        caseTypes={SUMMARY}
        selectedCaseTypeViews={VIEW}
        searchInput=""
        onSearchInputChange={vi.fn()}
      />,
    );
    const search = screen.getByRole('searchbox', { name: /Search cases/ });
    expect(search).not.toHaveFocus();
    await user.keyboard('/');
    expect(search).toHaveFocus();
  });
});
