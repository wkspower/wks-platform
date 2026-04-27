import type { CaseListQuery } from '@/lib/queryKeys';
import type { CaseDto, CaseSummary } from '@/types/case';

import { apiFetch } from './client';

export async function getCase(id: string): Promise<CaseDto> {
  const result = await apiFetch<CaseDto>(`/api/cases/${encodeURIComponent(id)}`);
  return result.data;
}

/**
 * `GET /api/cases?caseType=…&status=…&page=…&size=…&sort=…` — Story 2.3 contract.
 *
 * Phase 0 client-side filtering (Story 2.5) issues a single fetch per selected case type with
 * `size=100` (the backend max) and renders client-side. Phase 1 swaps to server-side pagination.
 */
export async function listCases(query: CaseListQuery): Promise<CaseSummary[]> {
  const params = new URLSearchParams();
  params.set('caseType', query.caseType);
  if (query.status !== undefined) params.set('status', query.status);
  if (query.page !== undefined) params.set('page', String(query.page));
  if (query.size !== undefined) params.set('size', String(query.size));
  if (query.sort) {
    for (const token of query.sort) params.append('sort', token);
  }
  const result = await apiFetch<CaseSummary[]>(`/api/cases?${params.toString()}`);
  return result.data;
}

/** Story 2.7 — request body for `POST /api/cases`. */
export interface CreateCaseRequest {
  caseTypeId: string;
  data: Record<string, unknown>;
  assignee?: string | null;
}

/** Story 2.7 — `POST /api/cases` (Story 2.3 endpoint, first consumed from a UI surface here). */
export async function createCase(req: CreateCaseRequest): Promise<CaseDto> {
  const result = await apiFetch<CaseDto>('/api/cases', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  });
  return result.data;
}
