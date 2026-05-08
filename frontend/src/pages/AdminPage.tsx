import { Link } from 'react-router-dom';

import { t } from '@/i18n';

export function AdminPage() {
  return (
    <section>
      <h1 className="font-heading text-2xl font-semibold">{t('page.admin.title')}</h1>
      <nav aria-label={t('page.admin.nav')} className="mt-[var(--space-4)]">
        <Link
          to="/admin/license"
          className="inline-flex items-center gap-[var(--space-2)] rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--card)] px-[var(--space-4)] py-[var(--space-3)] text-sm font-medium text-[var(--foreground)] transition-colors hover:bg-[var(--muted)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--secondary)] focus-visible:ring-offset-2"
        >
          {t('page.admin.license.link')}
        </Link>
      </nav>
    </section>
  );
}
