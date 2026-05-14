/**
 * Mirrors the backend {@code FieldType} enum wire form. Lowercase tokens — the backend
 * serialises via {@code @JsonValue String wire()} (Story 2.5).
 */
export const FIELD_TYPES = [
  'text',
  'number',
  'date',
  'select',
  'checkbox',
  'textarea',
  'file',
  'email',
] as const;

export type FieldType = (typeof FIELD_TYPES)[number];
