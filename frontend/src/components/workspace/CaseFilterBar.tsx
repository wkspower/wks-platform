import { ChevronDown, X } from 'lucide-react';
import { forwardRef, useEffect, useMemo, useRef, useState, type ChangeEvent } from 'react';

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
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/Popover';
import { t } from '@/i18n';
import { useUiStore, type CaseListFilters } from '@/stores/uiStore';
import type { CaseTypeSummary, CaseTypeView } from '@/types/caseType';
import { PRIORITIES, type Priority } from '@/types/priority';

export type CaseFilterBarVariant = 'full-width' | 'narrowed';

export interface CaseFilterBarProps {
  caseTypes: CaseTypeSummary[];
  /** View DTOs for the currently selected case types — supplies the status options. */
  selectedCaseTypeViews: CaseTypeView[];
  /** Search input value (controlled by the parent so debouncing happens upstream). */
  searchInput: string;
  onSearchInputChange: (value: string) => void;
  /** Story 2.6 — when narrowed, chips collapse to an "N filters" pill with overflow popover. */
  variant?: CaseFilterBarVariant;
}

function formatFilterCount(count: number): string {
  return count === 1
    ? t('cases.filter.oneFilter')
    : t('cases.filter.nFilters', { count: String(count) });
}

/**
 * AC4 + AC5 — filter bar with three multi-select dropdowns (status, priority, case type),
 * dismissible chips for active filters, a "Clear all" ghost button, and the search input.
 *
 * State is persisted to `useUiStore.caseListFilters` (Zustand). Selected case ID is NOT
 * persisted (UX spec §View memory).
 */
export const CaseFilterBar = forwardRef<HTMLDivElement, CaseFilterBarProps>(function CaseFilterBar(
  { caseTypes, selectedCaseTypeViews, searchInput, onSearchInputChange, variant = 'full-width' },
  ref,
) {
  const filters = useUiStore((s) => s.caseListFilters);
  const setFilters = useUiStore((s) => s.setCaseListFilters);
  const clearFilters = useUiStore((s) => s.clearCaseListFilters);
  const searchRef = useRef<HTMLInputElement>(null);
  const chipRowRef = useRef<HTMLDivElement>(null);

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
      // Skip when an IME composition is active — '/' may be a literal character in the
      // composing buffer (e.g., CJK input methods). Stealing focus would abort composition.
      if (e.isComposing) return;
      const target = e.target as HTMLElement | null;
      if (!target) return;
      const tag = target.tagName;
      if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT' || target.isContentEditable)
        return;
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

      {hasAny && variant === 'full-width' ? (
        <div
          ref={chipRowRef}
          tabIndex={-1}
          className="flex flex-wrap items-center gap-1 outline-none"
          data-chip-row
        >
          {renderChips(filters, caseTypes, statusOptions, (next) => setFilters(next))}
          <Button variant="ghost" size="sm" onClick={() => clearFilters()} className="ml-auto">
            {t('cases.filter.clearAll')}
          </Button>
        </div>
      ) : null}

      {variant === 'narrowed' ? (
        <NarrowedOverflow
          filters={filters}
          caseTypes={caseTypes}
          statusOptions={statusOptions}
          searchActive={(searchInput ?? '').length > 0}
          setFilters={(next) => setFilters(next)}
          clearFilters={() => clearFilters()}
          toggleStatus={toggleStatus}
          togglePriority={togglePriority}
          toggleCaseType={toggleCaseType}
        />
      ) : null}
    </div>
  );
});

interface NarrowedOverflowProps {
  filters: CaseListFilters;
  caseTypes: CaseTypeSummary[];
  statusOptions: { id: string; displayName: string }[];
  /** True when the search input has any text — counted in the pill so search isn't invisible. */
  searchActive: boolean;
  setFilters: (next: CaseListFilters) => void;
  clearFilters: () => void;
  toggleStatus: (id: string) => void;
  togglePriority: (id: Priority) => void;
  toggleCaseType: (id: string) => void;
}

