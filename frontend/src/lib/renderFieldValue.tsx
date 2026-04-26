import type { ReactElement } from 'react';

import { t } from '@/i18n';
import { EM_DASH, formatFieldValue, isEmpty } from '@/lib/fieldFormatters';
import type { FieldDefinition } from '@/types/caseType';

/**
 * Properties-tab field renderer. Wraps `formatFieldValue` and adds the read-only display bits
 * the table cell does not need: textarea newlines via `whitespace-pre-wrap`, the
 * "See Documents tab" copy for files (which the table renders as em-dash).
 */
export function renderFieldValue(field: FieldDefinition, value: unknown): ReactElement {
  if (isEmpty(value)) return <span>{EM_DASH}</span>;

  if (field.type === 'textarea') {
    return <span className="whitespace-pre-wrap">{String(value)}</span>;
  }

  if (field.type === 'file') {
    return (
      <span className="text-[var(--muted-foreground)]">{t('properties.file.placeholder')}</span>
    );
  }

  return <span>{formatFieldValue(field, value)}</span>;
}
