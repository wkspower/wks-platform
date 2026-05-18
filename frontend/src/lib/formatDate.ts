import { locale } from '@/i18n';

export function formatDate(value: Date | number | string, options?: Intl.DateTimeFormatOptions): string {
  const date = value instanceof Date ? value : new Date(value);
  return new Intl.DateTimeFormat(locale, options ?? { dateStyle: 'medium' }).format(date);
}

export function formatDateTime(value: Date | number | string): string {
  return formatDate(value, { dateStyle: 'medium', timeStyle: 'short' });
}

/**
 * Story 9-2 — format an ISO instant (or {@link Date}) as a human relative string against {@code
 * now} ("just now", "2 minutes ago", "3 hours ago", "5 days ago", or the absolute date for older
 * events). Uses {@link Intl.RelativeTimeFormat} so output respects the active locale.
 *
 * <p>{@code now} is injectable so component tests can pin the relative output deterministically.
 *
 * <p>For events older than 30 days we fall back to {@link formatDate} (medium date) — relative
 * strings beyond ~a month stop being useful for an audit feed.
 */
export function formatRelativeTime(value: Date | number | string, now: Date = new Date()): string {
  const date = value instanceof Date ? value : new Date(value);
  const diffMs = date.getTime() - now.getTime();
  const absSeconds = Math.abs(diffMs) / 1000;
  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' });
  if (absSeconds < 30) return rtf.format(0, 'second');
  if (absSeconds < 60) return rtf.format(Math.round(diffMs / 1000), 'second');
  if (absSeconds < 3600) return rtf.format(Math.round(diffMs / 60_000), 'minute');
  if (absSeconds < 86_400) return rtf.format(Math.round(diffMs / 3_600_000), 'hour');
  if (absSeconds < 30 * 86_400) return rtf.format(Math.round(diffMs / 86_400_000), 'day');
  return formatDate(date);
}
