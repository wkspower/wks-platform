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
  // Status palette
  '--status-open',
  '--status-in-progress',
  '--status-review',
  '--status-resolved',
  '--status-closed',
  '--status-escalated',
  '--status-cyan',
  '--status-rose',
  '--status-indigo',
  '--status-teal',
  // Typography
  '--font-heading',
  '--font-body',
  '--font-mono',
  // Spacing
  '--space-1',
  '--space-2',
  '--space-3',
  '--space-4',
  '--space-6',
  '--space-8',
  '--space-12',
  // Radii
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
