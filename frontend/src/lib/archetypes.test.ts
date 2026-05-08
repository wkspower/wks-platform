/**
 * Story 6.1 AC2 — Unit tests for the archetype affordance registry.
 *
 * Key assertions:
 * 1. Registry and entries are frozen (immutable at module-init).
 * 2. Each archetype id returns the expected affordance shape.
 * 3. Null / undefined / unknown inputs return the default affordance.
 * 4. No archetype literal strings leak into non-test renderer components
 *    (`frontend/src/components/**\/*.{ts,tsx}` excluding `*.test.*` files).
 */
import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join } from 'node:path';

import { describe, expect, it } from 'vitest';

import { ARCHETYPE_IDS, DEFAULT_AFFORDANCE, getArchetypeAffordance } from './archetypes';

describe('archetypes registry', () => {
  it('ARCHETYPE_IDS array is frozen', () => {
    expect(Object.isFrozen(ARCHETYPE_IDS)).toBe(true);
  });

  it('getArchetypeAffordance(undefined) returns default affordance', () => {
    const aff = getArchetypeAffordance(undefined);
    expect(aff).toEqual(DEFAULT_AFFORDANCE);
    expect(aff.ctaLabelKey).toBe('task.cta.complete');
    expect(aff.ctaTone).toBe('primary');
    expect(aff.confirmationFlow).toBe('inline');
    expect(aff.postActionState).toBe('idle');
  });

  it('getArchetypeAffordance(null) returns default affordance', () => {
    expect(getArchetypeAffordance(null)).toEqual(DEFAULT_AFFORDANCE);
  });

  it('getArchetypeAffordance("unknown_xyz") returns default affordance', () => {
    expect(getArchetypeAffordance('unknown_xyz')).toEqual(DEFAULT_AFFORDANCE);
  });

  it('draft_section affordance is correct', () => {
    const aff = getArchetypeAffordance('draft_section');
    expect(aff.ctaLabelKey).toBe('task.cta.draft_section.complete');
    expect(aff.ctaTone).toBe('secondary');
    expect(aff.confirmationFlow).toBe('inline');
    expect(aff.postActionState).toBe('idle');
    expect(Object.isFrozen(aff)).toBe(true);
  });

  it('submit_for_processing affordance is correct', () => {
    const aff = getArchetypeAffordance('submit_for_processing');
    expect(aff.ctaLabelKey).toBe('task.cta.submit_for_processing.complete');
    expect(aff.ctaTone).toBe('primary');
    expect(aff.confirmationFlow).toBe('processing-modal');
    expect(aff.postActionState).toBe('idle');
    expect(Object.isFrozen(aff)).toBe(true);
  });

  it('business_final affordance is correct', () => {
    const aff = getArchetypeAffordance('business_final');
    expect(aff.ctaLabelKey).toBe('task.cta.business_final.complete');
    expect(aff.ctaTone).toBe('primary');
    expect(aff.confirmationFlow).toBe('confirmation-dialog');
    expect(aff.postActionState).toBe('locked');
    expect(Object.isFrozen(aff)).toBe(true);
  });

  it('all archetype ids in ARCHETYPE_IDS return non-default affordance', () => {
    for (const id of ARCHETYPE_IDS) {
      const aff = getArchetypeAffordance(id);
      expect(aff).not.toEqual(DEFAULT_AFFORDANCE);
    }
  });
});

/**
 * AC2 contract: no archetype literal strings ('draft_section', 'submit_for_processing',
 * 'business_final') must appear in renderer components (non-test files).
 *
 * Uses Node.js fs APIs (available in Vitest's Node environment) to scan files under
 * `frontend/src/components/` recursively. This is equivalent to the CI grep check and
 * runs as a standard unit test so it fails the build automatically.
 */
describe('AC2 data-driven contract — no archetype literals in component sources', () => {
  const ARCHETYPE_LITERALS = ['draft_section', 'submit_for_processing', 'business_final'];

  /**
   * Recursively collect all .ts and .tsx files under a directory,
   * excluding test files (*.test.ts, *.test.tsx).
   */
  function collectSourceFiles(dir: string): string[] {
    const files: string[] = [];
    for (const entry of readdirSync(dir)) {
      const full = join(dir, entry);
      const st = statSync(full);
      if (st.isDirectory()) {
        files.push(...collectSourceFiles(full));
      } else if (
        (full.endsWith('.ts') || full.endsWith('.tsx')) &&
        !full.endsWith('.test.ts') &&
        !full.endsWith('.test.tsx')
      ) {
        files.push(full);
      }
    }
    return files;
  }

  it('no archetype literal strings appear in non-test component files', () => {
    // Resolve path relative to this test file: lib/ → components/ is ../components/
    const componentsDir = join(import.meta.dirname ?? __dirname, '..', 'components');
    const sourceFiles = collectSourceFiles(componentsDir);

    const violations: string[] = [];
    for (const filePath of sourceFiles) {
      const content = readFileSync(filePath, 'utf-8');
      for (const literal of ARCHETYPE_LITERALS) {
        // Match the literal as a JS string literal (single or double quotes)
        if (content.includes(`'${literal}'`) || content.includes(`"${literal}"`)) {
          violations.push(`${filePath}: contains '${literal}'`);
        }
      }
    }

    expect(violations).toEqual([]);
  });
});
