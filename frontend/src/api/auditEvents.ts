import type { AuditEventList } from '@/types/auditEvent';

import { apiFetch } from './client';

/**
 * Story 9-2 AC1 — fetch the chronological audit feed for a case. Newest-first. Server clamps
 * {@code limit} to {@code [1, 200]} (default 50); {@code truncated} is {@code true} when more
 * rows exist than the limit returned.
 */
export async function getCaseAuditEvents(caseId: string, limit?: number): Promise<AuditEventList> {
  const qs = limit !== undefined ? `?limit=${encodeURIComponent(String(limit))}` : '';
  const result = await apiFetch<AuditEventList>(`/api/cases/${encodeURIComponent(caseId)}/audit-events${qs}`);
  return result.data;
}
