import { Link } from 'react-router-dom';

import { t } from '@/i18n';

export interface CaseBreadcrumbsProps {
  caseIdShort: string;
}

export function CaseBreadcrumbs({ caseIdShort }: CaseBreadcrumbsProps) {
  return (
    <nav aria-label={t('breadcrumbs.label')} className="text-xs text-[var(--muted-foreground)]">
      <Link to="/cases" className="hover:underline">
        {t('cases.title')}
      </Link>
      <span aria-hidden="true" className="mx-1.5">
        {'›'}
      </span>
      <span aria-current="page" className="font-mono">
        {caseIdShort}
      </span>
    </nav>
  );
}
