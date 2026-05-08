import { X } from 'lucide-react';

import { Button } from '@/components/ui/Button';
import { useLicenseBanner } from '@/hooks/useLicenseBanner';
import { t } from '@/i18n';
import { formatDate } from '@/lib/formatDate';

/**
 * Shows a dismissible banner when the license is expired, degraded, expiring soon, or in OSS mode.
 *
 * Priority order (only one banner shown at a time):
 *  1. {@code "expired"} — amber/warning with expiry date.
 *  2. {@code "degraded"} — amber/warning without date.
 *  3. {@code "expiring_soon"} — amber/warning with expiry date; assertive aria-live.
 *  4. {@code "oss"} — blue/info; polite aria-live.
 *
 * Dismissed banners are suppressed for the rest of the browser session via sessionStorage.
 * A new tab or a state change will re-show the banner.
 *
 * Follows the same structure as {@link SessionExpiryBanner}.
 */
export function LicenseBanner() {
  const { state, expiredAt, expiresAt, isOss, isExpiringSoon, isDismissed, dismiss } =
    useLicenseBanner();

  if (isDismissed || state === null) {
    return null;
  }

  // OSS info banner — polite aria-live (not an error; just an informational notice)
  if (isOss) {
    return (
      <div
        role="alert"
        aria-live="polite"
        className="flex items-center justify-between gap-[var(--space-4)] border-b border-[var(--secondary)]/30 bg-[var(--secondary)]/10 px-[var(--space-4)] py-[var(--space-2)] text-sm text-[var(--secondary)]"
      >
        <span>{t('license.oss.banner')}</span>
        <Button variant="ghost" size="icon" onClick={dismiss} aria-label={t('license.dismiss')}>
          <X className="size-4" aria-hidden="true" />
        </Button>
      </div>
    );
  }

  // Expiring-soon warning — assertive aria-live (operator must act before service interruption)
  if (isExpiringSoon && expiresAt !== null) {
    return (
      <div
        role="alert"
        aria-live="assertive"
        className="flex items-center justify-between gap-[var(--space-4)] border-b border-[var(--warning)]/30 bg-[var(--warning)]/10 px-[var(--space-4)] py-[var(--space-2)] text-sm text-[var(--warning)]"
      >
        <span>{t('license.expiring_soon.banner', { date: formatDate(expiresAt) })}</span>
        <Button variant="ghost" size="icon" onClick={dismiss} aria-label={t('license.dismiss')}>
          <X className="size-4" aria-hidden="true" />
        </Button>
      </div>
    );
  }

  // Expired and degraded states — existing behavior preserved
  if (state !== 'expired' && state !== 'degraded') {
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
