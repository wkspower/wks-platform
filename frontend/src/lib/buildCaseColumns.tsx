import type { ColumnDef } from '@tanstack/react-table';

import { StatusBadge } from '@/components/workspace/StatusBadge';
import { t } from '@/i18n';
import { formatDate } from '@/lib/formatDate';
import { formatNumber } from '@/lib/formatNumber';
import type { CaseRow } from '@/types/case';
import type { CaseTypeView, FieldDefinition } from '@/types/caseType';

const EM_DASH = '—';

function isEmpty(value: unknown): boolean {
  return value === null || value === undefined || value === '';
}

function renderField(field: FieldDefinition, value: unknown): string {
  if (isEmpty(value)) return EM_DASH;
  switch (field.type) {
    case 'number':
      return typeof value === 'number' ? formatNumber(value) : EM_DASH;
    case 'date':
      return typeof value === 'string' ? formatDate(value) : EM_DASH;
    case 'checkbox':
      return value === true || value === 'true'
        ? t('cases.field.checkbox.true')
        : t('cases.field.checkbox.false');
    case 'file':
      return t('cases.field.file.placeholder');
    case 'text':
    case 'textarea':
    case 'select':
    default:
      return String(value);
  }
}

const SORTABLE_FIELD_TYPES = new Set<FieldDefinition['type']>([
  'text',
  'number',
  'date',
  'select',
  'checkbox',
]);

/**
 * AC2 — config-driven column generation. Order:
 *
 *   1. id (system column)        — last 8 hex chars + tooltip on full UUID
 *   2. status (system column)    — StatusBadge cell
 *   3. one column per fieldId in `caseType.listColumns` (in declared order)
 *   4. updatedAt (system column) — default-sort target
 *
 * AC3 sortability: id, status, updatedAt are sortable; YAML field columns sortable when their
 * `type ∈ {text, number, date, select, checkbox}`. textarea + file columns are NOT sortable.
 *
 * Defensive: when a `listColumns` entry references a `fieldId` not present in
 * `caseType.fields[]` the entry is dropped with a `console.warn` (server already rejects this
 * at deploy time per WKS-CFG-006, but a stale TanStack Query cache could surface it).
 */
export function buildCaseColumns(
  caseType: Pick<CaseTypeView, 'fields' | 'statuses' | 'listColumns'>,
): ColumnDef<CaseRow>[] {
  const fieldsById = new Map(caseType.fields.map((f) => [f.id, f]));

  const idColumn: ColumnDef<CaseRow> = {
    id: 'id',
    header: () => t('cases.column.id'),
    accessorKey: 'id',
    cell: ({ row }) => {
      const id = row.original.id;
      const short = id.slice(-8);
      return (
        <span title={id} className="font-mono text-xs">
          {short}
        </span>
      );
    },
    enableSorting: true,
  };

  const statusColumn: ColumnDef<CaseRow> = {
    id: 'status',
    header: () => t('cases.column.status'),
    accessorKey: 'status',
    cell: ({ row }) => <StatusBadge status={row.original.status} caseType={caseType} />,
    enableSorting: true,
  };

  const fieldColumns: ColumnDef<CaseRow>[] = [];
  for (const fieldId of caseType.listColumns) {
    const field = fieldsById.get(fieldId);
    if (!field) {
      // eslint-disable-next-line no-console
      console.warn(`buildCaseColumns: listColumn '${fieldId}' not in caseType.fields`);
      continue;
    }
    fieldColumns.push({
      id: `field:${field.id}`,
      header: field.displayName,
      accessorFn: (row) => row.fields[field.id],
      cell: ({ getValue }) => renderField(field, getValue()),
      enableSorting: SORTABLE_FIELD_TYPES.has(field.type),
      enableGlobalFilter: true,
    });
  }

  const updatedAtColumn: ColumnDef<CaseRow> = {
    id: 'updatedAt',
    header: () => t('cases.column.updatedAt'),
    accessorKey: 'updatedAt',
    cell: ({ row }) => formatDate(row.original.updatedAt),
    enableSorting: true,
  };

  return [idColumn, statusColumn, ...fieldColumns, updatedAtColumn];
}

/**
 * AC3 default-sort composite. Phase 0 ships with `slaBreached` always `false` so the SLA tier
 * is a no-op until SLA data arrives — Phase 1 wires `slaBreached` server-side and the
 * comparator picks it up without code change.
 */
export function urgencyDefaultSort(a: CaseRow, b: CaseRow): number {
  const slaCmp = (b.slaBreached ? 1 : 0) - (a.slaBreached ? 1 : 0);
  if (slaCmp !== 0) return slaCmp;
  return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime();
}
