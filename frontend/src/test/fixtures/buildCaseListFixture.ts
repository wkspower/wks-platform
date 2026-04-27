import type { CaseSummary } from '@/types/case';
import type { CaseTypeSummary, CaseTypeView } from '@/types/caseType';

/**
 * Story 2.5 AC8 perf-guardrail support — builds N synthetic `CaseSummary` rows for the
 * 1000-row render benchmark. Stable seed + deterministic field shape so snapshots stay
 * reproducible across runs.
 */
export function buildCaseListFixture(
  count: number,
  caseTypeId = 'loan-application',
): CaseSummary[] {
  const result: CaseSummary[] = [];
  const baseTime = Date.parse('2026-04-01T00:00:00Z');
  const statuses = ['open', 'in-progress', 'review', 'resolved'];
  for (let i = 0; i < count; i++) {
    const ts = new Date(baseTime + i * 60_000).toISOString();
    result.push({
      id: `00000000-0000-0000-0000-${i.toString().padStart(12, '0')}`,
      caseTypeId,
      status: statuses[i % statuses.length] as string,
      assignee: null,
      createdAt: ts,
      updatedAt: ts,
      fields: {
        applicant_name: `Applicant ${i}`,
        amount: 1000 + i,
      },
    });
  }
  return result;
}

export function loanApplicationCaseTypeView(): CaseTypeView {
  return {
    id: 'loan-application',
    displayName: 'Loan Application',
    version: 1,
    fields: [
      {
        id: 'applicant_name',
        displayName: 'Applicant',
        type: 'text',
        required: true,
        requiredOnCreate: true,
        order: 0,
        options: [],
        maxLength: 100,
      },
      {
        id: 'amount',
        displayName: 'Amount',
        type: 'number',
        required: false,
        requiredOnCreate: true,
        order: 1,
        options: [],
        min: 1,
        max: 1_000_000,
      },
    ],
    statuses: [
      { id: 'open', displayName: 'Open', color: 'blue' },
      { id: 'in-progress', displayName: 'In Progress', color: 'amber' },
      { id: 'review', displayName: 'Review', color: 'violet' },
      { id: 'resolved', displayName: 'Resolved', color: 'emerald' },
    ],
    listColumns: ['applicant_name', 'amount'],
  };
}

export function caseTypeSummaryFixture(over: Partial<CaseTypeSummary> = {}): CaseTypeSummary {
  return {
    id: 'loan-application',
    displayName: 'Loan Application',
    version: 1,
    statusCount: 4,
    fieldCount: 2,
    permissions: ['view', 'create'],
    ...over,
  };
}