function NarrowedOverflow({
  filters,
  caseTypes,
  statusOptions,
  searchActive,
  setFilters,
  clearFilters,
  toggleStatus,
  togglePriority,
  toggleCaseType,
}: NarrowedOverflowProps) {
  const [open, setOpen] = useState(false);
  const chipCount =
    filters.caseTypeIds.length + filters.statusIds.length + filters.priorities.length;
  // Search counts as one active filter so the user can see at a glance that something is narrowing
  // the list — pill never silently reads "0 filters" while a search is hiding rows.
  const activeCount = chipCount + (searchActive ? 1 : 0);

  if (chipCount === 0 && !searchActive) {
    // Empty state — wire the "Add filter" button to a popover that exposes the same three
    // filter dropdowns the full-width bar does (AC9 — "same controls as the full-width bar but
    // each in a single popover so the bar stays one-row").
    return (
      <div className="flex items-center">
        <Popover open={open} onOpenChange={setOpen}>
          <PopoverTrigger asChild>
            <Button
              variant="ghost"
              size="sm"
              data-testid="narrowed-filter-add"
              aria-label={t('cases.filter.add')}
            >
              {t('cases.filter.add')}
            </Button>
          </PopoverTrigger>
          <PopoverContent>
            <NarrowedAddFilterMenu
              filters={filters}
              caseTypes={caseTypes}
              statusOptions={statusOptions}
              toggleStatus={toggleStatus}
              togglePriority={togglePriority}
              toggleCaseType={toggleCaseType}
            />
          </PopoverContent>
        </Popover>
      </div>
    );
  }

  return (
    <div className="flex items-center gap-2">
      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger asChild>
          <Button variant="outline" size="sm" data-testid="narrowed-filter-pill">
            {formatFilterCount(activeCount)}
          </Button>
        </PopoverTrigger>
        <PopoverContent>
          <div className="flex flex-col gap-1">
            {renderChips(filters, caseTypes, statusOptions, setFilters)}
            {chipCount === 0 && searchActive ? (
              // Pill exists only because of search — surface the chip-row controls so the user can
              // still add structured filters without leaving narrowed mode.
              <NarrowedAddFilterMenu
                filters={filters}
                caseTypes={caseTypes}
                statusOptions={statusOptions}
                toggleStatus={toggleStatus}
                togglePriority={togglePriority}
                toggleCaseType={toggleCaseType}
              />
            ) : (
              <Button variant="ghost" size="sm" onClick={clearFilters} className="mt-1">
                {t('cases.filter.clearAll')}
              </Button>
            )}
          </div>
        </PopoverContent>
      </Popover>
    </div>
  );
}

interface NarrowedAddFilterMenuProps {
  filters: CaseListFilters;
  caseTypes: CaseTypeSummary[];
  statusOptions: { id: string; displayName: string }[];
  toggleStatus: (id: string) => void;
  togglePriority: (id: Priority) => void;
  toggleCaseType: (id: string) => void;
}

function NarrowedAddFilterMenu({
  filters,
  caseTypes,
  statusOptions,
  toggleStatus,
  togglePriority,
  toggleCaseType,
}: NarrowedAddFilterMenuProps) {
  return (
    <div className="flex flex-col gap-1">
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="sm" className="justify-between">
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

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="sm" className="justify-between">
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

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="sm" className="justify-between">
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
    </div>
  );
}

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
  function handleKeyDown(e: React.KeyboardEvent<HTMLButtonElement>) {
    // AC4 — Backspace on a focused chip dismisses it. After removal, advance focus to the
    // sibling chip's X (next, then previous), or fall back to the chip-row container so the user
    // never lands on document.body.
    if (e.key !== 'Backspace') return;
    e.preventDefault();
    const button = e.currentTarget;
    const row = button.closest('[data-chip-row]');
    const buttons = row
      ? Array.from(row.querySelectorAll<HTMLButtonElement>('button[data-chip-remove]'))
      : [];
    const idx = buttons.indexOf(button);
    const next = buttons[idx + 1] ?? buttons[idx - 1] ?? null;
    onRemove();
    // Defer focus until React has flushed the DOM removal of this chip.
    requestAnimationFrame(() => {
      if (next && document.body.contains(next)) {
        next.focus();
      } else if (row instanceof HTMLElement) {
        row.focus();
      }
    });
  }
  return (
    <span className="inline-flex items-center gap-1 rounded-[var(--radius-md)] bg-[var(--muted)] px-2 py-0.5 text-xs">
      <span className="text-[var(--muted-foreground)]">{`${label}:`}</span>
      <span>{value}</span>
      <button
        type="button"
        data-chip-remove
        aria-label={ariaLabel}
        onClick={onRemove}
        onKeyDown={handleKeyDown}
        className="ml-1 rounded p-0.5 hover:bg-[var(--border)]"
      >
        <X aria-hidden className="size-3" />
      </button>
    </span>
  );
}
