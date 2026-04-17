import fs from 'node:fs';
import path from 'node:path';

import { describe, expect, it } from 'vitest';

const ROOT = path.resolve(__dirname, '../..');
const INDEX_HTML = fs.readFileSync(path.join(ROOT, 'index.html'), 'utf8');
const INDEX_CSS = fs.readFileSync(path.join(ROOT, 'src/index.css'), 'utf8');

describe('fonts', () => {
  it('index.html does not reference any external font CDN', () => {
    expect(INDEX_HTML).not.toMatch(/googleapis\.com/);
    expect(INDEX_HTML).not.toMatch(/gstatic\.com/);
    expect(INDEX_HTML.match(/https?:\/\//) ?? []).toEqual([]);
  });

  it('index.css does not reference any off-origin URL', () => {
    expect(INDEX_CSS).not.toMatch(/googleapis\.com/);
    expect(INDEX_CSS).not.toMatch(/gstatic\.com/);
    expect(INDEX_CSS).not.toMatch(/https?:\/\//);
  });

  it('committed font assets exist on disk', () => {
    const fontDir = path.join(ROOT, 'src/assets/fonts');
    const required = [
      'Poppins-SemiBold.woff2',
      'Poppins-Bold.woff2',
      'Rubik-Regular.woff2',
      'Rubik-Medium.woff2',
    ];
    for (const file of required) {
      const full = path.join(fontDir, file);
      expect(fs.existsSync(full), `missing ${file}`).toBe(true);
    }
  });
});
