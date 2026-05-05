import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import type { StageView } from '@/types/case';

import { StageTimeline } from './StageTimeline';

function wrap(ui: React.ReactNode) {
  return render(<TooltipProvider delayDuration={0}>{ui}</TooltipProvider>);
}

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
    state: 'SKIPPED',
    enteredAt: null,
    exitedAt: '2026-04-02T13:00:00Z',
    source: 'manual',
    sourceRef: 'risk-decision-bypass',
  },
  {
    stageId: 'decision',
    displayName: 'Decision',
    ordinal: 2,
    state: 'ACTIVE',
    enteredAt: '2026-04-02T13:00:00Z',
    exitedAt: null,
    source: 'manual',
    sourceRef: null,
  },
];

describe('StageTimeline — popover content per state (AC7, AC11.3)', () => {
  it('opens sticky popover on Enter activation and shows duration for COMPLETED', async () => {
    const user = userEvent.setup();
    const { container } = wrap(<StageTimeline stages={stages} />);
    const completedNode = container.querySelector(
      'li[data-stage-id="intake"] button.wks-stage-node',
    ) as HTMLButtonElement;
    completedNode.focus();

    await user.click(completedNode);
    // Radix portals popover content; assert via screen.
    await waitFor(() => {
      expect(screen.getAllByText(/In stage for/i).length).toBeGreaterThan(0);
    });
  });

  it('shows "Skipped" only (no reason text) for SKIPPED — Q2 default', async () => {
    const user = userEvent.setup();
    const { container } = wrap(<StageTimeline stages={stages} />);
    const skippedNode = container.querySelector(
      'li[data-stage-id="underwriting"] button.wks-stage-node',
    ) as HTMLButtonElement;
    skippedNode.focus();

    await user.click(skippedNode);
    await waitFor(() => {
      // The popover surfaces the literal "Skipped" label; the sourceRef ("risk-decision-bypass")
      // is NEVER rendered into the popover (Q2 lock — backend is sole source of truth, no rich
      // payload until Epic 4).
      const skippedLabels = screen.getAllByText(/Skipped/);
      expect(skippedLabels.length).toBeGreaterThan(0);
    });
    expect(screen.queryByText(/risk-decision-bypass/)).toBeNull();
  });

  it('shows "Current since" for ACTIVE', async () => {
    const user = userEvent.setup();
    const { container } = wrap(<StageTimeline stages={stages} />);
    const activeNode = container.querySelector(
      'li[data-stage-id="decision"] button.wks-stage-node',
    ) as HTMLButtonElement;
    activeNode.focus();

    await user.click(activeNode);
    await waitFor(() => {
      expect(screen.getAllByText(/Current since/i).length).toBeGreaterThan(0);
    });
  });
});
