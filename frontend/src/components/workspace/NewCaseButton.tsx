import { Plus } from 'lucide-react';
import { useState } from 'react';

import { Button } from '@/components/ui/Button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/DropdownMenu';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/Tooltip';
import { useCaseType, useCaseTypes } from '@/hooks/useCaseTypes';
import { t } from '@/i18n';
import type { CaseTypeSummary } from '@/types/caseType';

import { NewCaseDialog } from './NewCaseDialog';

/**
 * Story 2.7 AC1 — primary CTA for creating a new case. Lives in the workspace header. Behavior:
 *
 * - 0 case types user can create → button disabled with tooltip explanation.
 * - 1 case type → click opens the dialog directly (skip the picker).
 * - ≥ 2 → click opens an inline dropdown listing each accessible case type by displayName.
 *
 * The verb filter is applied client-side from the {@code permissions[]} array on each summary
 * (Story 2.7 AC9 — backend populates the caller's verbs into the list response, so the dropdown
 * never shows non-actionable options).
 */
export function NewCaseButton() {
  const caseTypesQuery = useCaseTypes();
  const [openDialogFor, setOpenDialogFor] = useState<string | null>(null);

  const summaries = caseTypesQuery.data ?? [];
  const creatable = summaries.filter(
    (s: CaseTypeSummary) => Array.isArray(s.permissions) && s.permissions.includes('create'),
  );

  if (caseTypesQuery.isLoading) {
    // P23 — render an inert skeleton placeholder (not a disabled button) so the header layout
    // doesn't shift but the affordance reads as loading.
    return (
      <span
        aria-hidden
        className="inline-block h-9 w-28 animate-pulse rounded-[var(--radius-md)] bg-[var(--muted)]"
      />
    );
  }

  if (creatable.length === 0) {
    return (
      <Tooltip>
        <TooltipTrigger asChild>
          <span tabIndex={0}>
            <Button variant="default" disabled aria-disabled="true">
              <Plus className="size-4" aria-hidden />
              {t('cases.create.button')}
            </Button>
          </span>
        </TooltipTrigger>
        <TooltipContent>{t('cases.create.noTypes')}</TooltipContent>
      </Tooltip>
    );
  }

  // P1 — close handler must always set openDialogFor to null on close. The previous
  // `(open) => setOpenDialogFor(open ? <existing-id> : null)` captured the existing id from
  // render and re-asserted it on Radix's `open=true` callbacks, making the dialog effectively
  // un-closeable in the multi-type branch.
  const handleOpenChange = (open: boolean) => {
    if (!open) setOpenDialogFor(null);
  };

  // Single case type — click opens the dialog directly.
  if (creatable.length === 1) {
    const ct = creatable[0]!;
    return (
      <>
        <Button
          variant="default"
          onClick={() => setOpenDialogFor(ct.id)}
          aria-label={t('cases.create.button')}
        >
          <Plus className="size-4" aria-hidden />
          {t('cases.create.button')}
        </Button>
        <NewCaseDialogLoader caseTypeId={openDialogFor} onOpenChange={handleOpenChange} />
      </>
    );
  }

  // ≥ 2 case types — render dropdown picker.
  return (
    <>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="default">
            <Plus className="size-4" aria-hidden />
            {t('cases.create.button')}
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="start">
          {creatable.map((ct) => (
            <DropdownMenuItem key={ct.id} onSelect={() => setOpenDialogFor(ct.id)}>
              {ct.displayName}
            </DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>
      <NewCaseDialogLoader caseTypeId={openDialogFor} onOpenChange={handleOpenChange} />
    </>
  );
}

/**
 * Defers the case-type detail fetch until a user opens the dialog. Until then the picker only
 * needs the summary list (already loaded). On open we fetch the full {@code CaseTypeView} —
 * required for the form to know which fields to render and how to validate them.
 *
 * P1 — gate the rendered `caseType` on id-match so a fast pick-A-then-pick-B sequence doesn't
 * flash the previous case-type's fields while TanStack returns previous data during refetch.
 */
function NewCaseDialogLoader({
  caseTypeId,
  onOpenChange,
}: {
  caseTypeId: string | null;
  onOpenChange: (open: boolean) => void;
}) {
  const detail = useCaseType(caseTypeId ?? undefined);
  const matched = detail.data && caseTypeId && detail.data.id === caseTypeId ? detail.data : null;
  return (
    <NewCaseDialog
      open={caseTypeId !== null && matched !== null}
      caseType={matched}
      onOpenChange={onOpenChange}
    />
  );
}
