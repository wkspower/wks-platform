import { useQueryClient } from '@tanstack/react-query';
import { Plus, Search } from 'lucide-react';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { BulkActionBar } from '@/components/cases/BulkActionBar';
import { CaseDetailBody } from '@/components/cases/CaseDetailBody';
import {
  ASSIGNEE_FIELD_ID,
  CasesTable,
  STATUS_FIELD_ID,
  type InlineEditTarget,
} from '@/components/cases/CasesTable';
import { FilterBar } from '@/components/cases/FilterBar';
import { NewCaseDialog } from '@/components/cases/NewCaseDialog';
import { RecordDrawer } from '@/components/chrome/RecordDrawer';
import { ShortcutsOverlay } from '@/components/chrome/ShortcutsOverlay';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Spinner } from '@/components/ui/Spinner';
import { toast } from '@/components/ui/Toaster';
import { applyOptimisticStatus, useCase, useCases } from '@/hooks/useCases';
import { useCaseTypes, useCaseTypeViews } from '@/hooks/useCaseTypes';
import { useTableKeyboard } from '@/hooks/useTableKeyboard';
import { transitionCase, updateCase } from '@/api/cases';
import { caseQueryKeys } from '@/lib/queryKeys';
import { useUiStore } from '@/stores/uiStore';
import type { CaseDto, CaseRow, CaseSummary } from '@/types/case';
import type { CaseTypeView } from '@/types/caseType';

