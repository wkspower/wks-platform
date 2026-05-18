import { ChevronRight } from 'lucide-react';
import { useEffect, useMemo, useRef } from 'react';

import { InlineFieldEditor, InlineSelectEditor, InlineTextEditor, isInlineEditableField } from '@/components/cases/InlineEditCell';
import { Avatar } from '@/components/ui/Avatar';
import { Checkbox } from '@/components/ui/Checkbox';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { useCaseTypeViews } from '@/hooks/useCaseTypes';
import { cn } from '@/lib/cn';
import { formatRelativeTime } from '@/lib/formatDate';
import { renderFieldValue } from '@/lib/renderFieldValue';
import { useUiStore } from '@/stores/uiStore';
import type { CaseRow } from '@/types/case';
import type { CaseTypeView, FieldDefinition } from '@/types/caseType';

/** Identifier for the cell currently being inline-edited. */
export interface InlineEditTarget {
  rowId: string;
  /** Special field ids: `__status__` and `__assignee__`; otherwise a dynamic FieldDefinition.id. */
  fieldId: string;
}

export interface CasesTableProps {
  rows: CaseRow[];
  caseTypeIds: string[];
  selectedId?: string | null;
  onOpenCase?: (id: string) => void;

  // Selection (bulk)
  selectedIds: Set<string>;
  onToggleSelect: (id: string, opts?: { range?: boolean }) => void;
  onToggleSelectAll: () => void;

  // Keyboard focus
  focusedRowId: string | null;
  onRowFocus: (id: string) => void;

  // Inline edit
  editing: InlineEditTarget | null;
  onStartEdit: (target: InlineEditTarget) => void;
  onCancelEdit: () => void;
  onCommitEdit: (row: CaseRow, view: CaseTypeView | undefined, target: InlineEditTarget, next: unknown) => void;
}

export const STATUS_FIELD_ID = '__status__';
export const ASSIGNEE_FIELD_ID = '__assignee__';

