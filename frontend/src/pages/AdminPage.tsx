import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';

import { useCaseTypes } from '@/hooks/useCaseTypes';
import { t } from '@/i18n';

export function AdminPage() {
  const caseTypesQuery = useCaseTypes();
  const caseTypes = caseTypesQuery.data ?? null;

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

      {/* Story 4.6 AC6 — Mapping Inspector section */}
      <section aria-label={t('admin.mappingInspector.list.title')} className="mt-[var(--space-8)]">
        <h2 className="font-heading text-lg font-semibold">
          {t('admin.mappingInspector.list.title')}
        </h2>
        <p className="mt-[var(--space-1)] text-sm text-[var(--muted-foreground)]">
          {t('admin.mappingInspector.list.subtitle')}
        </p>
        {caseTypes && caseTypes.length > 0 ? (
          <ul
            className="mt-[var(--space-3)] flex flex-wrap gap-[var(--space-2)]"
            data-testid="mapping-inspector-case-type-list"
          >
            {caseTypes.map((ct) => (
              <li key={ct.id}>
                <Link
                  to={`/admin/mapping-inspector/${encodeURIComponent(ct.id)}`}
                  className="inline-flex items-center gap-[var(--space-2)] rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--card)] px-[var(--space-3)] py-[var(--space-2)] text-sm font-medium text-[var(--foreground)] transition-colors hover:bg-[var(--muted)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--secondary)] focus-visible:ring-offset-2"
                >
                  <span>{ct.displayName}</span>
                  <code className="text-xs text-[var(--muted-foreground)]">{ct.id}</code>
                </Link>
              </li>
            ))}
          </ul>
        ) : (
          <ManualCaseTypeIdForm />
        )}
      </section>
    </section>
  );
}

/**
 * Story 4.6 AC6 fallback — when the case-type listing endpoint returns no rows (or fails),
 * the admin can still navigate to the inspector by typing the caseTypeId manually. Documented
 * as a Phase-0 minimum to be upgraded by a future admin-list-endpoint story.
 */
function ManualCaseTypeIdForm() {
  const [value, setValue] = useState('');
  const navigate = useNavigate();
  const onSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const trimmed = value.trim();
    if (trimmed === '') {
      return;
    }
    navigate(`/admin/mapping-inspector/${encodeURIComponent(trimmed)}`);
  };
  return (
    <form
      onSubmit={onSubmit}
      className="mt-[var(--space-3)] flex max-w-md flex-col gap-[var(--space-2)] sm:flex-row sm:items-end"
      data-testid="mapping-inspector-fallback-form"
    >
      <label className="flex flex-col gap-[var(--space-1)] text-sm">
        <span className="font-medium">{t('admin.mappingInspector.list.fallback.label')}</span>
        <input
          type="text"
          value={value}
          onChange={(e) => setValue(e.target.value)}
          placeholder={t('admin.mappingInspector.list.fallback.placeholder')}
          className="rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--card)] px-[var(--space-3)] py-[var(--space-2)] text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--secondary)]"
        />
      </label>
      <button
        type="submit"
        className="inline-flex items-center justify-center rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--card)] px-[var(--space-4)] py-[var(--space-2)] text-sm font-medium text-[var(--foreground)] transition-colors hover:bg-[var(--muted)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--secondary)]"
      >
        {t('admin.mappingInspector.list.fallback.submit')}
      </button>
    </form>
  );
}
