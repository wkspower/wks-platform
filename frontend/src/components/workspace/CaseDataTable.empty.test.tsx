import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { buildCaseColumns } from '@/lib/buildCaseColumns';
import { loanApplicationCaseTypeView } from '@/test/fixtures/buildCaseListFixture';

import { CaseDataTable } from './CaseDataTable';

describe('CaseDataTable empty states', () => {
  const columns = buildCaseColumns(loanApplicationCaseTypeView());

  it("renders 'no-data' copy when data is empty and not filtered", () => {
    render(<CaseDataTable columns={columns} data={[]} emptyState="no-data" ariaLabel="Cases" />);
    expect(screen.getByText(/No cases yet/)).toBeInTheDocument();
  });

  it("renders 'filtered' copy and clear button when emptyState='filtered'", async () => {
    const user = userEvent.setup();
    const onClearFilters = vi.fn();
    render(
      <CaseDataTable
        columns={columns}
        data={[]}
        emptyState="filtered"
        onClearFilters={onClearFilters}
        ariaLabel="Cases"
      />,
    );
    expect(screen.getByText(/No cases match current filters/)).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /Clear filters/ }));
    expect(onClearFilters).toHaveBeenCalledTimes(1);
  });
});
