/**
 * TanStack Query key factory for Story 2.5. SSE-driven invalidation lands in Story 4.3 and will
 * `queryClient.invalidateQueries({ queryKey: caseQueryKeys.lists() })` on `CaseStatusChanged`
 * events — keep keys structured so a partial prefix invalidation works.
 */

export interface CaseListQuery {
  caseType: string;
  status?: string;
  page?: number;
  size?: number;
  sort?: string[];
}

export const caseQueryKeys = {
  all: () => ['cases'] as const,
  lists: () => ['cases', 'list'] as const,
  list: (query: CaseListQuery) => ['cases', 'list', query] as const,
  detail: (id: string) => ['case', id] as const,
} as const;

export const caseTypeQueryKeys = {
  all: () => ['caseTypes'] as const,
  list: () => ['caseTypes', 'list'] as const,
  detail: (id: string) => ['caseTypes', 'detail', id] as const,
} as const;
