import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { act, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { ReactNode } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import { useUiStore } from '@/stores/uiStore';
import { toCaseRow } from '@/types/case';

import { CaseWorkspace } from './CaseWorkspace';

function setViewport(width: number) {
  Object.defineProperty(window, 'innerWidth', { value: width, configurable: true });
  Object.defineProperty(window, 'innerHeight', { value: 800, configurable: true });
  window.dispatchEvent(new Event('resize'));
}

function wrap(ui: ReactNode) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <MemoryRouter>
      <QueryClientProvider client={client}>
        <TooltipProvider delayDuration={0}>{ui}</TooltipProvider>
      </QueryClientProvider>
    </MemoryRouter>,
  );
}

const rows = [
  toCaseRow({
    id: 'a',
    caseTypeId: 'loan',
    status: 'open',
    assignee: null,
    createdAt: '2026-04-01T00:00:00Z',
    updatedAt: '2026-04-01T00:00:00Z',
    fields: {},
  }),
];

describe('CaseWorkspace responsive', () => {
  it('shows the auto-close banner when viewport < 1140 with sidebar expanded', async () => {
    setViewport(1100);
    useUiStore.setState({ sidebarCollapsed: false });
    wrap(
      <CaseWorkspace
        filterBar={<div />}
        list={<div data-testid="list" />}
        selectedCaseId="a"
        onSelectionChange={() => undefined}
        sortedRows={rows}
      />,
    );
    expect(
      await screen.findByText('Detail panel hidden — viewport too narrow'),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Re-open' })).toBeInTheDocument();
  });

  it('Re-open collapses the sidebar and shows detail again', async () => {
    setViewport(1000);
    useUiStore.setState({ sidebarCollapsed: false });
    const user = userEvent.setup();
    wrap(
      <CaseWorkspace
        filterBar={<div />}
        list={<div data-testid="list" />}
        selectedCaseId="a"
        onSelectionChange={() => undefined}
        sortedRows={rows}
      />,
    );
    await user.click(screen.getByRole('button', { name: 'Re-open' }));
    expect(useUiStore.getState().sidebarCollapsed).toBe(true);
  });

  it('hides the banner when viewport >= 1140', async () => {
    setViewport(1400);
    useUiStore.setState({ sidebarCollapsed: false });
    wrap(
      <CaseWorkspace
        filterBar={<div />}
        list={<div data-testid="list" />}
        selectedCaseId="a"
        onSelectionChange={() => undefined}
        sortedRows={rows}
      />,
    );
    expect(screen.queryByText('Detail panel hidden — viewport too narrow')).not.toBeInTheDocument();
  });

  it('uses the lower 884 threshold when sidebar is collapsed', async () => {
    setViewport(900);
    useUiStore.setState({ sidebarCollapsed: true });
    wrap(
      <CaseWorkspace
        filterBar={<div />}
        list={<div data-testid="list" />}
        selectedCaseId="a"
        onSelectionChange={() => undefined}
        sortedRows={rows}
      />,
    );
    expect(screen.queryByText('Detail panel hidden — viewport too narrow')).not.toBeInTheDocument();
  });

  it('transitions from list-and-detail to list-only when shrinking past threshold', async () => {
    setViewport(1400);
    useUiStore.setState({ sidebarCollapsed: false });
    const { container } = wrap(
      <CaseWorkspace
        filterBar={<div />}
        list={<div data-testid="list" />}
        selectedCaseId="a"
        onSelectionChange={() => undefined}
        sortedRows={rows}
      />,
    );
    expect(container.querySelector('.workspace-list-and-detail')).toBeTruthy();

    await act(async () => {
      setViewport(1000);
      await new Promise((r) => requestAnimationFrame(() => r(undefined)));
    });
    expect(container.querySelector('.workspace-list-only')).toBeTruthy();
  });
});
