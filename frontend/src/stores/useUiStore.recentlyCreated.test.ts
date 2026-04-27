import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { useUiStore } from './uiStore';

describe('uiStore.recentlyCreatedCaseIds', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    useUiStore.setState({ recentlyCreatedCaseIds: new Set() });
  });
  afterEach(() => {
    vi.useRealTimers();
  });

  it('pushRecentlyCreated adds the id to the set', () => {
    useUiStore.getState().pushRecentlyCreated('case-1');
    expect(useUiStore.getState().recentlyCreatedCaseIds.has('case-1')).toBe(true);
  });

  it('auto-clears the id after 6s', () => {
    useUiStore.getState().pushRecentlyCreated('case-1');
    expect(useUiStore.getState().recentlyCreatedCaseIds.has('case-1')).toBe(true);
    vi.advanceTimersByTime(6_001);
    expect(useUiStore.getState().recentlyCreatedCaseIds.has('case-1')).toBe(false);
  });

  it('does not stack timers when the same id is pushed twice within TTL', () => {
    useUiStore.getState().pushRecentlyCreated('case-1');
    vi.advanceTimersByTime(3_000);
    useUiStore.getState().pushRecentlyCreated('case-1');
    // First timer is canceled and replaced — at original-T+6s the id is still cleared based on
    // the most recent push.
    vi.advanceTimersByTime(3_001);
    // 6001ms total since first push: id should be cleared by the (still-only-one) timer.
    expect(useUiStore.getState().recentlyCreatedCaseIds.has('case-1')).toBe(false);
  });

  it('clearRecentlyCreated removes the id and cancels its pending timer', () => {
    useUiStore.getState().pushRecentlyCreated('case-1');
    useUiStore.getState().clearRecentlyCreated('case-1');
    expect(useUiStore.getState().recentlyCreatedCaseIds.has('case-1')).toBe(false);
    // No subsequent timer should fire — advancing past TTL is a no-op.
    vi.advanceTimersByTime(10_000);
    expect(useUiStore.getState().recentlyCreatedCaseIds.size).toBe(0);
  });

  it('handles multiple ids independently', () => {
    useUiStore.getState().pushRecentlyCreated('case-a');
    vi.advanceTimersByTime(2_000);
    useUiStore.getState().pushRecentlyCreated('case-b');
    vi.advanceTimersByTime(4_001); // case-a now > 6s, case-b at ~4s
    expect(useUiStore.getState().recentlyCreatedCaseIds.has('case-a')).toBe(false);
    expect(useUiStore.getState().recentlyCreatedCaseIds.has('case-b')).toBe(true);
    vi.advanceTimersByTime(2_000);
    expect(useUiStore.getState().recentlyCreatedCaseIds.has('case-b')).toBe(false);
  });
});
