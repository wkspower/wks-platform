import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { act, fireEvent, render } from '@testing-library/react';
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

const rows = ['a', 'b', 'c'].map((id) =>
  toCaseRow({
    id,
    caseTypeId: 'loan',
    status: 'open',
    assignee: null,
    createdAt: '2026-04-01T00:00:00Z',
    updatedAt: '2026-04-01T00:00:00Z',
    fields: {},
  }),
);

describe('CaseWorkspace keyboard', () => {
  it('Escape closes detail when selection is set', async () => {
    setViewport(1400);
    useUiStore.setState({ sidebarCollapsed: false });
    const calls: (string | null)[] = [];
    wrap(
      <CaseWorkspace
        filterBar={<div />}
        list={<div />}
        selectedCaseId="b"
        onSelectionChange={(id) => calls.push(id)}
        sortedRows={rows}
      />,
    );
    await act(async () => {
      fireEvent.keyDown(document, { key: 'Escape' });
    });
    expect(calls).toEqual([null]);
  });

  it('J navigates to next row, K to previous', async () => {
    setViewport(1400);
    useUiStore.setState({ sidebarCollapsed: false });
    const calls: (string | null)[] = [];
    wrap(
      <CaseWorkspace
        filterBar={<div />}
        list={<div />}
        selectedCaseId="b"
        onSelectionChange={(id) => calls.push(id)}
        sortedRows={rows}
      />,
    );
    await act(async () => {
      fireEvent.keyDown(document, { key: 'j' });
    });
    expect(calls).toEqual(['c']);
    await act(async () => {
      fireEvent.keyDown(document, { key: 'k' });
    });
    expect(calls).toEqual(['c', 'a']);
  });

  it('J on last row is a no-op; K on first is a no-op', async () => {
    setViewport(1400);
    useUiStore.setState({ sidebarCollapsed: false });
    const calls: (string | null)[] = [];
    const { rerender } = wrap(
      <CaseWorkspace
        filterBar={<div />}
        list={<div />}
        selectedCaseId="c"
        onSelectionChange={(id) => calls.push(id)}
        sortedRows={rows}
      />,
    );
    await act(async () => {
      fireEvent.keyDown(document, { key: 'j' });
    });
    expect(calls).toEqual([]);

    rerender(
      <MemoryRouter>
        <QueryClientProvider
          client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}
        >
          <TooltipProvider delayDuration={0}>
            <CaseWorkspace
              filterBar={<div />}
              list={<div />}
              selectedCaseId="a"
              onSelectionChange={(id) => calls.push(id)}
              sortedRows={rows}
            />
          </TooltipProvider>
        </QueryClientProvider>
      </MemoryRouter>,
    );
    await act(async () => {
      fireEvent.keyDown(document, { key: 'k' });
    });
    expect(calls).toEqual([]);
  });

  it('skips J/K when target is an input', async () => {
    setViewport(1400);
    useUiStore.setState({ sidebarCollapsed: false });
    const calls: (string | null)[] = [];
    wrap(
      <CaseWorkspace
        filterBar={<input type="text" data-testid="search" />}
        list={<div />}
        selectedCaseId="b"
        onSelectionChange={(id) => calls.push(id)}
        sortedRows={rows}
      />,
    );
    const input = document.querySelector('input') as HTMLInputElement;
    input.focus();
    await act(async () => {
      fireEvent.keyDown(input, { key: 'j' });
    });
    expect(calls).toEqual([]);
  });

  it('skips J/K during IME composition', async () => {
    setViewport(1400);
    useUiStore.setState({ sidebarCollapsed: false });
    const calls: (string | null)[] = [];
    wrap(
      <CaseWorkspace
        filterBar={<div />}
        list={<div />}
        selectedCaseId="b"
        onSelectionChange={(id) => calls.push(id)}
        sortedRows={rows}
      />,
    );
    await act(async () => {
      fireEvent.keyDown(document, { key: 'j', isComposing: true });
    });
    expect(calls).toEqual([]);
  });
});
