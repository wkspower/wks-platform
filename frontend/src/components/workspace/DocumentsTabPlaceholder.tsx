import { FileText } from 'lucide-react';

import { t } from '@/i18n';

export function DocumentsTabPlaceholder() {
  return (
    <div
      data-testid="documents-placeholder"
      className="flex flex-col items-center justify-center py-12 text-center"
    >
      <FileText aria-hidden className="size-10 text-[var(--muted-foreground)]/60" />
      <h2 className="mt-3 text-base font-semibold">{t('documents.placeholder.title')}</h2>
      <p className="mt-1 text-sm text-[var(--muted-foreground)]">
        {t('documents.placeholder.body')}
      </p>
    </div>
  );
}
