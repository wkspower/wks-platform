import { useEffect, useMemo, useState } from 'react';

import { CaseDataTable } from '@/components/workspace/CaseDataTable';
import { CaseFilterBar } from '@/components/workspace/CaseFilterBar';
import { useCases } from '@/hooks/useCases';
import { useCaseTypes, useCaseTypeViews } from '@/hooks/useCaseTypes';
import { t } from '@/i18n';
import { buildCaseColumns } from '@/lib/buildCaseColumns';
import { useUiStore } from '@/stores/uiStore';
import type { CaseRow } from '@/types/case';

const SEARCH_DEBOUNCE_MS = 150;
const RESULT_ANNOUNCE_DEBOUNCE_MS = 200;

/**
 * Story 2.5 — config-driven cases list. Composes the filter bar + table; runs the
 * multi-case-type fetch strategy from §Multi-case-type fetch when more than one case type is
 * active. List-only width (split-pane and detail panel arrive in Story 2.6).
 */
export function CasesPage() {
  const filters = useUiStore((s) => s.caseListFilters);
  const setFilters = useUiStore((s) => s.setCaseListFilters);
  const clearFilters = useUiStore((s) => s.clearCaseListFilters);
  const caseTypesQuery = useCaseTypes();

  const allCaseTypes = useMemo(() => caseTypesQuery.data ?? [], [caseTypesQuery.data]);

  // Reconcile persisted filters against the live catalog once it loads. Drop case-type ids the
  // caller no longer has `view` verb on (or that were removed by a deployment) — they would 403
  // through `useCases` with no recovery surface. Silent prune (UX spec "Confidence Not Safety");
  // a Phase-1 notification primitive can announce the change explicitly.
  //
  // Same effect also auto-pins the first-by-name case type as a real chip when the user has no
  // selection. Without this, `effectiveCaseTypeIds` falls back to `[allCaseTypes[0].id]` silently
  // and the user can't tell why "no cases" is showing — they don't see a chip indicating which
  // case type the table represents. Auto-pinning makes the selection visible and dismissable.
  useEffect(() => {
    if (!caseTypesQuery.isSuccess) return;
    const liveIds = new Set(allCaseTypes.map((ct) => ct.id));
    const reconciledCaseTypeIds = filters.caseTypeIds.filter((id) => liveIds.has(id));
    if (reconciledCaseTypeIds.length === 0 && allCaseTypes.length > 0) {
      reconciledCaseTypeIds.push(allCaseTypes[0]!.id);
    }
    if (
      reconciledCaseTypeIds.length !== filters.caseTypeIds.length ||
      reconciledCaseTypeIds.some((id, i) => filters.caseTypeIds[i] !== id)
    ) {
      setFilters({ ...filters, caseTypeIds: reconciledCaseTypeIds });
    }
    // Status reconciliation runs after the case-type views have loaded, since statuses live on
    // the per-case-type detail DTO. Handled in the next effect.
  }, [caseTypesQuery.isSuccess, allCaseTypes, filters, setFilters]);

  // No selection → fall back to the first case type the caller is verbed for. Phase 0 has no
  // "All case types" option; that's a Phase 1 backend feature once GET /api/cases accepts a
  // comma-separated list.
  const effectiveCaseTypeIds = useMemo(() => {
    if (filters.caseTypeIds.length > 0) return filters.caseTypeIds;
    return allCaseTypes.length > 0 ? [allCaseTypes[0]!.id] : [];
  }, [filters.caseTypeIds, allCaseTypes]);

  const caseTypeViewsQueries = useCaseTypeViews(effectiveCaseTypeIds);
  const selectedCaseTypeViews = useMemo(
    () => caseTypeViewsQueries.flatMap((q) => (q.data ? [q.data] : [])),
    [caseTypeViewsQueries],
  );

  // Reconcile persisted statusIds against the union of statuses across the loaded case-type
  // views. Only runs once every active case-type view has loaded so we don't prune mid-flight.
  const allViewsLoaded =
    effectiveCaseTypeIds.length > 0 &&
    caseTypeViewsQueries.length === effectiveCaseTypeIds.length &&
    caseTypeViewsQueries.every((q) => q.isSuccess);
  useEffect(() => {
    if (!allViewsLoaded || filters.statusIds.length === 0) return;
    const liveStatusIds = new Set<string>();
    for (const view of selectedCaseTypeViews) {
      for (const s of view.statuses) liveStatusIds.add(s.id);
    }
    const reconciledStatusIds = filters.statusIds.filter((id) => liveStatusIds.has(id));
    if (reconciledStatusIds.length !== filters.statusIds.length) {
      setFilters({ ...filters, statusIds: reconciledStatusIds });
    }
  }, [allViewsLoaded, selectedCaseTypeViews, filters, setFilters]);

  const primaryView = selectedCaseTypeViews[0];
  const caseTypesById = useMemo(
    () => new Map(allCaseTypes.map((ct) => [ct.id, ct.displayName])),
    [allCaseTypes],
  );
  const columns = useMemo(
    () => (primaryView ? buildCaseColumns(primaryView, { caseTypesById }) : []),
    [primaryView, caseTypesById],
  );
  // AC4 — hide the case-type column when only one case type is selected (redundant: every row
  // shares the same type). Show it when multiple are active.
  const hiddenColumnIds = useMemo(
    () => (effectiveCaseTypeIds.length <= 1 ? ['caseType'] : []),
    [effectiveCaseTypeIds.length],
  );

  const casesResult = useCases({
    caseTypeIds: effectiveCaseTypeIds,
    sort: ['updatedAt,desc'],
  });

  // Apply the active priority/status filters client-side. Backend supports a single status
  // param but multi-select would require N×M fan-out — Phase 1 collapses.
  const filteredRows: CaseRow[] = useMemo(() => {
    let rows = casesResult.data;
    if (filters.statusIds.length > 0) {
      rows = rows.filter((r) => filters.statusIds.includes(r.status));
    }
    if (filters.priorities.length > 0) {
      rows = rows.filter((r) => {
        const p = r.fields['priority'];
        return typeof p === 'string' && (filters.priorities as string[]).includes(p);
      });
    }
    return rows;
  }, [casesResult.data, filters.statusIds, filters.priorities]);

  // AC5 — debounced search.
  const [searchInput, setSearchInput] = useState('');
  const [globalFilter, setGlobalFilter] = useState('');
  useEffect(() => {
    const handle = setTimeout(() => setGlobalFilter(searchInput), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(handle);
  }, [searchInput]);

  // AC7 #6 — debounced live region announcing result count. Mirrors the table's own filtered
  // count (after status/priority + global search) via `onFilteredCountChange` so the announcement
  // matches what the user actually sees.
  const [tableFilteredCount, setTableFilteredCount] = useState(0);
  const [announcedCount, setAnnouncedCount] = useState<number | null>(null);
  useEffect(() => {
    const handle = setTimeout(
      () => setAnnouncedCount(tableFilteredCount),
      RESULT_ANNOUNCE_DEBOUNCE_MS,
    );
    return () => clearTimeout(handle);
  }, [tableFilteredCount]);

  const isLoading = casesResult.isLoading || caseTypesQuery.isLoading;
  const hasAnyFilter =
    filters.caseTypeIds.length > 0 ||
    filters.statusIds.length > 0 ||
    filters.priorities.length > 0 ||
    globalFilter.length > 0;

  return (
    <section>
      <h1 className="font-heading text-2xl font-semibold">{t('cases.title')}</h1>

      <div className="mt-4 flex flex-col gap-4">
        <CaseFilterBar
          caseTypes={allCaseTypes}
          selectedCaseTypeViews={selectedCaseTypeViews}
          searchInput={searchInput}
          onSearchInputChange={setSearchInput}
        />

        <CaseDataTable
          columns={columns}
          data={filteredRows}
          isLoading={isLoading}
          emptyState={hasAnyFilter ? 'filtered' : 'no-data'}
          onClearFilters={() => {
            clearFilters();
            setSearchInput('');
          }}
          globalFilter={globalFilter}
          ariaLabel={t('cases.table.label')}
          onFilteredCountChange={setTableFilteredCount}
          hiddenColumnIds={hiddenColumnIds}
        />

        <div aria-live="polite" className="sr-only">
          {announcedCount !== null
            ? t('cases.search.results', { count: String(announcedCount) })
            : ''}
        </div>
      </div>
    </section>
  );
}
