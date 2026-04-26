import { useQuery, useQueries } from '@tanstack/react-query';

import { getCaseType, listCaseTypes } from '@/api/caseTypes';
import { caseTypeQueryKeys } from '@/lib/queryKeys';

const STALE_TIME_MS = 30_000;

export function useCaseTypes() {
  return useQuery({
    queryKey: caseTypeQueryKeys.list(),
    queryFn: () => listCaseTypes(),
    staleTime: STALE_TIME_MS,
    refetchOnWindowFocus: true,
  });
}

export function useCaseType(id: string | undefined) {
  return useQuery({
    queryKey: id ? caseTypeQueryKeys.detail(id) : ['caseTypes', 'detail', '__disabled__'],
    queryFn: () => getCaseType(id as string),
    enabled: id !== undefined && id !== '',
    staleTime: STALE_TIME_MS,
    refetchOnWindowFocus: true,
  });
}

/**
 * Multi-fetch helper for the case list. The backend `GET /api/cases` requires exactly one
 * `caseType` query parameter (Story 2.3 AC7); when multiple case types are selected on the
 * filter bar the frontend fans out one query per id and merges results client-side.
 */
export function useCaseTypeViews(ids: string[]) {
  return useQueries({
    queries: ids.map((id) => ({
      queryKey: caseTypeQueryKeys.detail(id),
      queryFn: () => getCaseType(id),
      staleTime: STALE_TIME_MS,
      refetchOnWindowFocus: true,
    })),
  });
}
