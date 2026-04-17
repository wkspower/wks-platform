import fs from 'node:fs';
import path from 'node:path';

import { describe, expect, it } from 'vitest';

const SRC = path.resolve(__dirname, '..');
const FETCH_RE = /\bfetch\s*\(/;
const ALLOWED = new Set([path.join(SRC, 'api', 'client.ts')]);

function walk(dir: string, acc: string[] = []): string[] {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      walk(full, acc);
    } else if (entry.isFile() && /\.(ts|tsx)$/.test(entry.name)) {
      acc.push(full);
    }
  }
  return acc;
}

describe('no direct fetch() calls outside api/client.ts', () => {
  it('grep finds fetch( only in api/client.ts', () => {
    const offenders: string[] = [];
    for (const file of walk(SRC)) {
      if (ALLOWED.has(file)) continue;
      // Skip tests; tests legitimately stub fetch via MSW (no direct calls
      // on the global) but a regex match alone would be a false positive.
      if (/\.test\.(ts|tsx)$/.test(file)) continue;
      const content = fs.readFileSync(file, 'utf8');
      if (FETCH_RE.test(content)) offenders.push(file);
    }
    expect(offenders).toEqual([]);
  });
});