export function CasesPage() {
  const { caseId } = useParams<{ caseId: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { data: types } = useCaseTypes();
  const filters = useUiStore((s) => s.caseListFilters);
  const [search, setSearch] = useState('');
  const [newOpen, setNewOpen] = useState(false);

  const effectiveTypes = useMemo(
    () => (filters.caseTypeIds.length > 0 ? filters.caseTypeIds : (types ?? []).map((t) => t.id)),
    [filters.caseTypeIds, types],
  );

  const { data: rows, isLoading, isError } = useCases({ caseTypeIds: effectiveTypes });
  const viewQueries = useCaseTypeViews(effectiveTypes);
  const views = useMemo<CaseTypeView[]>(() => {
    const out: CaseTypeView[] = [];
    for (const q of viewQueries) if (q.data) out.push(q.data);
    return out;
  }, [viewQueries]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return rows;
    return rows.filter((r) => {
      if (r.id.toLowerCase().includes(q)) return true;
      for (const v of Object.values(r.fields)) {
        if (typeof v === 'string' && v.toLowerCase().includes(q)) return true;
      }
      return false;
    });
  }, [rows, search]);

  // ---- Selection state ----
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [lastSelectedId, setLastSelectedId] = useState<string | null>(null);

  // Drop ids no longer in filtered rows (e.g. when filters change).
  useEffect(() => {
    if (selectedIds.size === 0) return;
    const visible = new Set(filtered.map((r) => r.id));
    let mutated = false;
    const next = new Set<string>();
    for (const id of selectedIds) {
      if (visible.has(id)) next.add(id);
      else mutated = true;
    }
    if (mutated) setSelectedIds(next);
  }, [filtered, selectedIds]);

  const onToggleSelect = useCallback(
    (id: string, opts?: { range?: boolean }) => {
      if (opts?.range && lastSelectedId) {
        const ids = filtered.map((r) => r.id);
        const from = ids.indexOf(lastSelectedId);
        const to = ids.indexOf(id);
        if (from !== -1 && to !== -1) {
          const [a, b] = from < to ? [from, to] : [to, from];
          setSelectedIds((prev) => {
            const next = new Set(prev);
            for (let i = a; i <= b; i++) next.add(ids[i]!);
            return next;
          });
          setLastSelectedId(id);
          return;
        }
      }
      setSelectedIds((prev) => {
        const next = new Set(prev);
        if (next.has(id)) next.delete(id);
        else next.add(id);
        return next;
      });
      setLastSelectedId(id);
    },
    [filtered, lastSelectedId],
  );

  const onToggleSelectAll = useCallback(() => {
    setSelectedIds((prev) => {
      if (prev.size === filtered.length && filtered.length > 0) return new Set();
      return new Set(filtered.map((r) => r.id));
    });
  }, [filtered]);

  // ---- Focus + keyboard ----
  const [focusedRowId, setFocusedRowId] = useState<string | null>(null);
  useEffect(() => {
    if (focusedRowId && filtered.some((r) => r.id === focusedRowId)) return;
    setFocusedRowId(filtered[0]?.id ?? null);
  }, [filtered, focusedRowId]);

  // ---- Inline edit ----
  const [editing, setEditing] = useState<InlineEditTarget | null>(null);
  const startEdit = useCallback((t: InlineEditTarget) => setEditing(t), []);
  const cancelEdit = useCallback(() => setEditing(null), []);

  const commitEdit = useCallback(
    async (row: CaseRow, view: CaseTypeView | undefined, target: InlineEditTarget, next: unknown) => {
      setEditing(null);

      if (target.fieldId === STATUS_FIELD_ID) {
        const action = String(next);
        if (action === row.status) return;
        const { rollback, prevStatus } = applyOptimisticStatus(queryClient, row.id, action);
        try {
          const dto = await transitionCase(row.id, { action });
          queryClient.setQueryData(caseQueryKeys.detail(dto.id), dto);
          queryClient.invalidateQueries({ queryKey: caseQueryKeys.lists() });
          toast({
            tone: 'success',
            message: `Status set to ${dto.caseType.statuses.find((s) => s.id === dto.status)?.displayName ?? dto.status}`,
            undo: prevStatus
              ? async () => {
                  try {
                    const reverted = await transitionCase(row.id, { action: prevStatus });
                    queryClient.setQueryData(caseQueryKeys.detail(reverted.id), reverted);
                    queryClient.invalidateQueries({ queryKey: caseQueryKeys.lists() });
                  } catch (e) {
                    const msg = e instanceof Error ? e.message : 'unknown error';
                    toast({ tone: 'error', message: `Couldn't undo — ${msg}` });
                  }
                }
              : undefined,
          });
        } catch (e) {
          rollback();
          const msg = e instanceof Error ? e.message : 'unknown error';
          toast({ tone: 'error', message: `Couldn't change status — ${msg}` });
        }
        return;
      }

      if (target.fieldId === ASSIGNEE_FIELD_ID) {
        // Backend does not yet expose an assignee mutation endpoint. Optimistic-only.
        applyAssigneeOptimistic(queryClient, row.id, next == null ? null : String(next));
        toast({ tone: 'info', message: 'Assignee updated locally (not persisted)' });
        return;
      }

      // Dynamic field — use updateCase (PUT /api/cases/{id}).
      if (!view) return;
      const current = queryClient.getQueryData<CaseDto>(caseQueryKeys.detail(row.id));
      const version = current?.version ?? null;
      if (version == null) {
        toast({ tone: 'error', message: 'Open the case once before editing — version unknown.' });
        return;
      }
      const prevValue = row.fields[target.fieldId];
      const optimisticPatch = applyFieldOptimistic(queryClient, row.id, target.fieldId, next);
      try {
        const dto = await updateCase(row.id, { data: { [target.fieldId]: next }, version });
        queryClient.setQueryData(caseQueryKeys.detail(dto.id), dto);
        queryClient.invalidateQueries({ queryKey: caseQueryKeys.lists() });
        toast({
          tone: 'success',
          message: 'Field updated',
          undo: async () => {
            const latest = queryClient.getQueryData<CaseDto>(caseQueryKeys.detail(dto.id)) ?? dto;
            try {
              const reverted = await updateCase(dto.id, {
                data: { [target.fieldId]: prevValue },
                version: latest.version,
              });
              queryClient.setQueryData(caseQueryKeys.detail(dto.id), reverted);
              queryClient.invalidateQueries({ queryKey: caseQueryKeys.lists() });
            } catch (e) {
              const msg = e instanceof Error ? e.message : 'unknown error';
              toast({ tone: 'error', message: `Couldn't undo — ${msg}` });
            }
          },
        });
      } catch (e) {
        optimisticPatch.rollback();
        const msg = e instanceof Error ? e.message : 'unknown error';
        toast({ tone: 'error', message: `Couldn't update field — ${msg}` });
      }
    },
    [queryClient],
  );

  // ---- Bulk actions ----
  const commonStatuses = useMemo(() => {
    if (views.length === 0) return [];
    const first = views[0]!;
    const sets = views.slice(1).map((v) => new Set(v.statuses.map((s) => s.id)));
    return first.statuses
      .filter((s) => sets.every((set) => set.has(s.id)))
      .map((s) => ({ id: s.id, displayName: s.displayName }));
  }, [views]);

  const bulkSetStatus = useCallback(
    async (statusId: string) => {
      const ids = Array.from(selectedIds);
      if (ids.length === 0) return;
      const prevStatuses = new Map<string, string>();
      for (const id of ids) {
        const row = filtered.find((r) => r.id === id);
        if (row) prevStatuses.set(id, row.status);
        applyOptimisticStatus(queryClient, id, statusId);
      }
      const results = await Promise.allSettled(
        ids.map((id) => transitionCase(id, { action: statusId })),
      );
      let ok = 0;
      let fail = 0;
      for (const r of results) {
        if (r.status === 'fulfilled') {
          ok++;
          queryClient.setQueryData(caseQueryKeys.detail(r.value.id), r.value);
        } else {
          fail++;
        }
      }
      queryClient.invalidateQueries({ queryKey: caseQueryKeys.lists() });
      if (fail === 0) {
        toast({
          tone: 'success',
          message: `Updated ${ok} ${ok === 1 ? 'case' : 'cases'}`,
          undo: async () => {
            const undoResults = await Promise.allSettled(
              Array.from(prevStatuses.entries()).map(([id, prev]) =>
                transitionCase(id, { action: prev }),
              ),
            );
            const undoFail = undoResults.filter((r) => r.status === 'rejected').length;
            if (undoFail > 0) {
              toast({ tone: 'error', message: `Couldn't undo ${undoFail} of ${prevStatuses.size}` });
            }
            queryClient.invalidateQueries({ queryKey: caseQueryKeys.lists() });
          },
        });
      } else {
        toast({
          tone: 'error',
          message: `Updated ${ok}, ${fail} failed`,
        });
      }
    },
    [filtered, queryClient, selectedIds],
  );

  const bulkAssign = useCallback(
    async (assignee: string) => {
      // Frontend-only optimistic (backend lacks PUT /assignee).
      for (const id of selectedIds) {
        applyAssigneeOptimistic(queryClient, id, assignee);
      }
      toast({
        tone: 'info',
        message: `Assigned ${selectedIds.size} cases locally (not persisted)`,
      });
    },
    [queryClient, selectedIds],
  );

  // ---- Drawer / detail ----
  const { data: drawerDto, isLoading: drawerLoading, isError: drawerError } = useCase(caseId ?? null);

  // ---- Keyboard ----
  const [shortcutsOpen, setShortcutsOpen] = useState(false);
  const searchRef = useRef<HTMLInputElement>(null);

  const focusedIndex = focusedRowId ? filtered.findIndex((r) => r.id === focusedRowId) : -1;
  const moveFocus = useCallback(
    (delta: 1 | -1) => {
      if (filtered.length === 0) return;
      const idx = focusedIndex === -1 ? 0 : Math.max(0, Math.min(filtered.length - 1, focusedIndex + delta));
      setFocusedRowId(filtered[idx]!.id);
    },
    [filtered, focusedIndex],
  );

  useTableKeyboard({
    disabled: editing != null || newOpen || shortcutsOpen,
    drawerOpen: Boolean(caseId),
    onNext: () => moveFocus(1),
    onPrev: () => moveFocus(-1),
    onOpen: () => {
      if (focusedRowId) navigate(`/cases/${focusedRowId}`);
    },
    onStartEdit: () => {
      if (focusedRowId) setEditing({ rowId: focusedRowId, fieldId: STATUS_FIELD_ID });
    },
    onToggleSelect: () => {
      if (focusedRowId) onToggleSelect(focusedRowId);
    },
    onRangeSelect: () => {
      if (focusedRowId) onToggleSelect(focusedRowId, { range: true });
    },
    onFocusSearch: () => searchRef.current?.focus(),
    onCloseDrawer: () => navigate('/cases'),
    onClearSelection: () => setSelectedIds(new Set()),
    onShowShortcuts: () => setShortcutsOpen(true),
  });

  return (
    <div className="min-h-full flex flex-col">
      <header className="px-6 pt-5 pb-3 border-b border-border bg-canvas">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="font-heading text-[18px] font-semibold">Cases</h1>
            <p className="text-[12px] text-foreground-muted mt-0.5">
              {isLoading ? 'Loading…' : `${filtered.length} ${filtered.length === 1 ? 'case' : 'cases'}`}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <div className="relative">
              <Search className="absolute left-2 top-1/2 -translate-y-1/2 size-3.5 text-foreground-subtle" />
              <Input
                ref={searchRef}
                placeholder="Search cases…    /"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="pl-7 w-64"
              />
            </div>
            <Button variant="primary" onClick={() => setNewOpen(true)}>
              <Plus className="size-3.5" /> New case
            </Button>
          </div>
        </div>
        <div className="mt-4">
          <FilterBar />
        </div>
      </header>

      <div className="bg-canvas flex-1">
        {isLoading ? (
          <div className="grid place-items-center py-20">
            <Spinner className="size-6" />
          </div>
        ) : isError ? (
          <div className="px-6 py-12 text-center text-[var(--destructive)]">
            Failed to load cases. Try again.
          </div>
        ) : (
          <CasesTable
            rows={filtered}
            caseTypeIds={effectiveTypes}
            selectedId={caseId ?? null}
            onOpenCase={(id) => navigate(`/cases/${id}`)}
            selectedIds={selectedIds}
            onToggleSelect={onToggleSelect}
            onToggleSelectAll={onToggleSelectAll}
            focusedRowId={focusedRowId}
            onRowFocus={setFocusedRowId}
            editing={editing}
            onStartEdit={startEdit}
            onCancelEdit={cancelEdit}
            onCommitEdit={commitEdit}
          />
        )}
      </div>

      {selectedIds.size > 0 && (
        <BulkActionBar
          count={selectedIds.size}
          onClear={() => setSelectedIds(new Set())}
          onAssign={bulkAssign}
          onSetStatus={bulkSetStatus}
          commonStatuses={commonStatuses}
        />
      )}

      <NewCaseDialog open={newOpen} onOpenChange={setNewOpen} />

      <RecordDrawer
        open={Boolean(caseId)}
        onClose={() => navigate('/cases')}
        title={drawerDto ? `${drawerDto.caseType.displayName} / ${drawerDto.id.slice(0, 8)}` : 'Case'}
      >
        {drawerLoading || !drawerDto ? (
          drawerError ? (
            <div className="px-6 py-12 text-center text-[var(--destructive)]">Failed to load case.</div>
          ) : (
            <div className="grid place-items-center py-20">
              <Spinner className="size-6" />
            </div>
          )
        ) : (
          <CaseDetailBody dto={drawerDto} compact />
        )}
      </RecordDrawer>

      <ShortcutsOverlay open={shortcutsOpen} onOpenChange={setShortcutsOpen} />
    </div>
  );
}

