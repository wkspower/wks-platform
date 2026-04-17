import { t } from '@/i18n';

export function DevConsolePage() {
  return (
    <section>
      <h1 className="font-heading text-2xl font-semibold">{t('page.dev.title')}</h1>
      <p className="mt-[var(--space-2)] text-[var(--muted-foreground)]">
        {t('page.dev.placeholder')}
      </p>
    </section>
  );
}
