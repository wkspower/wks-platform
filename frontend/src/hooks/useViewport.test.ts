import { act, renderHook } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';

import { useViewport } from './useViewport';

describe('useViewport', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('reflects window.innerWidth on first render', () => {
    Object.defineProperty(window, 'innerWidth', { value: 1500, configurable: true });
    Object.defineProperty(window, 'innerHeight', { value: 900, configurable: true });
    const { result } = renderHook(() => useViewport());
    expect(result.current.width).toBe(1500);
    expect(result.current.height).toBe(900);
  });

  it('updates on resize event (rAF debounced)', async () => {
    Object.defineProperty(window, 'innerWidth', { value: 1500, configurable: true });
    Object.defineProperty(window, 'innerHeight', { value: 900, configurable: true });
    const { result } = renderHook(() => useViewport());

    Object.defineProperty(window, 'innerWidth', { value: 1100, configurable: true });
    Object.defineProperty(window, 'innerHeight', { value: 800, configurable: true });

    await act(async () => {
      window.dispatchEvent(new Event('resize'));
      await new Promise((r) => requestAnimationFrame(() => r(undefined)));
    });

    expect(result.current.width).toBe(1100);
    expect(result.current.height).toBe(800);
  });

  it('removes resize listener on unmount', () => {
    const removeSpy = vi.spyOn(window, 'removeEventListener');
    const { unmount } = renderHook(() => useViewport());
    unmount();
    expect(removeSpy).toHaveBeenCalledWith('resize', expect.any(Function));
  });
});
