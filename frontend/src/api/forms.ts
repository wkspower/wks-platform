/**
 * Story 5.2 — Form submission API. Mirrors the backend
 * `POST /api/cases/{caseId}/forms/{formId}/submit` endpoint.
 */
import type { CaseDto } from '@/types/case';

import { apiFetch } from './client';

/**
 * Submit form data for an existing case.
 *
 * @param caseId - the case to update
 * @param formId - the form definition id declared on the case type
 * @param data   - submitted field values keyed by field id
 * @returns the updated {@link CaseDto} with embedded case-type view
 * @throws {@link ApiError} on validation failure (WKS-FORM-002 at HTTP 422) or other errors
 */
export async function submitForm(
  caseId: string,
  formId: string,
  data: Record<string, unknown>,
): Promise<CaseDto> {
  const result = await apiFetch<CaseDto>(
    `/api/cases/${encodeURIComponent(caseId)}/forms/${encodeURIComponent(formId)}/submit`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    },
  );
  return result.data;
}
