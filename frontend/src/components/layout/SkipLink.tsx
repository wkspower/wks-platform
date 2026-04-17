import { t } from '@/i18n';

export function SkipLink() {
  return (
    <a
      href="#main"
      className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-50 focus:rounded-[var(--radius-md)] focus:bg-[var(--primary)] focus:px-4 focus:py-2 focus:text-[var(--primary-foreground)] focus:shadow-[var(--shadow-md)] focus:outline-none focus:ring-2 focus:ring-[var(--ring)] focus:ring-offset-2"
    >
      {t('a11y.skipToMain')}
    </a>
  );
}
