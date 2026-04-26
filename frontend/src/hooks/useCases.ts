import { useQueries } from '@tanstack/react-query';
import { useMemo } from 'react';

import { listCases } from '@/api/cases';
import { caseQueryKeys, type CaseListQuery } from '@/lib/queryKeys';
import { toCaseRow, type CaseRow } from '@/types/case';

const STALE_TIME_MS = 30_000;

export interface UseCasesArgs {
  caseTypeIds: string[];
  status?: string;
  size?: number;
  sort?: string[];
}

export interface UseCasesResult {
  data: CaseRow[];
  isLoading: boolean;
  isError: boolean;
  errors: Error[];
}

/**
 * Story 2.5 Multi-case-type fetch — see Dev Notes §Multi-case-type fetch.
 *
 * The backend list endpoint requires exactly one `caseType` per call. When the user selects
 * N case types in the filter bar this hook issues N parallel `useQuery`s and merges the rows
 * client-side. Phase 1 collapses to a single backend call once `GET /api/cases` accepts an
 * array of case-type ids.
 */
export function useCases({ caseTypeIds, status, size = 100, sort }: UseCasesArgs): UseCasesResult {
  const queries = useQueries({
    queries: caseTypeIds.map((caseType) => {
      const query: CaseListQuery = { caseType, status, size, sort, page: 0 };
      return {
        queryKey: caseQueryKeys.list(query),
        queryFn: () => listCases(query),
        staleTime: STALE_TIME_MS,
        refetchOnWindowFocus: true,
      };
    }),
  });

  return useMemo(() => {
    const rows: CaseRow[] = [];
    const errors: Error[] = [];
    let isLoading = false;
    let isError = false;
    for (const q of queries) {
      if (q.isLoading) isLoading = true;
      if (q.isError) {
        isError = true;
        if (q.error instanceof Error) errors.push(q.error);
      }
      if (q.data) {
        for (const summary of q.data) rows.push(toCaseRow(summary));
      }
    }
    return { data: rows, isLoading, isError, errors };
  }, [queries]);
}
