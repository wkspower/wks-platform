import {
  skipToken,
  useMutation,
  useQueries,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import { createCase, getCase, listCases, type CreateCaseRequest } from '@/api/cases';
import { caseQueryKeys, type CaseListQuery } from '@/lib/queryKeys';
import { toCaseRow, type CaseDto, type CaseRow } from '@/types/case';

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
  // `combine` runs inside react-query and returns a stable reference between renders when none
  // of the underlying query results changed. Without it, `useQueries` returns a fresh array each
  // render, which would cascade through downstream `useMemo`s (filtered rows, columns) and
  // re-render TanStack Table on every parent state change — defeating the AC8 perf budget.
  return useQueries({
    queries: caseTypeIds.map((caseType) => {
      const query: CaseListQuery = { caseType, status, size, sort, page: 0 };
      return {
        queryKey: caseQueryKeys.list(query),
        queryFn: () => listCases(query),
        staleTime: STALE_TIME_MS,
        refetchOnWindowFocus: true,
      };
    }),
    combine: (results): UseCasesResult => {
      const errors: Error[] = [];
      let isError = false;
      // The aggregate is "loading" until every active query has either data or an error. This
      // wipes stale rows during chip-toggle transitions: when the user adds a new case type, its
      // query starts without cached data, so the table renders the skeleton instead of mixing
      // already-cached rows from other chips with the in-flight new chip. Without this, the user
      // sees a partial table that doesn't match the chip selection.
      let isPending = false;
      for (const q of results) {
        if (q.isError) {
          isError = true;
          if (q.error instanceof Error) errors.push(q.error);
          continue;
        }
        if (q.data === undefined) isPending = true;
      }
      if (isPending) {
        return { data: [], isLoading: true, isError, errors };
      }
      const rows: CaseRow[] = [];
      for (const q of results) {
        if (q.data) {
          for (const summary of q.data) rows.push(toCaseRow(summary));
        }
      }
      return { data: rows, isLoading: false, isError, errors };
    },
  });
}

/**
 * Story 2.6 — single-case fetch for the detail panel. Short-circuits when `id` is null
 * (URL has no caseId). Query key is `['case', id]` — Story 4.3 SSE invalidation will hit
 * this exact shape.
 */
export function useCase(id: string | null): UseQueryResult<CaseDto, Error> {
  // `skipToken` is the v5-blessed way to disable a query without inventing a sentinel
  // queryKey. It also makes the unsafe `id as string` cast unnecessary — when `id` is null
  // TanStack never invokes the queryFn, so `getCase` only ever sees a real id.
  return useQuery<CaseDto, Error>({
    queryKey: id ? caseQueryKeys.detail(id) : caseQueryKeys.detail('__disabled__'),
    queryFn: id ? () => getCase(id) : skipToken,
    staleTime: STALE_TIME_MS,
    refetchOnWindowFocus: true,
  });
}

/**
 * Story 2.7 — `POST /api/cases` mutation. On success: primes the detail-cache for the new id
 * (so the navigate to /cases/{id} paints from cache before the network round-trip), and
 * invalidates the cases-list queries so the next render picks the new row up. The new-case
 * highlight + announcement (recentlyCreated push) and the navigate are caller responsibility —
 * the hook stays UI-agnostic so unit tests can wrap it without a router.
 *
 * No automatic retry on 5xx — case-create has user-typed payload; silent retry can produce
 * duplicates if the backend half-succeeded. Idempotency-key headers ship in Phase 1.
 */
export function useCreateCase(): UseMutationResult<CaseDto, Error, CreateCaseRequest> {
  const queryClient = useQueryClient();
  return useMutation<CaseDto, Error, CreateCaseRequest>({
    mutationKey: ['cases', 'create'],
    mutationFn: createCase,
    onSuccess: (dto) => {
      queryClient.setQueryData(caseQueryKeys.detail(dto.id), dto);
      queryClient.invalidateQueries({ queryKey: caseQueryKeys.lists() });
    },
    retry: false,
  });
}
