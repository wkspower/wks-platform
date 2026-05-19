import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeEach, vi } from 'vitest';

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

beforeEach(() => {
  // Each test owns its own fetch stub. Guard so an un-stubbed fetch call
  // fails loudly instead of attempting a real network round-trip.
  vi.stubGlobal(
    'fetch',
    vi.fn(() => {
      throw new Error('fetch called without a stub — use vi.spyOn(globalThis, "fetch") in the test');
    }),
  );
});
