import { describe, expect, it } from 'vitest';

import type { FieldDefinition } from '@/types/caseType';

import { formatFieldValue } from './fieldFormatters';

const baseSelect: FieldDefinition = {
  id: 'priority',
  displayName: 'Priority',
  type: 'select',
  requiredOnCreate: false,
  order: 0,
  options: [
    { value: 1, label: 'High' },
    { value: 2, label: 'Medium' },
    { value: 3, label: 'Low' },
  ],
} as unknown as FieldDefinition;

describe('formatFieldValue select coercion (Story 2.8 AC12)', () => {
  it('matches when stored value is string but option.value is numeric', () => {
    expect(formatFieldValue(baseSelect, '1')).toBe('High');
  });

  it('matches when stored value is numeric and option.value is numeric', () => {
    expect(formatFieldValue(baseSelect, 1)).toBe('High');
  });

  it('matches when stored value is numeric but option.value is string', () => {
    const stringOpts: FieldDefinition = {
      ...baseSelect,
      options: [
        { value: '1', label: 'High' },
        { value: '2', label: 'Medium' },
      ],
    } as unknown as FieldDefinition;
    expect(formatFieldValue(stringOpts, 2)).toBe('Medium');
  });

  it('returns em-dash for null/undefined', () => {
    expect(formatFieldValue(baseSelect, null)).toBe('—');
    expect(formatFieldValue(baseSelect, undefined)).toBe('—');
  });

  it('returns em-dash for empty string', () => {
    expect(formatFieldValue(baseSelect, '')).toBe('—');
  });
});
