import { locale } from '@/i18n';

export function formatDate(
  value: Date | number | string,
  options?: Intl.DateTimeFormatOptions,
): string {
  const date = value instanceof Date ? value : new Date(value);
  return new Intl.DateTimeFormat(locale, options ?? { dateStyle: 'medium' }).format(date);
}

export function formatDateTime(value: Date | number | string): string {
  return formatDate(value, { dateStyle: 'medium', timeStyle: 'short' });
}
