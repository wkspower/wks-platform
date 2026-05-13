import fs from 'node:fs';
import path from 'node:path';

import { describe, expect, it } from 'vitest';

const tokens = fs.readFileSync(path.resolve(__dirname, 'tokens.css'), 'utf8');

function readToken(name: string): string {
  const re = new RegExp(`${name}:\\s*([^;]+);`);
  const match = tokens.match(re);
  if (!match) throw new Error(`Token not found: ${name}`);
  return match[1]!.trim();
}

function hexToRgb(hex: string): [number, number, number] {
  const cleaned = hex.replace('#', '');
  const expanded =
    cleaned.length === 3
      ? cleaned
          .split('')
          .map((c) => c + c)
          .join('')
      : cleaned;
  const r = parseInt(expanded.slice(0, 2), 16);
  const g = parseInt(expanded.slice(2, 4), 16);
  const b = parseInt(expanded.slice(4, 6), 16);
  return [r, g, b];
}

function relativeLuminance([r, g, b]: [number, number, number]): number {
  const channel = (c: number): number => {
    const s = c / 255;
    return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
  };
  return 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b);
}

function contrastRatio(fg: string, bg: string): number {
  const lFg = relativeLuminance(hexToRgb(fg));
  const lBg = relativeLuminance(hexToRgb(bg));
  const lighter = Math.max(lFg, lBg);
  const darker = Math.min(lFg, lBg);
  return (lighter + 0.05) / (darker + 0.05);
}

const PAIRS: Array<[string, string, string]> = [
  ['foreground on background', '--foreground', '--background'],
  ['primary-foreground on primary', '--primary-foreground', '--primary'],
  ['muted-foreground on background', '--muted-foreground', '--background'],
  ['secondary-foreground on secondary', '--secondary-foreground', '--secondary'],
  ['destructive-foreground on destructive', '--destructive-foreground', '--destructive'],
  // Status soft/on pairs — used by StatusBadge in soft-bg + strong-fg form.
  ['status-open-on on status-open-soft', '--status-open-on', '--status-open-soft'],
  [
    'status-in-progress-on on status-in-progress-soft',
    '--status-in-progress-on',
    '--status-in-progress-soft',
  ],
  ['status-review-on on status-review-soft', '--status-review-on', '--status-review-soft'],
  ['status-resolved-on on status-resolved-soft', '--status-resolved-on', '--status-resolved-soft'],
  ['status-closed-on on status-closed-soft', '--status-closed-on', '--status-closed-soft'],
  [
    'status-escalated-on on status-escalated-soft',
    '--status-escalated-on',
    '--status-escalated-soft',
  ],
  ['status-cyan-on on status-cyan-soft', '--status-cyan-on', '--status-cyan-soft'],
  ['status-rose-on on status-rose-soft', '--status-rose-on', '--status-rose-soft'],
  ['status-indigo-on on status-indigo-soft', '--status-indigo-on', '--status-indigo-soft'],
  ['status-teal-on on status-teal-soft', '--status-teal-on', '--status-teal-soft'],
];

describe('color contrast (WCAG AA)', () => {
  for (const [label, fgToken, bgToken] of PAIRS) {
    it(`${label} is at least 4.5:1`, () => {
      const ratio = contrastRatio(readToken(fgToken), readToken(bgToken));
      expect(ratio, `${label} ratio: ${ratio.toFixed(2)}`).toBeGreaterThanOrEqual(4.5);
    });
  }
});
