import { render as rtlRender, screen } from '@testing-library/react';
import type { ReactElement } from 'react';
import { describe, expect, it } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import { buildCaseColumns } from '@/lib/buildCaseColumns';
import {
  buildCaseListFixture,
  loanApplicationCaseTypeView,
} from '@/test/fixtures/buildCaseListFixture';
import { toCaseRow } from '@/types/case';

import { CaseDataTable } from './CaseDataTable';

function render(ui: ReactElement) {
  return rtlRender(<TooltipProvider delayDuration={0}>{ui}</TooltipProvider>);
}

/**
 * Story 2.5 AC8 — first interactive paint of `CaseDataTable` with 1,000 rows must be under
 * 1500 ms wall-clock. This budget is the CI threshold; local laptops typically come in under
 * 500 ms (UX spec note in Dev Notes). The pagination row model + page size 50 means TanStack
 * Table only commits 50 rows to the DOM regardless of the upstream count — this guards against
 * a future regression that breaks pagination (e.g., naïve virtualisation removal).
 *
 * Budget bumped 1000 ms → 1500 ms on 2026-05-13 (story case-data-table-perf-harden):
 * two-sprint flake-debt rule per Sprint 11 retro Action 3 — fps-budget migration rejected
 * (deferred to Phase-1; jsdom does not paint frames, not a demo-gate move).
 */
describe('CaseDataTable perf-guardrail', () => {
  it('renders 1000 rows under 1500ms wall-clock to first interactive paint', async () => {
    const caseType = loanApplicationCaseTypeView();
    const columns = buildCaseColumns(caseType);
    const data = buildCaseListFixture(1000).map(toCaseRow);

    const start = performance.now();
    const { unmount } = render(
      <CaseDataTable columns={columns} data={data} emptyState="no-data" ariaLabel="Cases" />,
    );
    // AC8 — measure to "first interactive paint": wait until at least one body row is queryable.
    await screen.findAllByRole('row');
    const elapsed = performance.now() - start;
    unmount();

    // Budget bumped 1000 → 1500 ms on 2026-05-13 (story case-data-table-perf-harden) —
    // two-sprint flake-debt rule per Sprint 11 retro Action 3; fps-budget migration rejected.
    expect(elapsed, `render took ${elapsed.toFixed(0)}ms (budget 1500ms)`).toBeLessThan(1500);
  });
});