function applyFieldOptimistic(
  queryClient: ReturnType<typeof useQueryClient>,
  id: string,
  fieldId: string,
  value: unknown,
): { rollback: () => void } {
  const detailKey = caseQueryKeys.detail(id);
  const prevDetail = queryClient.getQueryData<CaseDto>(detailKey);
  if (prevDetail) {
    queryClient.setQueryData<CaseDto>(detailKey, {
      ...prevDetail,
      data: { ...prevDetail.data, [fieldId]: value },
    });
  }
  const snapshots: Array<{ key: readonly unknown[]; data: CaseSummary[] }> = [];
  const lists = queryClient.getQueriesData<CaseSummary[]>({ queryKey: caseQueryKeys.lists() });
  for (const [key, data] of lists) {
    if (!data) continue;
    const idx = data.findIndex((s) => s.id === id);
    if (idx === -1) continue;
    snapshots.push({ key, data });
    const next = [...data];
    next[idx] = { ...data[idx]!, fields: { ...data[idx]!.fields, [fieldId]: value } };
    queryClient.setQueryData<CaseSummary[]>(key, next);
  }
  return {
    rollback: () => {
      if (prevDetail) queryClient.setQueryData(detailKey, prevDetail);
      for (const { key, data } of snapshots) queryClient.setQueryData(key, data);
    },
  };
}

function applyAssigneeOptimistic(
  queryClient: ReturnType<typeof useQueryClient>,
  id: string,
  assignee: string | null,
): void {
  const detailKey = caseQueryKeys.detail(id);
  const prevDetail = queryClient.getQueryData<CaseDto>(detailKey);
  if (prevDetail) {
    queryClient.setQueryData<CaseDto>(detailKey, { ...prevDetail, assignee });
  }
  const lists = queryClient.getQueriesData<CaseSummary[]>({ queryKey: caseQueryKeys.lists() });
  for (const [key, data] of lists) {
    if (!data) continue;
    const idx = data.findIndex((s) => s.id === id);
    if (idx === -1) continue;
    const next = [...data];
    next[idx] = { ...data[idx]!, assignee };
    queryClient.setQueryData<CaseSummary[]>(key, next);
  }
}
