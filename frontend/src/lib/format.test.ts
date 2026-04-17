import { describe, expect, it } from 'vitest';

import { formatDate, formatDateTime } from './formatDate';
import { formatNumber } from './formatNumber';

describe('format helpers', () => {
  it('formatDate returns a localized medium-style date', () => {
    const out = formatDate(new Date('2026-04-17T12:00:00Z'));
    // Locale en — exact spacing/format is platform dependent, just assert
    // the year + a recognisable month token are present.
    expect(out).toMatch(/2026/);
    expect(out).toMatch(/Apr/i);
  });

  it('formatDateTime includes both date and time', () => {
    const out = formatDateTime(new Date('2026-04-17T12:00:00Z'));
    expect(out).toMatch(/2026/);
    expect(out).toMatch(/[0-9]{1,2}:[0-9]{2}/);
  });

  it('formatNumber formats with grouping by default', () => {
    expect(formatNumber(1234567)).toBe('1,234,567');
  });

  it('formatNumber respects options', () => {
    expect(formatNumber(0.255, { style: 'percent', minimumFractionDigits: 1 })).toBe('25.5%');
  });
});
