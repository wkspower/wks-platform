import { X } from 'lucide-react';

import { Button } from '@/components/ui/Button';
import { useLicenseBanner } from '@/hooks/useLicenseBanner';
import { t } from '@/i18n';
import { formatDate } from '@/lib/formatDate';

/**
 * Shows a dismissible warning banner when the license is expired or degraded.
 *
 * - State {@code "expired"}: renders an amber/warning banner with the expiry date.
 * - State {@code "degraded"}: renders the same banner without a date.
 * - State {@code "valid"} / {@code "oss"} / {@code null}: renders nothing.
 * - Dismissed banners are suppressed for the rest of the browser session via sessionStorage.
 *   A new tab or a state change will re-show the banner.
 *
 * Follows the same structure as {@link SessionExpiryBanner}.
 */
export function LicenseBanner() {
  const { state, expiredAt, isDismissed, dismiss } = useLicenseBanner();

  if (isDismissed || state === null || state === 'valid' || state === 'oss') {
    return null;
  }

  const message =
    state === 'expired'
      ? t('license.expired.banner', { date: expiredAt ? formatDate(expiredAt) : '' })
      : t('license.degraded.banner');

  return (
    <div
      role="alert"
      aria-live="assertive"
      className="flex items-center justify-between gap-[var(--space-4)] border-b border-[var(--warning)]/30 bg-[var(--warning)]/10 px-[var(--space-4)] py-[var(--space-2)] text-sm text-[var(--warning)]"
    >
      <span>{message}</span>
      <Button variant="ghost" size="icon" onClick={dismiss} aria-label={t('license.dismiss')}>
        <X className="size-4" aria-hidden="true" />
      </Button>
    </div>
  );
}
