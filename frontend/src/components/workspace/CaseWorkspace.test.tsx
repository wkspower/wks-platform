import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { act, render, screen } from '@testing-library/react';
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
  toCaseRow({
    id: 'b',
    caseTypeId: 'loan',
    status: 'open',
    assignee: null,
    createdAt: '2026-04-01T00:00:00Z',
    updatedAt: '2026-04-01T00:00:00Z',
    fields: {},
  }),
];

function StubList({ variant, hiddenColumnIds }: { variant?: string; hiddenColumnIds?: string[] }) {
  return (
    <div data-testid="list" data-variant={variant} data-hidden={hiddenColumnIds?.join(',') ?? ''} />
  );
}

function StubFilterBar({ variant }: { variant?: string }) {
  return <div data-testid="filter-bar" data-variant={variant} />;
}

describe('CaseWorkspace CSS state', () => {
  it('renders list-only when selectedCaseId is null', () => {
    setViewport(1400);
    useUiStore.setState({ sidebarCollapsed: false });
    const { container } = wrap(
      <CaseWorkspace
        filterBar={<StubFilterBar />}
        list={<StubList />}
        selectedCaseId={null}
        onSelectionChange={() => undefined}
        sortedRows={rows}
      />,
    );
    expect(container.querySelector('.workspace-list-only')).toBeTruthy();
    expect(container.querySelector('.workspace-list-and-detail')).toBeFalsy();
    expect(screen.getByTestId('filter-bar').dataset.variant).toBe('full-width');
    expect(screen.getByTestId('list').dataset.variant).toBe('full-width');
  });

  it('renders list-and-detail when selectedCaseId is set and viewport allows', () => {
    setViewport(1400);
    useUiStore.setState({ sidebarCollapsed: false });
    const { container } = wrap(
      <CaseWorkspace
        filterBar={<StubFilterBar />}
        list={<StubList />}
        selectedCaseId="missing-id-so-detail-loads"
        onSelectionChange={() => undefined}
        sortedRows={rows}
      />,
    );
    expect(container.querySelector('.workspace-list-and-detail')).toBeTruthy();
    expect(screen.getByTestId('filter-bar').dataset.variant).toBe('narrowed');
    expect(screen.getByTestId('list').dataset.variant).toBe('narrowed');
    expect(screen.getByTestId('list').dataset.hidden).toBe('caseType');
  });

  it('passes onRowSelect through to the list', async () => {
    setViewport(1400);
    useUiStore.setState({ sidebarCollapsed: false });
    let capturedHandler: ((row: { id: string }) => void) | undefined;
    function CapturingList({ onRowSelect }: { onRowSelect?: (row: { id: string }) => void }) {
      capturedHandler = onRowSelect;
      return <div data-testid="list" />;
    }
    const calls: (string | null)[] = [];
    wrap(
      <CaseWorkspace
        filterBar={<StubFilterBar />}
        list={<CapturingList />}
        selectedCaseId={null}
        onSelectionChange={(id) => calls.push(id)}
        sortedRows={rows}
      />,
    );
    await act(async () => {
      capturedHandler?.({ id: 'a' });
    });
    expect(calls).toEqual(['a']);
  });
});
