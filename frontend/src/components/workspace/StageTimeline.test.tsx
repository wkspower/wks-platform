import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import type { StageView } from '@/types/case';

import { StageTimeline } from './StageTimeline';

function wrap(ui: React.ReactNode) {
  return render(<TooltipProvider delayDuration={0}>{ui}</TooltipProvider>);
}

function pending(idx: number, name = `Stage ${idx}`): StageView {
  return {
    stageId: `s${idx}`,
    displayName: name,
    ordinal: idx,
    state: 'PENDING',
    enteredAt: null,
    exitedAt: null,
    source: null,
    sourceRef: null,
  };
}

describe('StageTimeline — render-state matrix (AC1, AC2, AC11.1)', () => {
  it('renders three nodes for COMPLETED → ACTIVE → PENDING', () => {
    const stages: StageView[] = [
      {
        stageId: 'intake',
        displayName: 'Intake',
        ordinal: 0,
        state: 'COMPLETED',
        enteredAt: '2026-04-01T09:00:00Z',
        exitedAt: '2026-04-02T13:00:00Z',
        source: 'manual',
        sourceRef: null,
      },
      {
        stageId: 'underwriting',
        displayName: 'Underwriting',
        ordinal: 1,
        state: 'ACTIVE',
        enteredAt: '2026-04-02T13:00:00Z',
        exitedAt: null,
        source: 'manual',
        sourceRef: null,
      },
      {
        stageId: 'decision',
        displayName: 'Decision',
        ordinal: 2,
        state: 'PENDING',
        enteredAt: null,
        exitedAt: null,
        source: null,
        sourceRef: null,
      },
    ];
    const { container } = wrap(<StageTimeline stages={stages} />);

    // Three list items, in declared ordinal order.
    const items = container.querySelectorAll('li[data-stage-state]');
    expect(items).toHaveLength(3);
    expect(items[0]?.getAttribute('data-stage-state')).toBe('completed');
    expect(items[1]?.getAttribute('data-stage-state')).toBe('active');
    expect(items[2]?.getAttribute('data-stage-state')).toBe('pending');

    // Active stage carries the WAI-ARIA aria-current="step" treatment (AC8).
    expect(items[1]?.getAttribute('aria-current')).toBe('step');
    // Non-active stages do NOT carry aria-current.
    expect(items[0]?.getAttribute('aria-current')).toBeNull();
    expect(items[2]?.getAttribute('aria-current')).toBeNull();

    // Display names are visible.
    expect(screen.getByText('Intake')).toBeInTheDocument();
    expect(screen.getByText('Underwriting')).toBeInTheDocument();
    expect(screen.getByText('Decision')).toBeInTheDocument();
  });

  it('marks the SKIPPED stage at its declared ordinal (AC3 — skipped never omitted)', () => {
    const stages: StageView[] = [
      {
        stageId: 'intake',
        displayName: 'Intake',
        ordinal: 0,
        state: 'COMPLETED',
        enteredAt: '2026-04-01T09:00:00Z',
        exitedAt: '2026-04-01T10:00:00Z',
        source: 'manual',
        sourceRef: null,
      },
      {
        stageId: 'underwriting',
        displayName: 'Underwriting',
        ordinal: 1,
        state: 'SKIPPED',
        enteredAt: null,
        exitedAt: '2026-04-01T10:00:00Z',
        source: 'manual',
        sourceRef: null,
      },
      {
        stageId: 'decision',
        displayName: 'Decision',
        ordinal: 2,
        state: 'ACTIVE',
        enteredAt: '2026-04-01T10:00:00Z',
        exitedAt: null,
        source: 'manual',
        sourceRef: null,
      },
    ];
    const { container } = wrap(<StageTimeline stages={stages} />);
    const items = container.querySelectorAll('li[data-stage-state]');
    expect(items[1]?.getAttribute('data-stage-state')).toBe('skipped');
    // Skipped stage label retains italic-opacity treatment via class hook.
    const skippedMeta = items[1]?.querySelector('.wks-stage-meta');
    expect(skippedMeta?.className).toMatch(/italic/);
  });

  it('returns null on empty stages array (AC2 — no DOM, no skeleton)', () => {
    const { container } = wrap(<StageTimeline stages={[]} />);
    expect(container.querySelector('nav')).toBeNull();
    expect(container.firstChild).toBeNull();
  });

  it('forces vertical-stepper layout when stages.length > 12 (Q1 cap)', () => {
    const thirteen = Array.from({ length: 13 }, (_, i) => pending(i, `S${i}`));
    const { container } = wrap(<StageTimeline stages={thirteen} />);
    const nav = container.querySelector('nav.wks-stage-timeline');
    expect(nav?.getAttribute('data-layout')).toBe('vertical');
  });

  it('emits WKS-UI-2001 console.warn for two ACTIVE stages (AC10 — defensive)', () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    const stages: StageView[] = [
      {
        stageId: 's0',
        displayName: 'A',
        ordinal: 0,
        state: 'ACTIVE',
        enteredAt: '2026-04-01T00:00:00Z',
        exitedAt: null,
        source: 'manual',
        sourceRef: null,
      },
      {
        stageId: 's1',
        displayName: 'B',
        ordinal: 1,
        state: 'ACTIVE',
        enteredAt: '2026-04-01T01:00:00Z',
        exitedAt: null,
        source: 'manual',
        sourceRef: null,
      },
    ];
    wrap(<StageTimeline stages={stages} />);
    expect(warn).toHaveBeenCalled();
    const msg = warn.mock.calls[0]?.[0] as string;
    expect(msg).toMatch(/WKS-UI-2001/);
    warn.mockRestore();
  });
});
