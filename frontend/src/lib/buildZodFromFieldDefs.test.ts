import { describe, expect, it } from 'vitest';

import type { FieldDefinition } from '@/types/caseType';

import { buildZodFromFieldDefs } from './buildZodFromFieldDefs';

function field(over: Partial<FieldDefinition>): FieldDefinition {
  return {
    id: 'x',
    displayName: 'X',
    type: 'text',
    required: true,
    requiredOnCreate: true,
    order: 0,
    options: [],
    ...over,
  };
}

describe('buildZodFromFieldDefs — create mode filter', () => {
  it('only includes fields where requiredOnCreate is true', () => {
    const schema = buildZodFromFieldDefs(
      [field({ id: 'a', requiredOnCreate: true }), field({ id: 'b', requiredOnCreate: false })],
      'create',
    );
    expect(Object.keys(schema.shape)).toEqual(['a']);
  });
});

describe('buildZodFromFieldDefs — text', () => {
  it('rejects empty string with required message', () => {
    const schema = buildZodFromFieldDefs([field({ id: 'a', maxLength: 10 })], 'create');
    const result = schema.safeParse({ a: '' });
    expect(result.success).toBe(false);
    if (!result.success) expect(result.error.issues[0]?.message).toMatch(/required/i);
  });

  it('rejects strings longer than maxLength', () => {
    const schema = buildZodFromFieldDefs([field({ id: 'a', maxLength: 3 })], 'create');
    const result = schema.safeParse({ a: 'abcd' });
    expect(result.success).toBe(false);
    if (!result.success) expect(result.error.issues[0]?.message).toMatch(/3/);
  });

  it('honours minLength when > 1', () => {
    const schema = buildZodFromFieldDefs([field({ id: 'a', minLength: 5 })], 'create');
    const result = schema.safeParse({ a: 'abc' });
    expect(result.success).toBe(false);
    if (!result.success) expect(result.error.issues[0]?.message).toMatch(/5/);
  });
});

describe('buildZodFromFieldDefs — number', () => {
  it('rejects empty string with required message (not 0)', () => {
    const schema = buildZodFromFieldDefs([field({ id: 'a', type: 'number' })], 'create');
    const result = schema.safeParse({ a: '' });
    expect(result.success).toBe(false);
    if (!result.success) expect(result.error.issues[0]?.message).toMatch(/required/i);
  });

  it('rejects "abc" with notNumber message (not NaN-passes-as-number)', () => {
    const schema = buildZodFromFieldDefs([field({ id: 'a', type: 'number' })], 'create');
    const result = schema.safeParse({ a: 'abc' });
    expect(result.success).toBe(false);
    if (!result.success) expect(result.error.issues[0]?.message).toMatch(/number/i);
  });

  it('honours min/max', () => {
    const schema = buildZodFromFieldDefs(
      [field({ id: 'a', type: 'number', min: 10, max: 20 })],
      'create',
    );
    expect(schema.safeParse({ a: '5' }).success).toBe(false);
    expect(schema.safeParse({ a: '25' }).success).toBe(false);
    expect(schema.safeParse({ a: '15' }).success).toBe(true);
  });

  it('honours step (multipleOf) within float epsilon', () => {
    const schema = buildZodFromFieldDefs([field({ id: 'a', type: 'number', step: 0.5 })], 'create');
    expect(schema.safeParse({ a: '1' }).success).toBe(true);
    expect(schema.safeParse({ a: '1.5' }).success).toBe(true);
    expect(schema.safeParse({ a: '1.3' }).success).toBe(false);
  });
});

describe('buildZodFromFieldDefs — date', () => {
  it('requires YYYY-MM-DD format', () => {
    const schema = buildZodFromFieldDefs([field({ id: 'a', type: 'date' })], 'create');
    expect(schema.safeParse({ a: '2026-1-1' }).success).toBe(false);
    expect(schema.safeParse({ a: '2026-01-01' }).success).toBe(true);
  });

  it('normalises non-zero-padded dateMin/dateMax for compare', () => {
    // YAML author may write "2026-2-3" (non-zero-padded). Lexicographic compare without
    // normalisation would fail "2026-12-01" >= "2026-2-3" → wrong outcome.
    const schema = buildZodFromFieldDefs(
      [field({ id: 'a', type: 'date', dateMin: '2026-2-3', dateMax: '2026-12-31' })],
      'create',
    );
    expect(schema.safeParse({ a: '2026-12-01' }).success).toBe(true);
    expect(schema.safeParse({ a: '2026-01-15' }).success).toBe(false);
  });
});

