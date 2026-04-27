import { describe, expect, it, vi } from 'vitest';

import type { FieldDefinition } from '@/types/caseType';

import { EM_DASH, formatFieldValue, isEmpty } from './fieldFormatters';

function field(over: Partial<FieldDefinition>): FieldDefinition {
  return {
    id: 'x',
    displayName: 'X',
    type: 'text',
    required: false,
    order: 0,
    options: [],
    ...over,
  };
}

describe('isEmpty', () => {
  it.each([null, undefined, ''])('treats %p as empty', (v) => {
    expect(isEmpty(v)).toBe(true);
  });
  it.each([0, false, 'x'])('treats %p as non-empty', (v) => {
    expect(isEmpty(v)).toBe(false);
  });
});

describe('formatFieldValue', () => {
  it('returns em-dash on empty for every type', () => {
    for (const type of [
      'text',
      'textarea',
      'number',
      'date',
      'select',
      'checkbox',
      'file',
    ] as const) {
      expect(formatFieldValue(field({ type }), null)).toBe(EM_DASH);
    }
  });

  it('text/textarea returns String(value)', () => {
    expect(formatFieldValue(field({ type: 'text' }), 'hello')).toBe('hello');
    expect(formatFieldValue(field({ type: 'textarea' }), 'a\nb')).toBe('a\nb');
  });

  it('number renders via formatNumber, non-number falls to em-dash', () => {
    expect(formatFieldValue(field({ type: 'number' }), 1234)).toMatch(/1[,.]?234/);
    expect(formatFieldValue(field({ type: 'number' }), 'oops')).toBe(EM_DASH);
  });

  it('date renders via formatDate, non-string falls to em-dash', () => {
    expect(formatFieldValue(field({ type: 'date' }), '2026-04-26T12:00:00Z')).toMatch(/2026/);
    expect(formatFieldValue(field({ type: 'date' }), 1234)).toBe(EM_DASH);
  });

  it('checkbox dispatches by truthy/falsy', () => {
    expect(formatFieldValue(field({ type: 'checkbox' }), true)).toBe('Yes');
    expect(formatFieldValue(field({ type: 'checkbox' }), false)).toBe('No');
    expect(formatFieldValue(field({ type: 'checkbox' }), 'true')).toBe('Yes');
  });

  it('file returns the table-cell placeholder', () => {
    expect(formatFieldValue(field({ type: 'file' }), 'whatever')).toBe('—');
  });

  it('select uses option.label, not the raw value (2.5 review patch regression)', () => {
    const f = field({
      type: 'select',
      options: [
        { value: 'low', label: 'Low' },
        { value: 'high', label: 'High' },
      ],
    });
    expect(formatFieldValue(f, 'low')).toBe('Low');
    expect(formatFieldValue(f, 'high')).toBe('High');
  });

  it('select warns and falls back to raw value when option missing', () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    const f = field({ type: 'select', options: [{ value: 'low', label: 'Low' }] });
    expect(formatFieldValue(f, 'unknown')).toBe('unknown');
    expect(warn).toHaveBeenCalled();
    warn.mockRestore();
  });
});
