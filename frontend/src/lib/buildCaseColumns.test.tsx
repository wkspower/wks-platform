import type { CellContext } from '@tanstack/react-table';
import { render } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { loanApplicationCaseTypeView } from '@/test/fixtures/buildCaseListFixture';
import { toCaseRow } from '@/types/case';
import type { CaseRow } from '@/types/case';
import type { CaseTypeView, FieldDefinition } from '@/types/caseType';

import { buildCaseColumns, urgencyDefaultSort } from './buildCaseColumns';

function renderCell(
  columnId: string,
  caseType: Pick<CaseTypeView, 'fields' | 'statuses' | 'listColumns'>,
  row: CaseRow,
) {
  const cols = buildCaseColumns(caseType);
  const col = cols.find((c) => c.id === columnId);
  if (!col || !col.cell) throw new Error(`column ${columnId} or its cell missing`);
  // Minimal stub of TanStack's cell context — exercises just the parts the renderers read.
  const accessor = (col as unknown as { accessorFn?: (r: CaseRow) => unknown }).accessorFn;
  const ctx = {
    row: { original: row },
    getValue: () => (accessor ? accessor(row) : undefined),
  } as unknown as CellContext<CaseRow, unknown>;
  const node = typeof col.cell === 'function' ? col.cell(ctx) : col.cell;
  return render(<>{node}</>);
}

describe('buildCaseColumns', () => {
  const caseType = loanApplicationCaseTypeView();

  it('emits id, status, every listColumn field (in declared order), updatedAt', () => {
    const cols = buildCaseColumns(caseType);
    expect(cols.map((c) => c.id)).toEqual([
      'id',
      'status',
      'field:applicant_name',
      'field:amount',
      'updatedAt',
    ]);
  });

  it('renders null/undefined field values as em-dash', () => {
    const row = toCaseRow({
      id: '00000000-0000-0000-0000-000000000001',
      caseTypeId: caseType.id,
      status: 'open',
      assignee: null,
      createdAt: '2026-04-01T00:00:00Z',
      updatedAt: '2026-04-01T00:00:00Z',
      fields: { applicant_name: null, amount: undefined },
    });
    const r1 = renderCell('field:applicant_name', caseType, row);
    expect(r1.container.textContent).toContain('—');
    r1.unmount();
    const r2 = renderCell('field:amount', caseType, row);
    expect(r2.container.textContent).toContain('—');
  });

  it('drops unknown listColumn ids and warns', () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    const broken: CaseTypeView = { ...caseType, listColumns: ['applicant_name', 'mystery'] };
    const cols = buildCaseColumns(broken);
    expect(cols.map((c) => c.id)).not.toContain('field:mystery');
    expect(warn).toHaveBeenCalled();
    warn.mockRestore();
  });

  it('marks textarea + file columns as not sortable', () => {
    const ct: CaseTypeView = {
      ...caseType,
      fields: [
        ...caseType.fields,
        {
          id: 'notes',
          displayName: 'Notes',
          type: 'textarea',
          required: false,
          order: 2,
          options: [],
          slots: null,
        } satisfies FieldDefinition,
        {
          id: 'attach',
          displayName: 'Attachments',
          type: 'file',
          required: false,
          order: 3,
          options: [],
          slots: null,
        } satisfies FieldDefinition,
      ],
      listColumns: [...caseType.listColumns, 'notes', 'attach'],
    };
    const cols = buildCaseColumns(ct);
    expect(cols.find((c) => c.id === 'field:notes')?.enableSorting).toBe(false);
    expect(cols.find((c) => c.id === 'field:attach')?.enableSorting).toBe(false);
  });
});

describe('urgencyDefaultSort', () => {
  it('puts SLA-breaching rows first, then by updatedAt desc', () => {
    const a = toCaseRow({
      id: 'a',
      caseTypeId: 'x',
      status: 'open',
      assignee: null,
      createdAt: '2026-04-01T00:00:00Z',
      updatedAt: '2026-04-02T00:00:00Z',
      fields: {},
    });
    const b = toCaseRow({
      id: 'b',
      caseTypeId: 'x',
      status: 'open',
      assignee: null,
      createdAt: '2026-04-01T00:00:00Z',
      updatedAt: '2026-04-03T00:00:00Z',
      fields: {},
    });
    expect(urgencyDefaultSort(a, b)).toBeGreaterThan(0); // b before a (newer)

    const aBreached = { ...a, slaBreached: true };
    expect(urgencyDefaultSort(aBreached, b)).toBeLessThan(0); // a-breached before b
  });
});
