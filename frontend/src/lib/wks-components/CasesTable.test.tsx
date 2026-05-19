import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { renderWithProviders } from '@/test/renderWithProviders';
import type { CaseRow } from '@/types/case';
import type { CaseTypeView } from '@/types/caseType';

import { CasesTable } from './CasesTable';

// Narrow the test: stub the case-type-view hook so we don't fetch anything.
// The hook contract is `{ data, isLoading, ... }[]`, one per requested id.
vi.mock('@/hooks/useCaseTypes', () => ({
  useCaseTypeViews: (ids: string[]) =>
    ids.map((id) => ({
      data: {
        id,
        displayName: 'Loan Application',
        version: 1,
        fields: [],
        statuses: [{ id: 'open', displayName: 'Open', color: 'zinc' as const }],
        listColumns: [],
      } satisfies CaseTypeView,
      isLoading: false,
      isError: false,
    })),
}));

function row(overrides: Partial<CaseRow> = {}): CaseRow {
  return {
    id: 'c1',
    caseTypeId: 'loan-application',
    status: 'open',
    assignee: null,
    createdAt: '2026-05-01T10:00:00Z',
    updatedAt: '2026-05-01T10:00:00Z',
    fields: {},
    hasUnreadActivity: false,
    slaBreached: false,
    ...overrides,
  };
}

const noop = () => {};

const defaultProps = {
  caseTypeIds: ['loan-application'],
  selectedIds: new Set<string>(),
  onToggleSelect: noop,
  onToggleSelectAll: noop,
  focusedRowId: null,
  onRowFocus: noop,
  editing: null,
  onStartEdit: noop,
  onCancelEdit: noop,
  onCommitEdit: noop,
};

describe('<CasesTable>', () => {
  it('renders the empty state when rows is empty', () => {
    renderWithProviders(<CasesTable rows={[]} {...defaultProps} />);
    expect(screen.getByText(/no cases match the current filters/i)).toBeInTheDocument();
  });

  it('renders one row per case', () => {
    const rows = [row({ id: 'aaaa1111' }), row({ id: 'bbbb2222' })];
    renderWithProviders(<CasesTable rows={rows} {...defaultProps} />);

    // ID column shows the first 8 chars of the case id.
    expect(screen.getByText('aaaa1111')).toBeInTheDocument();
    expect(screen.getByText('bbbb2222')).toBeInTheDocument();
  });

  it('fires onOpenCase when a row is clicked', async () => {
    const user = userEvent.setup();
    const onOpenCase = vi.fn();
    const rows = [row({ id: 'aaaa1111' })];

    renderWithProviders(<CasesTable rows={rows} onOpenCase={onOpenCase} {...defaultProps} />);

    await user.click(screen.getByText('aaaa1111'));

    expect(onOpenCase).toHaveBeenCalledWith('aaaa1111');
  });
});
