import { t } from '@/i18n';
import { formatDate } from '@/lib/formatDate';
import { formatNumber } from '@/lib/formatNumber';
import type { FieldDefinition } from '@/types/caseType';

export const EM_DASH = '—';

export function isEmpty(value: unknown): boolean {
  return value === null || value === undefined || value === '';
}

/**
 * Shared field-value formatter used by both the table cell renderer (`buildCaseColumns`) and
 * the read-only Properties tab (`renderFieldValue`). Returns plain strings — callers wrap in
 * appropriate JSX (whitespace-pre-wrap for textarea, etc).
 */
export function formatFieldValue(field: FieldDefinition, value: unknown): string {
  if (isEmpty(value)) return EM_DASH;
  switch (field.type) {
    case 'number':
      return typeof value === 'number' ? formatNumber(value) : EM_DASH;
    case 'date':
      return typeof value === 'string' ? formatDate(value) : EM_DASH;
    case 'checkbox':
      return value === true || value === 'true'
        ? t('cases.field.checkbox.true')
        : t('cases.field.checkbox.false');
    case 'file':
      return t('cases.field.file.placeholder');
    case 'select': {
      // Story 2.8 AC12 — coerce both sides to string before compare. YAML may declare numeric
      // option values (`value: 1`) while the JSONB round-trip stringifies stored data; strict
      // equality across that type boundary silently fails to label-match.
      const stored = String(value);
      const match = field.options?.find((opt) => String(opt.value) === stored);
      if (match) return match.label;

      console.warn(`formatFieldValue: select '${field.id}' has no option for '${String(value)}'`);
      return String(value);
    }
    case 'text':
    case 'textarea':
    case 'email':
    default:
      return String(value);
  }
}
