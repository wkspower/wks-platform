import { act, render as rtlRender, screen } from '@testing-library/react';
import type { ReactElement } from 'react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import { buildCaseColumns } from '@/lib/buildCaseColumns';
import { useUiStore } from '@/stores/uiStore';
import {
  buildCaseListFixture,
  loanApplicationCaseTypeView,
} from '@/test/fixtures/buildCaseListFixture';
import { toCaseRow } from '@/types/case';

import { CaseDataTable } from './CaseDataTable';

function render(ui: ReactElement) {
  return rtlRender(<TooltipProvider delayDuration={0}>{ui}</TooltipProvider>);
}

describe('CaseDataTable — recentlyCreatedCaseIds wiring (P5 / AC8)', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    useUiStore.setState({ recentlyCreatedCaseIds: new Set() });
  });
  afterEach(() => {
    vi.useRealTimers();
  });

  const caseType = loanApplicationCaseTypeView();
  const columns = buildCaseColumns(caseType);
  const rows = buildCaseListFixture(3).map(toCaseRow);

  it('applies the border-l-3 highlight to rows whose id is in recentlyCreatedCaseIds', () => {
    const targetId = rows[1]!.id;
    render(<CaseDataTable columns={columns} data={rows} emptyState="no-data" ariaLabel="Cases" />);
    act(() => {
      useUiStore.getState().pushRecentlyCreated(targetId);
    });
    const tr = document.querySelector(`tr[data-row-id="${targetId}"]`);
    expect(tr).toBeTruthy();
    expect(tr?.className).toMatch(/border-l-\[3px\]/);
  });

  it('announces the freshly-created case once via aria-live=polite', () => {
    const targetId = rows[0]!.id;
    render(<CaseDataTable columns={columns} data={rows} emptyState="no-data" ariaLabel="Cases" />);
    expect(screen.getByRole('status')).toHaveTextContent('');
    act(() => {
      useUiStore.getState().pushRecentlyCreated(targetId);
    });
    expect(screen.getByRole('status')).toHaveTextContent(/created/i);
  });

  it('drops the highlight after the 6s TTL expires', () => {
    const targetId = rows[2]!.id;
    render(<CaseDataTable columns={columns} data={rows} emptyState="no-data" ariaLabel="Cases" />);
    act(() => {
      useUiStore.getState().pushRecentlyCreated(targetId);
    });
    let tr = document.querySelector(`tr[data-row-id="${targetId}"]`);
    expect(tr?.className).toMatch(/border-l-\[3px\]/);
    act(() => {
      vi.advanceTimersByTime(6_001);
    });
    tr = document.querySelector(`tr[data-row-id="${targetId}"]`);
    expect(tr?.className).not.toMatch(/border-l-\[3px\] border-\[var\(--primary\)\]/);
  });
});
