/**
 * Phase 0 priority enum. The case-type YAML schema does NOT yet model priority — UX spec
 * §Filters calls for it on the case list filter bar so we hardcode the four levels here.
 *
 * Phase 1 will fold this into the YAML schema (a `priorities[]` block on `CaseTypeConfig`)
 * and the backend will echo per-case-type priorities; until then the wire form stores priority
 * as a free-form `string` field on the case `data` map and the frontend filter narrows by it.
 */
export const PRIORITIES = ['low', 'medium', 'high', 'urgent'] as const;

export type Priority = (typeof PRIORITIES)[number];
