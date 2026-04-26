import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { CaseDataTable } from '@/components/workspace/CaseDataTable';
import { CaseFilterBar } from '@/components/workspace/CaseFilterBar';
import { CaseWorkspace } from '@/components/workspace/CaseWorkspace';
import { useCases } from '@/hooks/useCases';
import { useCaseTypes, useCaseTypeViews } from '@/hooks/useCaseTypes';
import { t } from '@/i18n';
import { buildCaseColumns } from '@/lib/buildCaseColumns';
import { useUiStore } from '@/stores/uiStore';
import type { CaseRow } from '@/types/case';

const SEARCH_DEBOUNCE_MS = 150;
const RESULT_ANNOUNCE_DEBOUNCE_MS = 200;

export function CasesPage() {
  const filters = useUiStore((s) => s.caseListFilters);
  const setFilters = useUiStore((s) => s.setCaseListFilters);
  const clearFilters = useUiStore((s) => s.clearCaseListFilters);
  const caseTypesQuery = useCaseTypes();
  const navigate = useNavigate();
  const { caseId: routeCaseId } = useParams<{ caseId?: string }>();
  const selectedCaseId = routeCaseId ?? null;

  const allCaseTypes = useMemo(() => caseTypesQuery.data ?? [], [caseTypesQuery.data]);

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
  }, [caseTypesQuery.isSuccess, allCaseTypes, filters, setFilters]);

  const effectiveCaseTypeIds = useMemo(() => {
    if (filters.caseTypeIds.length > 0) return filters.caseTypeIds;
    return allCaseTypes.length > 0 ? [allCaseTypes[0]!.id] : [];
  }, [filters.caseTypeIds, allCaseTypes]);

  const caseTypeViewsQueries = useCaseTypeViews(effectiveCaseTypeIds);
  const selectedCaseTypeViews = useMemo(
    () => caseTypeViewsQueries.flatMap((q) => (q.data ? [q.data] : [])),
    [caseTypeViewsQueries],
  );

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
  const hiddenColumnIds = useMemo(
    () => (effectiveCaseTypeIds.length <= 1 ? ['caseType'] : []),
    [effectiveCaseTypeIds.length],
  );

  const casesResult = useCases({
    caseTypeIds: effectiveCaseTypeIds,
  });

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

  const [searchInput, setSearchInput] = useState('');
  const [globalFilter, setGlobalFilter] = useState('');
  useEffect(() => {
    const handle = setTimeout(() => setGlobalFilter(searchInput), SEARCH_DEBOUNCE_MS);
    return () => clearTimeout(handle);
  }, [searchInput]);

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

  const [sortedRows, setSortedRows] = useState<CaseRow[]>([]);
  const handleSortedRowsChange = useCallback((rows: CaseRow[]) => setSortedRows(rows), []);

  const handleSelectionChange = useCallback(
    (id: string | null) => {
      navigate(id === null ? '/cases' : `/cases/${id}`);
    },
    [navigate],
  );

  const filterBar = (
    <CaseFilterBar
      caseTypes={allCaseTypes}
      selectedCaseTypeViews={selectedCaseTypeViews}
      searchInput={searchInput}
      onSearchInputChange={setSearchInput}
    />
  );

  const list = (
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
      onSortedRowsChange={handleSortedRowsChange}
      hiddenColumnIds={hiddenColumnIds}
    />
  );

  return (
    <section className="flex flex-1 flex-col">
      {selectedCaseId === null ? (
        <h1 className="font-heading text-2xl font-semibold">{t('cases.title')}</h1>
      ) : null}

      <div className="mt-4 flex flex-1 flex-col gap-4">
        <CaseWorkspace
          filterBar={filterBar}
          list={list}
          selectedCaseId={selectedCaseId}
          onSelectionChange={handleSelectionChange}
          sortedRows={sortedRows}
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
