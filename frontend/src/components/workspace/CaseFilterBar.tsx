import { ChevronDown, X } from 'lucide-react';
import { forwardRef, useEffect, useMemo, useRef, type ChangeEvent } from 'react';

import { Button } from '@/components/ui/Button';
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/DropdownMenu';
import { Input } from '@/components/ui/Input';
import { t } from '@/i18n';
import { useUiStore, type CaseListFilters } from '@/stores/uiStore';
import type { CaseTypeSummary, CaseTypeView } from '@/types/caseType';
import { PRIORITIES, type Priority } from '@/types/priority';

export interface CaseFilterBarProps {
  caseTypes: CaseTypeSummary[];
  /** View DTOs for the currently selected case types — supplies the status options. */
  selectedCaseTypeViews: CaseTypeView[];
  /** Search input value (controlled by the parent so debouncing happens upstream). */
  searchInput: string;
  onSearchInputChange: (value: string) => void;
}

/**
 * AC4 + AC5 — filter bar with three multi-select dropdowns (status, priority, case type),
 * dismissible chips for active filters, a "Clear all" ghost button, and the search input.
 *
 * State is persisted to `useUiStore.caseListFilters` (Zustand). Selected case ID is NOT
 * persisted (UX spec §View memory).
 */
export const CaseFilterBar = forwardRef<HTMLDivElement, CaseFilterBarProps>(function CaseFilterBar(
  { caseTypes, selectedCaseTypeViews, searchInput, onSearchInputChange },
  ref,
) {
  const filters = useUiStore((s) => s.caseListFilters);
  const setFilters = useUiStore((s) => s.setCaseListFilters);
  const clearFilters = useUiStore((s) => s.clearCaseListFilters);
  const searchRef = useRef<HTMLInputElement>(null);

  // Compose the union of statuses across selected case types.
  const statusOptions = useMemo(() => {
    const seen = new Map<string, string>();
    for (const ct of selectedCaseTypeViews) {
      for (const s of ct.statuses) {
        if (!seen.has(s.id)) seen.set(s.id, s.displayName);
      }
    }
    return Array.from(seen.entries()).map(([id, displayName]) => ({ id, displayName }));
  }, [selectedCaseTypeViews]);

  // AC5 — `/` focuses the search input from anywhere outside an editable element.
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (e.key !== '/' || e.metaKey || e.ctrlKey || e.altKey || e.shiftKey) return;
      const target = e.target as HTMLElement | null;
      if (!target) return;
      const tag = target.tagName;
      if (tag === 'INPUT' || tag === 'TEXTAREA' || target.isContentEditable) return;
      e.preventDefault();
      searchRef.current?.focus();
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, []);

  function toggleCaseType(id: string) {
    const has = filters.caseTypeIds.includes(id);
    setFilters({
      ...filters,
      caseTypeIds: has ? filters.caseTypeIds.filter((x) => x !== id) : [...filters.caseTypeIds, id],
    });
  }

  function toggleStatus(id: string) {
    const has = filters.statusIds.includes(id);
    setFilters({
      ...filters,
      statusIds: has ? filters.statusIds.filter((x) => x !== id) : [...filters.statusIds, id],
    });
  }

  function togglePriority(id: Priority) {
    const has = filters.priorities.includes(id);
    setFilters({
      ...filters,
      priorities: has ? filters.priorities.filter((x) => x !== id) : [...filters.priorities, id],
    });
  }

  const hasAny =
    filters.caseTypeIds.length > 0 || filters.statusIds.length > 0 || filters.priorities.length > 0;

  return (
    <div ref={ref} className="flex flex-col gap-2">
      <div className="flex items-center gap-2">
        {/* Status */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline" size="sm">
              {t('cases.filter.status')}
              <ChevronDown aria-hidden className="size-3" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuLabel>{t('cases.filter.status')}</DropdownMenuLabel>
            <DropdownMenuSeparator />
            {statusOptions.map((s) => (
              <DropdownMenuCheckboxItem
                key={s.id}
                checked={filters.statusIds.includes(s.id)}
                onCheckedChange={() => toggleStatus(s.id)}
              >
                {s.displayName}
              </DropdownMenuCheckboxItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>

        {/* Priority */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline" size="sm">
              {t('cases.filter.priority')}
              <ChevronDown aria-hidden className="size-3" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuLabel>{t('cases.filter.priority')}</DropdownMenuLabel>
            <DropdownMenuSeparator />
            {PRIORITIES.map((p) => (
              <DropdownMenuCheckboxItem
                key={p}
                checked={filters.priorities.includes(p)}
                onCheckedChange={() => togglePriority(p)}
              >
                {t(`cases.priority.${p}`)}
              </DropdownMenuCheckboxItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>

        {/* Case type */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline" size="sm">
              {t('cases.filter.caseType')}
              <ChevronDown aria-hidden className="size-3" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuLabel>{t('cases.filter.caseType')}</DropdownMenuLabel>
            <DropdownMenuSeparator />
            {caseTypes.map((ct) => (
              <DropdownMenuCheckboxItem
                key={ct.id}
                checked={filters.caseTypeIds.includes(ct.id)}
                onCheckedChange={() => toggleCaseType(ct.id)}
              >
                {ct.displayName}
              </DropdownMenuCheckboxItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>

        {/* Search */}
        <Input
          ref={searchRef}
          type="search"
          aria-label={t('cases.search.label')}
          placeholder={t('cases.search.placeholder')}
          value={searchInput}
          onChange={(e: ChangeEvent<HTMLInputElement>) => onSearchInputChange(e.target.value)}
          className="ml-auto w-64"
        />
      </div>

      {hasAny ? (
        <div className="flex flex-wrap items-center gap-1">
          {renderChips(filters, caseTypes, statusOptions, (next) => setFilters(next))}
          <Button variant="ghost" size="sm" onClick={() => clearFilters()} className="ml-auto">
            {t('cases.filter.clearAll')}
          </Button>
        </div>
      ) : null}
    </div>
  );
});

function renderChips(
  filters: CaseListFilters,
  caseTypes: CaseTypeSummary[],
  statusOptions: { id: string; displayName: string }[],
  setFilters: (next: CaseListFilters) => void,
) {
  const chips: ReturnType<typeof Chip>[] = [];

  for (const id of filters.caseTypeIds) {
    const ct = caseTypes.find((c) => c.id === id);
    chips.push(
      <Chip
        key={`ct-${id}`}
        label={t('cases.filter.caseType')}
        value={ct?.displayName ?? id}
        ariaLabel={t('cases.filter.removeChip', {
          label: t('cases.filter.caseType'),
          value: ct?.displayName ?? id,
        })}
        onRemove={() =>
          setFilters({ ...filters, caseTypeIds: filters.caseTypeIds.filter((x) => x !== id) })
        }
      />,
    );
  }
  for (const id of filters.statusIds) {
    const s = statusOptions.find((x) => x.id === id);
    chips.push(
      <Chip
        key={`s-${id}`}
        label={t('cases.filter.status')}
        value={s?.displayName ?? id}
        ariaLabel={t('cases.filter.removeChip', {
          label: t('cases.filter.status'),
          value: s?.displayName ?? id,
        })}
        onRemove={() =>
          setFilters({ ...filters, statusIds: filters.statusIds.filter((x) => x !== id) })
        }
      />,
    );
  }
  for (const p of filters.priorities) {
    chips.push(
      <Chip
        key={`p-${p}`}
        label={t('cases.filter.priority')}
        value={t(`cases.priority.${p}`)}
        ariaLabel={t('cases.filter.removeChip', {
          label: t('cases.filter.priority'),
          value: t(`cases.priority.${p}`),
        })}
        onRemove={() =>
          setFilters({ ...filters, priorities: filters.priorities.filter((x) => x !== p) })
        }
      />,
    );
  }
  return chips;
}

interface ChipProps {
  label: string;
  value: string;
  ariaLabel: string;
  onRemove: () => void;
}

function Chip({ label, value, ariaLabel, onRemove }: ChipProps) {
  return (
    <span className="inline-flex items-center gap-1 rounded-[var(--radius-md)] bg-[var(--muted)] px-2 py-0.5 text-xs">
      <span className="text-[var(--muted-foreground)]">{`${label}:`}</span>
      <span>{value}</span>
      <button
        type="button"
        aria-label={ariaLabel}
        onClick={onRemove}
        className="ml-1 rounded p-0.5 hover:bg-[var(--border)]"
      >
        <X aria-hidden className="size-3" />
      </button>
    </span>
  );
}
