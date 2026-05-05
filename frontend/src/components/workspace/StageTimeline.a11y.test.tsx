import { render } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import type { StageView } from '@/types/case';

import { StageTimeline } from './StageTimeline';

function wrap(ui: React.ReactNode) {
  return render(<TooltipProvider delayDuration={0}>{ui}</TooltipProvider>);
}

describe('StageTimeline — accessibility (AC8, AC11.4)', () => {
  it('renders <nav aria-label> > <ol> with aria-current="step" on the active stage', () => {
    const stages: StageView[] = [
      {
        stageId: 's0',
        displayName: 'Intake',
        ordinal: 0,
        state: 'COMPLETED',
        enteredAt: '2026-04-01T00:00:00Z',
        exitedAt: '2026-04-02T00:00:00Z',
        source: 'manual',
        sourceRef: null,
      },
      {
        stageId: 's1',
        displayName: 'Decision',
        ordinal: 1,
        state: 'ACTIVE',
        enteredAt: '2026-04-02T00:00:00Z',
        exitedAt: null,
        source: 'manual',
        sourceRef: null,
      },
    ];
    const { container } = wrap(<StageTimeline stages={stages} />);

    const nav = container.querySelector('nav.wks-stage-timeline');
    expect(nav).not.toBeNull();
    expect(nav?.getAttribute('aria-label')).toBe('Stage timeline');

    const list = nav?.querySelector('ol');
    expect(list).not.toBeNull();

    const items = list?.querySelectorAll('li[data-stage-state]');
    expect(items?.length).toBe(2);
    // Only the ACTIVE stage carries aria-current="step".
    expect(items?.[0]?.getAttribute('aria-current')).toBeNull();
    expect(items?.[1]?.getAttribute('aria-current')).toBe('step');
  });

  it('renders an aria-live="polite" region announcing the full stage list on mount', () => {
    const stages: StageView[] = [
      {
        stageId: 's0',
        displayName: 'Intake',
        ordinal: 0,
        state: 'COMPLETED',
        enteredAt: '2026-04-01T00:00:00Z',
        exitedAt: '2026-04-02T00:00:00Z',
        source: 'manual',
        sourceRef: null,
      },
      {
        stageId: 's1',
        displayName: 'Decision',
        ordinal: 1,
        state: 'ACTIVE',
        enteredAt: '2026-04-02T00:00:00Z',
        exitedAt: null,
        source: 'manual',
        sourceRef: null,
      },
    ];
    const { container } = wrap(<StageTimeline stages={stages} />);
    const live = container.querySelector('[aria-live="polite"]');
    expect(live).not.toBeNull();
    // The announcement names the count and surfaces each stage's display name + state label.
    expect(live?.textContent).toContain('2 stages');
    expect(live?.textContent).toContain('Intake');
    expect(live?.textContent).toContain('Completed');
    expect(live?.textContent).toContain('Decision');
    expect(live?.textContent).toContain('Current');
  });
});
