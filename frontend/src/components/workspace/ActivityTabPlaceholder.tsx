import { MessageSquare } from 'lucide-react';

import { t } from '@/i18n';

export function ActivityTabPlaceholder() {
  return (
    <div
      data-testid="activity-placeholder"
      className="flex flex-col items-center justify-center py-12 text-center"
    >
      <MessageSquare aria-hidden className="size-10 text-[var(--muted-foreground)]/60" />
      <h2 className="mt-3 text-base font-semibold">{t('activity.placeholder.title')}</h2>
      <p className="mt-1 text-sm text-[var(--muted-foreground)]">
        {t('activity.placeholder.body')}
      </p>
    </div>
  );
}
