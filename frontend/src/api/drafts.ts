import { apiFetch, ApiError } from './client';

/**
 * Story 5.4 — wire DTO returned by GET / PUT /api/cases/{caseId}/forms/{formId}/draft.
 */
export interface FormDraftDto {
  id: string;
  caseId: string;
  formId: string;
  payload: Record<string, unknown>;
  scrollY: number;
  sectionExpanded: Record<string, boolean> | null;
  caseTypeVersionAtSave: number;
  /** ISO-8601 timestamp serialized from the backend Instant. */
  updatedAt: string;
}

export interface SaveDraftRequest {
  payload: Record<string, unknown>;
  scrollY: number;
  sectionExpanded: Record<string, boolean> | null;
  caseTypeVersionAtSave: number;
}

/**
 * GET the current user's draft for {@code (caseId, formId)}. Returns {@code null} when no draft
 * exists (the backend returns 404 in that case — the hook treats it as a non-error empty state).
 */
export async function getFormDraft(caseId: string, formId: string): Promise<FormDraftDto | null> {
  try {
    const res = await apiFetch<FormDraftDto>(`/api/cases/${caseId}/forms/${formId}/draft`, {
      method: 'GET',
    });
    return res.data;
  } catch (err) {
    if (err instanceof ApiError && err.status === 404) {
      return null;
    }
    throw err;
  }
}

/** Upsert the draft — returns the persisted draft DTO. */
export async function saveFormDraft(
  caseId: string,
  formId: string,
  body: SaveDraftRequest,
): Promise<FormDraftDto> {
  const res = await apiFetch<FormDraftDto>(`/api/cases/${caseId}/forms/${formId}/draft`, {
    method: 'PUT',
    body: JSON.stringify(body),
  });
  return res.data;
}

/** Delete the draft. Idempotent — succeeds silently when no row exists. */
export async function deleteFormDraft(caseId: string, formId: string): Promise<void> {
  await apiFetch<void>(`/api/cases/${caseId}/forms/${formId}/draft`, { method: 'DELETE' });
}
