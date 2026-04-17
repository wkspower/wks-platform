import fs from 'node:fs';
import path from 'node:path';

import { describe, expect, it } from 'vitest';

const ROOT = path.resolve(__dirname, '../..');
const INDEX_HTML = fs.readFileSync(path.join(ROOT, 'index.html'), 'utf8');
const INDEX_CSS = fs.readFileSync(path.join(ROOT, 'src/index.css'), 'utf8');

// Matches an off-origin fetch target: href / src / url() with an http(s)
// URL or a protocol-relative `//host` URL (which the browser resolves to
// the page scheme and still constitutes an off-origin request). The SVG
// xmlns="http://www.w3.org/2000/svg" namespace attribute is intentionally
// ignored — it's not a fetch, just an identifier.
const OFF_ORIGIN_FETCH_RE =
  /(?:href|src)=["'](?:https?:)?\/\/[a-z]|url\(\s*["']?(?:https?:)?\/\/[a-z]/i;

describe('fonts', () => {
  it('index.html does not reference any external font CDN', () => {
    expect(INDEX_HTML).not.toMatch(/googleapis\.com/);
    expect(INDEX_HTML).not.toMatch(/gstatic\.com/);
    expect(INDEX_HTML).not.toMatch(OFF_ORIGIN_FETCH_RE);
  });

  it('index.css does not reference any off-origin URL', () => {
    expect(INDEX_CSS).not.toMatch(/googleapis\.com/);
    expect(INDEX_CSS).not.toMatch(/gstatic\.com/);
    expect(INDEX_CSS).not.toMatch(OFF_ORIGIN_FETCH_RE);
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
