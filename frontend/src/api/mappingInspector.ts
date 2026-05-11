import { apiFetch } from './client';

/**
 * Story 4.6 AC1 wire shape — projection of a CaseType's active mapping for the
 * admin Mapping Inspector page.
 */
export interface MappingInspectorDto {
  caseTypeId: string;
  version: string;
  attachments: AttachmentView[];
  emptyMapping: boolean;
}

export interface AttachmentView {
  name: string;
  bpmnSource: string;
  elements: ElementMappingRow[];
}

export interface ElementMappingRow {
  bpmnElement: string;
  wksEffect: string;
  target: string | null;
  rule: string | null;
}

/**
 * Story 4.6 AC2 wire shape — ring buffer snapshot of the most recent routing
 * decisions for a CaseType (newest-first, capped at 50).
 */
export interface RecentSignalsDto {
  caseTypeId: string;
  signals: RecentSignalView[];
}

export interface RecentSignalView {
  timestamp: string;
  kind: string;
  source: string;
  decision: 'matched-rule' | 'unmapped' | 'case-not-found' | 'version-not-registered';
  matchedRule: string | null;
  effect: string | null;
  caseId: string | null;
  errorCode: string | null;
}

export async function fetchMappingInspector(
  caseTypeId: string,
  signal?: AbortSignal,
): Promise<MappingInspectorDto> {
  const result = await apiFetch<MappingInspectorDto>(
    `/api/admin/case-types/${encodeURIComponent(caseTypeId)}/mapping-inspector`,
    { signal },
  );
  return result.data;
}

export async function fetchRecentSignals(
  caseTypeId: string,
  signal?: AbortSignal,
): Promise<RecentSignalsDto> {
  const result = await apiFetch<RecentSignalsDto>(
    `/api/admin/case-types/${encodeURIComponent(caseTypeId)}/recent-signals`,
    { signal },
  );
  return result.data;
}
