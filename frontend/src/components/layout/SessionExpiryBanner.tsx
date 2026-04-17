import { X } from 'lucide-react';

import { Button } from '@/components/ui/Button';
import { useSessionExpiry } from '@/hooks/useSessionExpiry';
import { t } from '@/i18n';

export function SessionExpiryBanner() {
  const { expired, dismiss, triggerLogin } = useSessionExpiry();
  if (!expired) return null;
  return (
    <div
      role="alert"
      aria-live="assertive"
      className="flex items-center justify-between gap-[var(--space-4)] border-b border-[var(--warning)]/30 bg-[var(--warning)]/10 px-[var(--space-4)] py-[var(--space-2)] text-sm text-[var(--warning)]"
    >
      <span>{t('session.expired')}</span>
      <div className="flex items-center gap-[var(--space-2)]">
        <Button variant="link" size="sm" onClick={triggerLogin}>
          {t('session.reauth')}
        </Button>
        <Button
          variant="ghost"
          size="icon"
          onClick={dismiss}
          aria-label={t('session.dismiss')}
        >
          <X className="size-4" aria-hidden="true" />
        </Button>
      </div>
    </div>
  );
}