export function CasesTable({
  rows,
  caseTypeIds,
  selectedId,
  onOpenCase,
  selectedIds,
  onToggleSelect,
  onToggleSelectAll,
  focusedRowId,
  onRowFocus,
  editing,
  onStartEdit,
  onCancelEdit,
  onCommitEdit,
}: CasesTableProps) {
  const recent = useUiStore((s) => s.recentlyCreatedCaseIds);
  const views = useCaseTypeViews(caseTypeIds);

  const viewMap = useMemo(() => {
    const m = new Map<string, CaseTypeView>();
    for (const r of views) if (r.data) m.set(r.data.id, r.data);
    return m;
  }, [views]);

  const isSingle = caseTypeIds.length === 1;
  const dynamicCols: FieldDefinition[] = useMemo(() => {
    if (!isSingle) return [];
    const v = viewMap.get(caseTypeIds[0]!);
    if (!v) return [];
    return [...v.fields].sort((a, b) => a.order - b.order).slice(0, 4);
  }, [isSingle, caseTypeIds, viewMap]);

  const allSelected = rows.length > 0 && rows.every((r) => selectedIds.has(r.id));
  const someSelected = !allSelected && rows.some((r) => selectedIds.has(r.id));

  const focusedRef = useRef<HTMLTableRowElement | null>(null);
  useEffect(() => {
    focusedRef.current?.scrollIntoView({ block: 'nearest' });
  }, [focusedRowId]);

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-[13px] border-separate border-spacing-0">
        <thead>
          <tr className="text-foreground-subtle text-[11px] uppercase tracking-wider">
            <th className="w-8 px-3 py-2 border-b border-border bg-background sticky top-0 z-10">
              <Checkbox
                checked={allSelected ? true : someSelected ? 'indeterminate' : false}
                onCheckedChange={() => onToggleSelectAll()}
                aria-label={allSelected ? 'Deselect all' : 'Select all visible'}
              />
            </th>
            <th className="text-left font-medium px-3 py-2 border-b border-border bg-background sticky top-0 z-10">ID</th>
            {!isSingle && (
              <th className="text-left font-medium px-3 py-2 border-b border-border bg-background sticky top-0 z-10">
                Case type
              </th>
            )}
            <th className="text-left font-medium px-3 py-2 border-b border-border bg-background sticky top-0 z-10">
              Status
            </th>
            {dynamicCols.map((c) => (
              <th
                key={c.id}
                className="text-left font-medium px-3 py-2 border-b border-border bg-background sticky top-0 z-10"
              >
                {c.displayName}
              </th>
            ))}
            <th className="text-left font-medium px-3 py-2 border-b border-border bg-background sticky top-0 z-10">
              Assignee
            </th>
            <th className="text-left font-medium px-3 py-2 border-b border-border bg-background sticky top-0 z-10">
              Updated
            </th>
            <th className="w-8 border-b border-border bg-background sticky top-0 z-10" />
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 && (
            <tr>
              <td colSpan={4 + dynamicCols.length + 3} className="px-3 py-12 text-center text-foreground-muted">
                No cases match the current filters.
              </td>
            </tr>
          )}
          {rows.map((row) => {
            const v = viewMap.get(row.caseTypeId);
            const status = v?.statuses.find((s) => s.id === row.status);
            const isFocused = focusedRowId === row.id;
            const isChecked = selectedIds.has(row.id);
            return (
              <tr
                key={row.id}
                ref={isFocused ? focusedRef : undefined}
                onClick={() => {
                  onRowFocus(row.id);
                  onOpenCase?.(row.id);
                }}
                aria-selected={selectedId === row.id || isFocused || undefined}
                className={cn(
                  'group cursor-pointer transition-colors',
                  'hover:bg-surface-hover',
                  selectedId === row.id && 'bg-surface-hover',
                  isChecked && 'bg-[var(--primary-soft)]/40',
                  recent.has(row.id) && 'bg-[var(--primary-soft)]',
                  isFocused && 'outline outline-2 -outline-offset-2 outline-[var(--primary)]',
                )}
              >
                <td
                  className="px-3 py-2 border-b border-divider"
                  onClick={(e) => {
                    e.stopPropagation();
                    onRowFocus(row.id);
                    onToggleSelect(row.id, { range: e.shiftKey });
                  }}
                >
                  <Checkbox
                    checked={isChecked}
                    onCheckedChange={() => onToggleSelect(row.id)}
                    aria-label={isChecked ? 'Deselect row' : 'Select row'}
                  />
                </td>
                <td className="px-3 py-2 border-b border-divider font-mono text-[12px] text-foreground-muted">
                  {row.id.slice(0, 8)}
                </td>
                {!isSingle && (
                  <td className="px-3 py-2 border-b border-divider">{v?.displayName ?? row.caseTypeId}</td>
                )}
                <td
                  className="px-3 py-2 border-b border-divider"
                  onDoubleClick={(e) => {
                    e.stopPropagation();
                    onStartEdit({ rowId: row.id, fieldId: STATUS_FIELD_ID });
                  }}
                >
                  {editing && editing.rowId === row.id && editing.fieldId === STATUS_FIELD_ID && v ? (
                    <InlineSelectEditor
                      initialValue={row.status}
                      options={v.statuses.map((s) => ({ value: s.id, label: s.displayName }))}
                      onCommit={(val) => onCommitEdit(row, v, editing, val)}
                      onCancel={onCancelEdit}
                    />
                  ) : (
                    <StatusBadge
                      label={status?.displayName ?? row.status}
                      color={status?.color}
                    />
                  )}
                </td>
                {dynamicCols.map((c) => {
                  const isEditing = editing && editing.rowId === row.id && editing.fieldId === c.id;
                  const editable = isInlineEditableField(c);
                  return (
                    <td
                      key={c.id}
                      className="px-3 py-2 border-b border-divider"
                      onDoubleClick={(e) => {
                        if (!editable) return;
                        e.stopPropagation();
                        onStartEdit({ rowId: row.id, fieldId: c.id });
                      }}
                    >
                      {isEditing ? (
                        <InlineFieldEditor
                          field={c}
                          initialValue={row.fields[c.id]}
                          onCommit={(val) => onCommitEdit(row, v, editing, val)}
                          onCancel={onCancelEdit}
                        />
                      ) : (
                        <span className="truncate inline-block max-w-[260px] align-middle">
                          {renderField(row, c, v)}
                        </span>
                      )}
                    </td>
                  );
                })}
                <td
                  className="px-3 py-2 border-b border-divider"
                  onDoubleClick={(e) => {
                    e.stopPropagation();
                    onStartEdit({ rowId: row.id, fieldId: ASSIGNEE_FIELD_ID });
                  }}
                >
                  {editing && editing.rowId === row.id && editing.fieldId === ASSIGNEE_FIELD_ID ? (
                    <InlineTextEditor
                      initialValue={row.assignee ?? ''}
                      placeholder="username"
                      onCommit={(val) => onCommitEdit(row, v, editing, val)}
                      onCancel={onCancelEdit}
                    />
                  ) : row.assignee ? (
                    <span className="inline-flex items-center gap-1.5">
                      <Avatar name={row.assignee} size="sm" />
                      <span className="text-foreground-muted">{row.assignee}</span>
                    </span>
                  ) : (
                    <span className="text-foreground-subtle">—</span>
                  )}
                </td>
                <td className="px-3 py-2 border-b border-divider text-foreground-muted">
                  {formatRelativeTime(row.updatedAt)}
                </td>
                <td className="px-3 py-2 border-b border-divider text-foreground-subtle">
                  <ChevronRight className="size-3.5 opacity-0 group-hover:opacity-100 transition" />
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

function renderField(row: CaseRow, c: FieldDefinition, _view: CaseTypeView | undefined) {
  const raw = row.fields[c.id];
  try {
    return renderFieldValue(c, raw);
  } catch {
    return <span className="text-foreground-subtle">—</span>;
  }
}
