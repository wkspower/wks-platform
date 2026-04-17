import fs from 'node:fs';
import path from 'node:path';

import { describe, expect, it } from 'vitest';

import en from './en.json';

const SRC = path.resolve(__dirname, '..');
const T_CALL_RE = /\bt\(\s*['"]([^'"]+)['"]/g;

function walk(dir: string, acc: string[] = []): string[] {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (entry.name === 'node_modules' || entry.name === 'dist') continue;
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      walk(full, acc);
    } else if (entry.isFile() && /\.(ts|tsx)$/.test(entry.name)) {
      acc.push(full);
    }
  }
  return acc;
}

describe('i18n keys', () => {
  it('every t(...) reference in source has a bundle entry in en.json', () => {
    const bundle = en as Record<string, string>;
    const missing = new Set<string>();
    for (const file of walk(SRC)) {
      // Skip the i18n module itself + tests + the test infra.
      if (file.includes(`${path.sep}i18n${path.sep}`)) continue;
      if (/\.test\.(ts|tsx)$/.test(file)) continue;
      const content = fs.readFileSync(file, 'utf8');
      let match: RegExpExecArray | null;
      T_CALL_RE.lastIndex = 0;
      while ((match = T_CALL_RE.exec(content)) !== null) {
        const key = match[1];
        if (key && bundle[key] === undefined) missing.add(key);
      }
    }
    expect([...missing]).toEqual([]);
  });

  it('en.json is non-empty and has the required core keys', () => {
    const bundle = en as Record<string, string>;
    const required = [
      'login.title',
      'login.submit',
      'login.error.invalid',
      'nav.primary',
      'nav.cases',
      'session.expired',
      'session.reauth',
      'error.title',
      'error.reload',
      'a11y.skipToMain',
    ];
    for (const key of required) {
      expect(bundle[key], `missing required key ${key}`).toBeTypeOf('string');
    }
  });
});
