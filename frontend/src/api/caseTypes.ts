import type { CaseTypeSummary, CaseTypeView } from '@/types/caseType';

import { apiFetch } from './client';

/** `GET /api/case-types` — list of case types the caller has the `view` verb on. */
export async function listCaseTypes(): Promise<CaseTypeSummary[]> {
  const result = await apiFetch<CaseTypeSummary[]>('/api/case-types');
  return result.data;
}

/** `GET /api/case-types/{id}` — full view DTO including fields, statuses, and listColumns. */
export async function getCaseType(id: string): Promise<CaseTypeView> {
  const result = await apiFetch<CaseTypeView>(`/api/case-types/${encodeURIComponent(id)}`);
  return result.data;
}
