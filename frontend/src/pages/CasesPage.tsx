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
  const clearFilters = useUiStore((s) => s.clearCaseListFilters);
  const caseTypesQuery = useCaseTypes();

  const allCaseTypes = useMemo(() => caseTypesQuery.data ?? [], [caseTypesQuery.data]);

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

  const primaryView = selectedCaseTypeViews[0];
  const columns = useMemo(() => (primaryView ? buildCaseColumns(primaryView) : []), [primaryView]);

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

  // AC7 #6 — debounced live region announcing result count.
  const [announcedCount, setAnnouncedCount] = useState<number | null>(null);
  useEffect(() => {
    const handle = setTimeout(
      () => setAnnouncedCount(filteredRows.length),
      RESULT_ANNOUNCE_DEBOUNCE_MS,
    );
    return () => clearTimeout(handle);
  }, [filteredRows.length]);

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
