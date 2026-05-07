import { apiFetch } from './client';

/** Metadata for a document attached to a case (mirrors {@code CaseDocumentDto}). */
export interface CaseDocument {
  id: string;
  caseId: string;
  fileName: string;
  contentType: string;
  sizeBytes: number;
  uploadedBy: string;
  uploadedAt: string;
}

/** Preview endpoint response (mirrors {@code PreviewResponse}). */
export interface PreviewResponse {
  previewable: boolean;
  url: string;
}

/**
 * {@code GET /api/cases/{caseId}/documents} — returns list ordered by uploadedAt DESC.
 */
export async function listDocuments(caseId: string): Promise<CaseDocument[]> {
  const result = await apiFetch<CaseDocument[]>(
    `/api/cases/${encodeURIComponent(caseId)}/documents`,
  );
  return result.data;
}

/**
 * {@code POST /api/cases/{caseId}/documents} — multipart upload.
 * The {@code apiFetch} client skips Content-Type injection for FormData so the browser sets
 * the boundary automatically.
 */
export async function uploadDocument(caseId: string, file: File): Promise<CaseDocument> {
  const form = new FormData();
  form.append('file', file);
  const result = await apiFetch<CaseDocument>(
    `/api/cases/${encodeURIComponent(caseId)}/documents`,
    {
      method: 'POST',
      body: form,
    },
  );
  return result.data;
}

/**
 * {@code GET /api/documents/{documentId}/preview} — returns a preview URL or download URL.
 */
export async function getPreview(documentId: string): Promise<PreviewResponse> {
  const result = await apiFetch<PreviewResponse>(
    `/api/documents/${encodeURIComponent(documentId)}/preview`,
  );
  return result.data;
}

/** Returns the download URL for a document (no API call needed — just the path). */
export function downloadUrl(documentId: string): string {
  return `/api/documents/${encodeURIComponent(documentId)}/download`;
}
