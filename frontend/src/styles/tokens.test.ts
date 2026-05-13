import fs from 'node:fs';
import path from 'node:path';

import { describe, expect, it } from 'vitest';

const tokens = fs.readFileSync(path.resolve(__dirname, 'tokens.css'), 'utf8');

const REQUIRED_TOKENS = [
  // Brand + semantic
  '--primary',
  '--primary-foreground',
  '--secondary',
  '--secondary-foreground',
  '--brand-navy',
  '--sidebar-bg',
  '--background',
  '--card',
  '--muted',
  '--border',
  '--muted-foreground',
  '--foreground',
  '--destructive',
  '--warning',
  '--success',
  '--info',
  '--ring',
  '--ring-destructive',
  '--destructive-foreground',
  '--disabled-bg',
  '--disabled-fg',
  // Status palette — solids and soft/on pairs
  '--status-open',
  '--status-open-soft',
  '--status-open-on',
  '--status-in-progress',
  '--status-in-progress-soft',
  '--status-in-progress-on',
  '--status-review',
  '--status-review-soft',
  '--status-review-on',
  '--status-resolved',
  '--status-resolved-soft',
  '--status-resolved-on',
  '--status-closed',
  '--status-closed-soft',
  '--status-closed-on',
  '--status-escalated',
  '--status-escalated-soft',
  '--status-escalated-on',
  '--status-cyan',
  '--status-cyan-soft',
  '--status-cyan-on',
  '--status-rose',
  '--status-rose-soft',
  '--status-rose-on',
  '--status-indigo',
  '--status-indigo-soft',
  '--status-indigo-on',
  '--status-teal',
  '--status-teal-soft',
  '--status-teal-on',
  // Typography
  '--font-heading',
  '--font-body',
  '--font-mono',
  '--text-xs',
  '--text-sm',
  '--text-base',
  '--text-lg',
  '--text-xl',
  '--text-2xl',
  '--leading-tight',
  '--leading-normal',
  '--tracking-tight',
  '--tracking-normal',
  // Spacing
  '--space-1',
  '--space-2',
  '--space-3',
  '--space-4',
  '--space-5',
  '--space-6',
  '--space-7',
  '--space-8',
  '--space-9',
  '--space-10',
  '--space-11',
  '--space-12',
  // Radii
  '--radius-sm',
  '--radius-md',
  '--radius-lg',
  '--radius-xl',
  '--radius-full',
  // Elevation
  '--shadow-sm',
  '--shadow-md',
  '--shadow-lg',
  // Motion
  '--motion-fast',
  '--motion-normal',
  '--motion-slow',
  '--ease-out',
  '--ease-in-out',
];

describe('design tokens', () => {
  it('every required token is declared in tokens.css', () => {
    const missing = REQUIRED_TOKENS.filter((token) => !tokens.includes(`${token}:`));
    expect(missing).toEqual([]);
  });
});