describe('buildZodFromFieldDefs — select', () => {
  it('rejects empty value with required message (not notInList)', () => {
    const schema = buildZodFromFieldDefs(
      [
        field({
          id: 'a',
          type: 'select',
          options: [
            { value: 'x', label: 'X' },
            { value: 'y', label: 'Y' },
          ],
        }),
      ],
      'create',
    );
    const result = schema.safeParse({ a: '' });
    expect(result.success).toBe(false);
    if (!result.success) expect(result.error.issues[0]?.message).toMatch(/required/i);
  });

  it('rejects off-list value with notInList message', () => {
    const schema = buildZodFromFieldDefs(
      [
        field({
          id: 'a',
          type: 'select',
          options: [{ value: 'x', label: 'X' }],
        }),
      ],
      'create',
    );
    const result = schema.safeParse({ a: 'z' });
    expect(result.success).toBe(false);
    if (!result.success) expect(result.error.issues[0]?.message).toMatch(/options/i);
  });
});

describe('buildZodFromFieldDefs — checkbox', () => {
  it('requires === true in create mode when requiredOnCreate', () => {
    const schema = buildZodFromFieldDefs([field({ id: 'a', type: 'checkbox' })], 'create');
    expect(schema.safeParse({ a: false }).success).toBe(false);
    expect(schema.safeParse({ a: true }).success).toBe(true);
  });

  it('uses f.required (not requiredOnCreate) in edit mode', () => {
    const schema = buildZodFromFieldDefs(
      [field({ id: 'a', type: 'checkbox', required: true, requiredOnCreate: false })],
      'edit',
    );
    expect(schema.safeParse({ a: false }).success).toBe(false);
    expect(schema.safeParse({ a: true }).success).toBe(true);
  });
});

describe('buildZodFromFieldDefs — file', () => {
  it('treats file fields as optional even when requiredOnCreate (3.1 lands the upload)', () => {
    const schema = buildZodFromFieldDefs([field({ id: 'a', type: 'file' })], 'create');
    expect(schema.safeParse({}).success).toBe(true);
    expect(schema.safeParse({ a: undefined }).success).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// Story 5.2 — 'submit' mode
// ---------------------------------------------------------------------------

describe('buildZodFromFieldDefs — submit mode', () => {
  it('includes ALL fields in submit mode — required fields are mandatory, optional fields have constraints validated', () => {
    // P1 fix: submit mode includes ALL fields so optional fields with constraints are still
    // validated. The distinction: required fields must be present; optional fields are wrapped
    // with .optional() so they may be omitted but if present must satisfy type constraints.
    const schema = buildZodFromFieldDefs(
      [
        field({ id: 'a', required: true, requiredOnCreate: false }),
        field({ id: 'b', required: false, requiredOnCreate: false }),
      ],
      'submit',
    );
    expect(Object.keys(schema.shape)).toContain('a');
    // P1: optional field 'b' is now included (as .optional()) so its constraints are validated
    expect(Object.keys(schema.shape)).toContain('b');
  });

  it('validates required text field in submit mode', () => {
    const schema = buildZodFromFieldDefs(
      [field({ id: 'a', type: 'text', required: true, requiredOnCreate: false })],
      'submit',
    );
    expect(schema.safeParse({ a: '' }).success).toBe(false);
    expect(schema.safeParse({ a: 'hello' }).success).toBe(true);
  });

  it('validates required checkbox in submit mode using f.required', () => {
    const schema = buildZodFromFieldDefs(
      [field({ id: 'a', type: 'checkbox', required: true, requiredOnCreate: false })],
      'submit',
    );
    expect(schema.safeParse({ a: false }).success).toBe(false);
    expect(schema.safeParse({ a: true }).success).toBe(true);
  });

  it('includes optional fields in submit schema (as .optional()) so constraints are still enforced', () => {
    // P1 fix: optional fields are included so type constraints (maxLength, min/max) are validated.
    // They are wrapped with .optional() so omitting the field entirely is still valid.
    const schema = buildZodFromFieldDefs(
      [field({ id: 'a', required: false, requiredOnCreate: false })],
      'submit',
    );
    // Optional field IS present in the schema shape (as .optional()), not excluded.
    expect(Object.keys(schema.shape)).toContain('a');
    // Empty object still passes because the field is optional.
    expect(schema.safeParse({}).success).toBe(true);
  });
});
