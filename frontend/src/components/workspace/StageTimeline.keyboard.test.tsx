import { render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import type { StageView } from '@/types/case';

import { StageTimeline } from './StageTimeline';

function makeStages(count: number): StageView[] {
  return Array.from({ length: count }, (_, i) => ({
    stageId: `s${i}`,
    displayName: `Stage ${i}`,
    ordinal: i,
    state: i === 0 ? 'COMPLETED' : i === 1 ? 'ACTIVE' : 'PENDING',
    enteredAt: i <= 1 ? '2026-04-01T00:00:00Z' : null,
    exitedAt: i === 0 ? '2026-04-02T00:00:00Z' : null,
    source: i <= 1 ? 'manual' : null,
    sourceRef: null,
  }));
}

function wrap(ui: React.ReactNode) {
  return render(<TooltipProvider delayDuration={0}>{ui}</TooltipProvider>);
}

describe('StageTimeline — keyboard navigation (AC7, AC11.2)', () => {
  it('initial focus targets the ACTIVE stage; ArrowRight steps forward; ArrowLeft back', async () => {
    const user = userEvent.setup();
    const { container } = wrap(<StageTimeline stages={makeStages(4)} />);
    const nodes = container.querySelectorAll('button.wks-stage-node');
    expect(nodes).toHaveLength(4);

    // Roving tabindex: only the active stage (index 1) starts with tabIndex=0.
    expect(nodes[0]?.getAttribute('tabindex')).toBe('-1');
    expect(nodes[1]?.getAttribute('tabindex')).toBe('0');
    expect(nodes[2]?.getAttribute('tabindex')).toBe('-1');

    // Move focus to the active node first (Tab simulation via direct focus is fine — we test
    // the component's roving behavior, not the browser's tab order).
    (nodes[1] as HTMLButtonElement).focus();
    expect(document.activeElement).toBe(nodes[1]);

    await user.keyboard('{ArrowRight}');
    expect(document.activeElement).toBe(nodes[2]);

    await user.keyboard('{ArrowRight}');
    expect(document.activeElement).toBe(nodes[3]);

    // No-wrap: another ArrowRight stays on last node.
    await user.keyboard('{ArrowRight}');
    expect(document.activeElement).toBe(nodes[3]);

    await user.keyboard('{ArrowLeft}');
    expect(document.activeElement).toBe(nodes[2]);
  });

  it('Home jumps to first node, End jumps to last (AC7)', async () => {
    const user = userEvent.setup();
    const { container } = wrap(<StageTimeline stages={makeStages(5)} />);
    const nodes = container.querySelectorAll('button.wks-stage-node');
    (nodes[1] as HTMLButtonElement).focus();

    await user.keyboard('{End}');
    expect(document.activeElement).toBe(nodes[4]);

    await user.keyboard('{Home}');
    expect(document.activeElement).toBe(nodes[0]);
  });

  it('ArrowDown / ArrowUp move focus identically to ArrowRight / ArrowLeft', async () => {
    const user = userEvent.setup();
    const { container } = wrap(<StageTimeline stages={makeStages(3)} />);
    const nodes = container.querySelectorAll('button.wks-stage-node');
    (nodes[1] as HTMLButtonElement).focus();

    await user.keyboard('{ArrowDown}');
    expect(document.activeElement).toBe(nodes[2]);

    await user.keyboard('{ArrowUp}');
    expect(document.activeElement).toBe(nodes[1]);
  });
});
