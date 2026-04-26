import { render as rtlRender, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { ReactElement } from 'react';
import { describe, expect, it, vi } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import { buildCaseColumns } from '@/lib/buildCaseColumns';
import {
  buildCaseListFixture,
  loanApplicationCaseTypeView,
} from '@/test/fixtures/buildCaseListFixture';
import { toCaseRow, type CaseRow } from '@/types/case';

import { CaseDataTable } from './CaseDataTable';

function render(ui: ReactElement) {
  return rtlRender(<TooltipProvider delayDuration={0}>{ui}</TooltipProvider>);
}

describe('CaseDataTable', () => {
  const caseType = loanApplicationCaseTypeView();
  const columns = buildCaseColumns(caseType);

  function makeRows(n: number): CaseRow[] {
    return buildCaseListFixture(n).map(toCaseRow);
  }

  it('renders a semantic <table> with <th scope="col"> headers', () => {
    render(
      <CaseDataTable columns={columns} data={makeRows(2)} emptyState="no-data" ariaLabel="Cases" />,
    );
    const table = screen.getByRole('table', { name: 'Cases' });
    expect(table.tagName).toBe('TABLE');
    const headers = within(table).getAllByRole('columnheader');
    expect(headers.length).toBeGreaterThan(0);
    for (const h of headers) {
      expect(h.getAttribute('scope')).toBe('col');
    }
  });

  it('emits 5 skeleton rows when isLoading', () => {
    render(
      <CaseDataTable
        columns={columns}
        data={[]}
        isLoading
        emptyState="no-data"
        ariaLabel="Cases"
      />,
    );
    // The header row + 5 skeleton rows; pulse cells are aria-hidden so role=row counts the rows.
    const rows = screen.getAllByRole('row');
    expect(rows.length).toBe(1 + 5);
  });

  it('cycles aria-sort on header click', async () => {
    const user = userEvent.setup();
    render(
      <CaseDataTable columns={columns} data={makeRows(3)} emptyState="no-data" ariaLabel="Cases" />,
    );
    const idHeader = screen.getByRole('columnheader', { name: /ID/i });
    expect(idHeader.getAttribute('aria-sort')).toBe('none');
    await user.click(idHeader);
    expect(idHeader.getAttribute('aria-sort')).toBe('ascending');
    await user.click(idHeader);
    expect(idHeader.getAttribute('aria-sort')).toBe('descending');
    await user.click(idHeader);
    expect(idHeader.getAttribute('aria-sort')).toBe('none');
  });

  it('fires onRowSelect on click and on Enter', async () => {
    const user = userEvent.setup();
    const onRowSelect = vi.fn();
    const rows = makeRows(2);
    render(
      <CaseDataTable
        columns={columns}
        data={rows}
        emptyState="no-data"
        ariaLabel="Cases"
        onRowSelect={onRowSelect}
      />,
    );
    const tbodyRows = screen.getAllByRole('row').slice(1); // drop header
    await user.click(tbodyRows[0]!);
    expect(onRowSelect).toHaveBeenCalledTimes(1);

    tbodyRows[1]!.focus();
    await user.keyboard('{Enter}');
    expect(onRowSelect).toHaveBeenCalledTimes(2);
  });

  it('marks has-new rows with the primary border + sr-only label', () => {
    const rows = makeRows(1);
    rows[0] = { ...rows[0]!, hasUnreadActivity: true };
    render(<CaseDataTable columns={columns} data={rows} emptyState="no-data" ariaLabel="Cases" />);
    expect(screen.getByText('new activity')).toHaveClass('sr-only');
  });
});
