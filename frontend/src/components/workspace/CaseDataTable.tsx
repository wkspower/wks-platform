import {
  flexRender,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  useReactTable,
  type ColumnDef,
  type Row,
  type SortingState,
  type VisibilityState,
} from '@tanstack/react-table';
import { ChevronDown, ChevronUp, ChevronsUpDown } from 'lucide-react';
import { useEffect, useMemo, useRef, useState, type KeyboardEvent } from 'react';

import { Button } from '@/components/ui/Button';
import { Table, TBody, THead, Th, Td, Tr } from '@/components/ui/Table';
import { t } from '@/i18n';
import { urgencyDefaultSort } from '@/lib/buildCaseColumns';
import { cn } from '@/lib/cn';
import { useUiStore } from '@/stores/uiStore';
import type { CaseRow } from '@/types/case';

export type EmptyState = 'no-data' | 'filtered';
export type TableVariant = 'full-width' | 'narrowed';

export interface CaseDataTableProps<TRow extends CaseRow = CaseRow> {
  columns: ColumnDef<TRow>[];
  data: TRow[];
  isLoading?: boolean;
  emptyState: EmptyState;
  onClearFilters?: () => void;
  onRowSelect?: (row: TRow) => void;
  variant?: TableVariant;
  density?: 'comfortable' | 'compact';
  globalFilter?: string;
  ariaLabel: string;
  /** Column ids to keep visible when `variant === 'narrowed'`. Default: first three. */
  narrowedVisibleColumnIds?: string[];
  /**
   * Column ids to hide unconditionally (in addition to the narrowed-variant logic). The case list
   * uses this to drop the `caseType` column when exactly one case type is selected (it would be
   * redundant — every row shares the same type).
   */
  hiddenColumnIds?: string[];
  /**
   * Fired whenever TanStack Table's filtered row count changes (after status/priority filters AND
   * the global search filter are applied). Lets the parent surface an accurate count to a
   * live-region — `data.length` upstream of the table is the pre-search count.
   */
  onFilteredCountChange?: (count: number) => void;
  /**
   * Fired whenever TanStack Table's sorted row order changes. Story 2.6 — the workspace lifts
   * the row order to drive J/K keyboard navigation across the split-pane.
   */
  onSortedRowsChange?: (rows: TRow[]) => void;
}

const PAGE_SIZE = 50;
const SKELETON_ROW_COUNT = 5;

function SkeletonCell() {
  return (
    <Td>
      <div aria-hidden className="h-4 rounded bg-[var(--muted)]/60 animate-pulse" />
    </Td>
  );
}

/**
 * AC1 / AC2 / AC3 / AC7 / AC8 — config-driven cases table. Generic over the row type so the
 * Tasks screen (Story 8.1) can reuse the abstraction without surgery. TanStack Table v8 with
 * `getCoreRowModel`, `getSortedRowModel`, `getFilteredRowModel`, `getPaginationRowModel`
 * (50 rows/page); virtualization is Phase 1.
 */
