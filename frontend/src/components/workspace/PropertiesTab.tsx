import { Fragment } from 'react';

import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/Tooltip';
import { t } from '@/i18n';
import { renderFieldValue } from '@/lib/renderFieldValue';
import type { CaseDto } from '@/types/case';
import type { CaseTypeView } from '@/types/caseType';

export interface PropertiesTabProps {
  caseDto: CaseDto;
  caseTypeView: CaseTypeView;
}

export function PropertiesTab({ caseDto, caseTypeView }: PropertiesTabProps) {
  // Defensive: types declare `caseTypeView.fields` and `caseDto.data` as required, but a
  // legacy/malformed backend response (or an orphaned case-type version) could leave either
  // missing. Render the empty-state copy instead of letting a TypeError reach the boundary.
  const fields = caseTypeView?.fields ?? [];
  const data = caseDto?.data ?? {};
  if (fields.length === 0) {
    return <p className="py-6 text-sm text-[var(--muted-foreground)]">{t('properties.empty')}</p>;
  }

  return (
    <dl className="grid grid-cols-[120px_1fr] gap-x-3 gap-y-2 px-1 py-2">
      {fields.map((field) => (
        <Fragment key={field.id}>
          {/*
            Tooltip wraps the label so the full displayName is reachable when the 120px column
            ellipses. `tabIndex` intentionally omitted: adding a tab-stop per field would create
            an N-step tab chain through the panel for keyboard users — Radix Tooltip exposes the
            content on hover/focus of any focusable descendant, but the `<dt>` itself has no
            interactive purpose, so we keep it out of the tab order.
          */}
          <Tooltip>
            <TooltipTrigger asChild>
              <dt className="truncate text-sm text-[var(--muted-foreground)]">
                {field.displayName}
              </dt>
            </TooltipTrigger>
            <TooltipContent>{field.displayName}</TooltipContent>
          </Tooltip>
          <dd className="text-sm">{renderFieldValue(field, data[field.id])}</dd>
        </Fragment>
      ))}
    </dl>
  );
}
