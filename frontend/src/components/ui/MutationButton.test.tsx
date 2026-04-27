import { act, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { MutationButton } from './MutationButton';

describe('MutationButton', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });
  afterEach(() => {
    vi.useRealTimers();
  });

  it('idle state shows the children, button enabled', () => {
    render(<MutationButton state="idle">Save</MutationButton>);
    const btn = screen.getByRole('button');
    expect(btn).not.toBeDisabled();
    expect(btn).toHaveTextContent('Save');
    expect(btn).toHaveAttribute('data-state', 'idle');
  });

  it('confirming state disables the button and exposes aria-busy', () => {
    render(
      <MutationButton state="confirming" confirmingLabel="Saving…">
        Save
      </MutationButton>,
    );
    const btn = screen.getByRole('button');
    expect(btn).toBeDisabled();
    expect(btn).toHaveAttribute('aria-busy', 'true');
    expect(btn).toHaveTextContent('Saving…');
  });

  it('confirmed state holds 2s then fades to idle (re-enables button + restores children)', () => {
    const { rerender } = render(
      <MutationButton state="idle" confirmedLabel="Saved">
        Save
      </MutationButton>,
    );
    rerender(
      <MutationButton state="confirmed" confirmedLabel="Saved">
        Save
      </MutationButton>,
    );
    const btn = screen.getByRole('button');
    expect(btn).toBeDisabled();
    expect(btn).toHaveTextContent('Saved');
    act(() => {
      vi.advanceTimersByTime(2_001);
    });
    expect(btn).not.toBeDisabled();
    expect(btn).toHaveTextContent('Save');
    expect(btn).toHaveAttribute('data-state', 'idle');
  });

  it('failed state renders retryAction next to the button', () => {
    render(
      <MutationButton
        state="failed"
        failedLabel="Failed — boom"
        retryAction={<button type="button">Retry</button>}
      >
        Save
      </MutationButton>,
    );
    // The button label appears twice (visible text + sr-only live region announcement) — both
    // good. Just confirm the button has the failed copy and the retry button is rendered.
    const buttons = screen.getAllByRole('button');
    expect(buttons[0]).toHaveTextContent('Failed — boom');
    expect(screen.getByRole('button', { name: 'Retry' })).toBeInTheDocument();
  });

  it('always-polite live region (never assertive)', () => {
    render(
      <MutationButton state="failed" failedLabel="Failed">
        Save
      </MutationButton>,
    );
    // The sr-only span carries aria-live="polite" regardless of state to keep AT region tracking.
    const polite = document.querySelector('[aria-live="polite"]');
    expect(polite).toBeInTheDocument();
    expect(document.querySelector('[aria-live="assertive"]')).toBeNull();
  });
});
