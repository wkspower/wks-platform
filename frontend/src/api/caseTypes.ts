import type { CaseTypeSummary, CaseTypeView } from '@/types/caseType';

import { apiFetch, apiFetchText } from './client';

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

/**
 * `GET /api/admin/case-types/{id}/source` — raw YAML of the active version. ADMIN-only.
 * Powers the in-UI case-type editor: load → edit → deploy → reload.
 */
export async function getCaseTypeSource(id: string): Promise<string> {
  return apiFetchText(`/api/admin/case-types/${encodeURIComponent(id)}/source`);
}

/**
 * `GET /api/admin/case-types/meta-schema` — Draft 2020-12 JSON Schema describing the case-type
 * YAML grammar (what fields may appear in the file). Distinct from the per-case data schema in
 * `JsonSchemaGenerator`. ADMIN-only. Powers Monaco YAML IntelliSense in the editor.
 */
export async function getCaseTypeMetaSchema(): Promise<unknown> {
  const text = await apiFetchText('/api/admin/case-types/meta-schema');
  return JSON.parse(text);
}

export interface DeployResponse {
  caseTypeId: string;
  version: number;
  deploymentId: string | null;
  processDefinitionId: string | null;
  schemaPath: string;
}

/**
 * `POST /api/admin/deploy` — multipart deploy of a case-type YAML (optionally with a BPMN).
 * YAML-only deploys skip the engine and register a zero-process case type.
 */
export async function deployCaseType(
  yaml: string,
  options: { bumpVersion?: boolean; bpmn?: Blob | null; bpmnFilename?: string } = {},
): Promise<DeployResponse> {
  const form = new FormData();
  form.append('caseType', new Blob([yaml], { type: 'application/x-yaml' }), 'case-type.yaml');
  if (options.bpmn) {
    form.append('bpmn', options.bpmn, options.bpmnFilename ?? 'workflow.bpmn');
  }
  const qs = options.bumpVersion ? '?bumpVersion=true' : '';
  const result = await apiFetch<DeployResponse>(`/api/admin/deploy${qs}`, {
    method: 'POST',
    body: form,
  });
  return result.data;
}