export function CaseDataTable<TRow extends CaseRow = CaseRow>({
  columns,
  data,
  isLoading,
  emptyState,
  onClearFilters,
  onRowSelect,
  variant = 'full-width',
  globalFilter,
  ariaLabel,
  narrowedVisibleColumnIds,
  hiddenColumnIds,
  onFilteredCountChange,
  onSortedRowsChange,
}: CaseDataTableProps<TRow>) {
  const [sorting, setSorting] = useState<SortingState>([]);
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({});
  // P5 — AC8: highlighted-row affordance + announcement for the freshly-created case. The set
  // self-clears 6s after each push (uiStore TTL); subscribing here means a row repaints once the
  // dialog pushes its id, and again when the timer trims it.
  const recentlyCreated = useUiStore((s) => s.recentlyCreatedCaseIds);
  const announcedRef = useRef<Set<string>>(new Set());
  const [announceMessage, setAnnounceMessage] = useState<string>('');
  // AC7 #4 — roving tabindex. Only the active row carries `tabIndex={0}` so Tab leaves the
  // table after a single stop instead of walking every row. Initial entry point is row 0; on
  // `onFocus` the active index follows the user's last-focused row, so Tab back into the table
  // restores their position. Clamped down when the row count shrinks (filter / pagination).
  const [activeRowIndex, setActiveRowIndex] = useState(0);

  // Apply narrowed-variant visibility via the TanStack visibility API (not CSS) so screen
  // readers don't see hidden columns.
  useEffect(() => {
    const next: VisibilityState = {};
    if (variant === 'narrowed') {
      const keep =
        narrowedVisibleColumnIds ??
        columns
          .slice(0, 3)
          .map((c) => c.id ?? '')
          .filter(Boolean);
      for (const col of columns) {
        const id = col.id ?? '';
        if (id) next[id] = keep.includes(id);
      }
    }
    if (hiddenColumnIds) {
      for (const id of hiddenColumnIds) next[id] = false;
    }
    setColumnVisibility(next);
  }, [variant, columns, narrowedVisibleColumnIds, hiddenColumnIds]);

  const table = useReactTable<TRow>({
    data,
    columns,
    state: { sorting, columnVisibility, globalFilter: globalFilter ?? '' },
    onSortingChange: setSorting,
    onColumnVisibilityChange: setColumnVisibility,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    initialState: { pagination: { pageSize: PAGE_SIZE, pageIndex: 0 } },
    globalFilterFn: 'includesString',
  });

  // AC3 default sort applied when no user sort is active. `table` is stable across
  // re-renders; `data` triggers re-derivation through table.getRowModel().
  const tableRows = table.getRowModel().rows;
  const sortedRows = useMemo(() => {
    if (sorting.length > 0) return tableRows;
    const rows = [...tableRows];
    rows.sort((a, b) => urgencyDefaultSort(a.original, b.original));
    return rows;
  }, [sorting, tableRows]);

  // Story 2.6 — J/K keyboard nav must walk the full filtered+sorted set, not the paginated
  // page slice. `getRowModel().rows` is post-pagination (50 rows). `getSortedRowModel().rows`
  // is post-filter, post-sort, pre-pagination — exactly the "current sortedRows order across
  // pages" the workspace needs.
  const navRowsSource = table.getSortedRowModel().rows;
  const navRows = useMemo(() => {
    if (sorting.length > 0) return navRowsSource;
    const rows = [...navRowsSource];
    rows.sort((a, b) => urgencyDefaultSort(a.original, b.original));
    return rows;
  }, [sorting, navRowsSource]);

  const filteredCount = table.getFilteredRowModel().rows.length;
  const showEmpty = !isLoading && filteredCount === 0;

  useEffect(() => {
    onFilteredCountChange?.(filteredCount);
  }, [filteredCount, onFilteredCountChange]);

  useEffect(() => {
    if (!onSortedRowsChange) return;
    onSortedRowsChange(navRows.map((r) => r.original));
  }, [navRows, onSortedRowsChange]);

  // P5 — AC8: announce the freshly-created case once per id. Walks the recently-created set;
  // any id that appears in `data` AND has not yet been announced gets a one-shot polite live-
  // region update. The id is recorded in `announcedRef` so re-renders don't re-announce.
  useEffect(() => {
    if (recentlyCreated.size === 0) return;
    for (const id of recentlyCreated) {
      if (announcedRef.current.has(id)) continue;
      const matched = data.find((row) => row.id === id);
      if (!matched) continue;
      announcedRef.current.add(id);
      const idShort = id.length > 8 ? `${id.slice(0, 8)}…` : id;
      setAnnounceMessage(t('cases.created.announcement', { idShort }));
    }
  }, [recentlyCreated, data]);

  // AC5 — when the search input is non-empty OR the parent declared a filtered context, render the
  // 'filtered' empty copy. Earlier ternary form was parsed as `showEmpty && (cond1 ? true : cond2)`
  // which silently flipped the meaning when a sibling caller passed `globalFilter` with
  // `emptyState='no-data'`.
  const isSearching = (globalFilter ?? '').length > 0;
  const effectiveEmpty: EmptyState =
    showEmpty && (isSearching || emptyState === 'filtered') ? 'filtered' : emptyState;

  function handleRowKeyDown(
    event: KeyboardEvent<HTMLTableRowElement>,
    row: Row<TRow>,
    index: number,
  ) {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      onRowSelect?.(row.original);
      return;
    }
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault();
      const tbody = event.currentTarget.closest('tbody');
      if (!tbody) return;
      const rows = Array.from(tbody.querySelectorAll<HTMLTableRowElement>('tr[data-row]'));
      // Use the DOM position of the current row instead of the React `index` — when sorted, the
      // React `index` reflects sortedRows order, which may differ from DOM order if the upstream
      // table state changes between render and keystroke.
      const currentIdx = rows.indexOf(event.currentTarget);
      const startIdx = currentIdx >= 0 ? currentIdx : index;
      const dir = event.key === 'ArrowDown' ? 1 : -1;
      const nextIdx = Math.max(0, Math.min(rows.length - 1, startIdx + dir));
      rows[nextIdx]?.focus();
      setActiveRowIndex(nextIdx);
    }
  }

  const visibleColumnCount = table.getVisibleLeafColumns().length;

  return (
    <div className={cn(variant === 'narrowed' ? 'w-[380px] min-w-[380px]' : 'w-full')}>
      <Table aria-label={ariaLabel} role="table">
        <THead>
          {table.getHeaderGroups().map((hg) => (
            <Tr key={hg.id}>
              {hg.headers.map((header) => {
                const canSort = header.column.getCanSort();
                const sortDir = header.column.getIsSorted();
                const ariaSort: 'ascending' | 'descending' | 'none' =
                  sortDir === 'asc' ? 'ascending' : sortDir === 'desc' ? 'descending' : 'none';
                const SortIcon =
                  sortDir === 'asc' ? ChevronUp : sortDir === 'desc' ? ChevronDown : ChevronsUpDown;
                return (
                  <Th
                    key={header.id}
                    aria-sort={canSort ? ariaSort : undefined}
                    onClick={canSort ? header.column.getToggleSortingHandler() : undefined}
                    className={cn(canSort && 'cursor-pointer select-none')}
                  >
                    <span className="inline-flex items-center gap-1">
                      {flexRender(header.column.columnDef.header, header.getContext())}
                      {canSort && <SortIcon aria-hidden className="size-3" />}
                    </span>
                  </Th>
                );
              })}
            </Tr>
          ))}
        </THead>
        <TBody>
          {isLoading ? (
            Array.from({ length: SKELETON_ROW_COUNT }).map((_, i) => (
              <Tr key={`skeleton-${i}`}>
                {Array.from({ length: visibleColumnCount }).map((__, j) => (
                  <SkeletonCell key={`skeleton-${i}-${j}`} />
                ))}
              </Tr>
            ))
          ) : showEmpty ? (
            <Tr>
              <Td
                colSpan={visibleColumnCount}
                className="text-center text-[var(--muted-foreground)] py-8"
              >
                {effectiveEmpty === 'filtered'
                  ? t('cases.empty.filtered')
                  : t('cases.empty.noData')}
                {effectiveEmpty === 'filtered' && onClearFilters ? (
                  <Button variant="ghost" size="sm" onClick={onClearFilters} className="ml-2">
                    {t('cases.empty.clearFilters')}
                  </Button>
                ) : null}
              </Td>
            </Tr>
          ) : (
            sortedRows.map((row, idx) => (
              <Tr
                key={row.id}
                data-row
                data-row-id={row.original.id}
                data-state={row.getIsSelected() ? 'selected' : undefined}
                tabIndex={idx === Math.min(activeRowIndex, sortedRows.length - 1) ? 0 : -1}
                onFocus={() => setActiveRowIndex(idx)}
                onKeyDown={(e) => handleRowKeyDown(e, row, idx)}
                onClick={() => onRowSelect?.(row.original)}
                className={cn(
                  // P5 — recentlyCreated reuses the same border-l affordance as unread activity
                  // (AC8 explicitly says "the same affordance 2.5 §AC11 added for hasUnreadActivity").
                  (row.original.hasUnreadActivity || recentlyCreated.has(row.original.id)) &&
                    'border-l-[3px] border-[var(--primary)]',
                  'cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ring)]',
                )}
              >
                {row.getVisibleCells().map((cell, cellIdx) => (
                  <Td key={cell.id}>
                    {cellIdx === 0 && row.original.hasUnreadActivity ? (
                      <span className="sr-only">{t('cases.row.newActivity')}</span>
                    ) : null}
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </Td>
                ))}
              </Tr>
            ))
          )}
        </TBody>
      </Table>
      <div role="status" aria-live="polite" className="sr-only">
        {announceMessage}
      </div>
      {!isLoading && filteredCount > PAGE_SIZE ? (
        <div className="flex items-center justify-end gap-2 px-2 py-2 text-sm">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => table.previousPage()}
            disabled={!table.getCanPreviousPage()}
          >
            {t('cases.pagination.prev')}
          </Button>
          <span className="text-[var(--muted-foreground)]">
            {t('cases.pagination.page', {
              page: String(table.getState().pagination.pageIndex + 1),
              total: String(table.getPageCount()),
            })}
          </span>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => table.nextPage()}
            disabled={!table.getCanNextPage()}
          >
            {t('cases.pagination.next')}
          </Button>
        </div>
      ) : null}
    </div>
  );
}
