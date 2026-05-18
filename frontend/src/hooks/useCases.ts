import {
  skipToken,
  useMutation,
  useQueries,
  useQuery,
  useQueryClient,
  type UseMutationResult,
  type UseQueryResult,
} from '@tanstack/react-query';

import {
  createCase,
  getCase,
  listCases,
  transitionCase,
  updateCase,
  type CreateCaseRequest,
  type TransitionCaseRequest,
  type UpdateCaseRequest,
} from '@/api/cases';
import { toast } from '@/components/ui/Toaster';
import { caseQueryKeys, type CaseListQuery } from '@/lib/queryKeys';
import { toCaseRow, type CaseDto, type CaseRow, type CaseSummary } from '@/types/case';

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

export function useCases({ caseTypeIds, status, size = 100, sort }: UseCasesArgs): UseCasesResult {
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

export function useCase(id: string | null): UseQueryResult<CaseDto, Error> {
  return useQuery<CaseDto, Error>({
    queryKey: id ? caseQueryKeys.detail(id) : caseQueryKeys.detail('__disabled__'),
    queryFn: id ? () => getCase(id) : skipToken,
    staleTime: STALE_TIME_MS,
    refetchOnWindowFocus: true,
  });
}

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

/**
 * Optimistic helper — patches the case detail cache + every cached list query that contains
 * the case. Returns a rollback function that restores all touched snapshots.
 */
function patchCaseCaches(
  queryClient: ReturnType<typeof useQueryClient>,
  id: string,
  patch: (prev: CaseDto) => CaseDto,
  patchSummary: (prev: CaseSummary) => CaseSummary,
): () => void {
  const detailKey = caseQueryKeys.detail(id);
  const prevDetail = queryClient.getQueryData<CaseDto>(detailKey);
  if (prevDetail) {
    queryClient.setQueryData<CaseDto>(detailKey, patch(prevDetail));
  }
  // Patch every list query whose data contains this case.
  const listSnapshots: Array<{ key: readonly unknown[]; data: CaseSummary[] }> = [];
  const lists = queryClient.getQueriesData<CaseSummary[]>({ queryKey: caseQueryKeys.lists() });
  for (const [key, data] of lists) {
    if (!data) continue;
    const idx = data.findIndex((s) => s.id === id);
    if (idx === -1) continue;
    listSnapshots.push({ key, data });
    const next = [...data];
    next[idx] = patchSummary(data[idx]!);
    queryClient.setQueryData<CaseSummary[]>(key, next);
  }
  return () => {
    if (prevDetail) queryClient.setQueryData(detailKey, prevDetail);
    else queryClient.removeQueries({ queryKey: detailKey });
    for (const { key, data } of listSnapshots) {
      queryClient.setQueryData(key, data);
    }
  };
}

export interface UpdateCaseContext {
  rollback: () => void;
  prevData: Record<string, unknown> | null;
  prevVersion: number | null;
}

export function useUpdateCase(
  id: string,
): UseMutationResult<CaseDto, Error, UpdateCaseRequest, UpdateCaseContext> {
  const queryClient = useQueryClient();
  return useMutation<CaseDto, Error, UpdateCaseRequest, UpdateCaseContext>({
    mutationKey: ['cases', 'update', id],
    mutationFn: (req) => updateCase(id, req),
    onMutate: async (req) => {
      await queryClient.cancelQueries({ queryKey: caseQueryKeys.detail(id) });
      const prevDetail = queryClient.getQueryData<CaseDto>(caseQueryKeys.detail(id));
      const rollback = patchCaseCaches(
        queryClient,
        id,
        (prev) => ({ ...prev, data: { ...prev.data, ...req.data } }),
        (prev) => ({ ...prev, fields: { ...prev.fields, ...req.data } }),
      );
      return {
        rollback,
        prevData: prevDetail?.data ?? null,
        prevVersion: prevDetail?.version ?? null,
      };
    },
    onError: (err, _req, ctx) => {
      ctx?.rollback();
      toast({ tone: 'error', message: `Couldn't update case — ${err.message}` });
    },
    onSuccess: (dto, _req, ctx) => {
      queryClient.setQueryData(caseQueryKeys.detail(dto.id), dto);
      queryClient.invalidateQueries({ queryKey: caseQueryKeys.lists() });
      const prevData = ctx?.prevData;
      if (prevData == null) {
        toast({ tone: 'success', message: 'Case updated' });
        return;
      }
      toast({
        tone: 'success',
        message: 'Case updated',
        undo: async () => {
          // Use the latest server version to avoid 409 on undo.
          const latest = queryClient.getQueryData<CaseDto>(caseQueryKeys.detail(dto.id)) ?? dto;
          try {
            const reverted = await updateCase(dto.id, { data: prevData, version: latest.version });
            queryClient.setQueryData(caseQueryKeys.detail(dto.id), reverted);
            queryClient.invalidateQueries({ queryKey: caseQueryKeys.lists() });
            toast({ tone: 'info', message: 'Change reverted' });
          } catch (e) {
            const msg = e instanceof Error ? e.message : 'unknown error';
            toast({ tone: 'error', message: `Couldn't undo — ${msg}` });
          }
        },
      });
    },
    retry: false,
  });
}

export function useTransitionCase(
  id: string,
): UseMutationResult<CaseDto, Error, TransitionCaseRequest, { rollback: () => void }> {
  const queryClient = useQueryClient();
  return useMutation<CaseDto, Error, TransitionCaseRequest, { rollback: () => void }>({
    mutationKey: ['cases', 'transition', id],
    mutationFn: (req) => transitionCase(id, req),
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: caseQueryKeys.detail(id) });
      // The engine determines the resulting status; we can't reliably predict it, so the
      // optimistic patch here is a noop for status. We still snapshot caches so onError has
      // something to roll back to if the request fails mid-flight.
      const rollback = patchCaseCaches(
        queryClient,
        id,
        (prev) => prev,
        (prev) => prev,
      );
      return { rollback };
    },
    onError: (err, _req, ctx) => {
      ctx?.rollback();
      toast({ tone: 'error', message: `Couldn't transition case — ${err.message}` });
    },
    onSuccess: (dto) => {
      queryClient.setQueryData(caseQueryKeys.detail(dto.id), dto);
      queryClient.invalidateQueries({ queryKey: caseQueryKeys.lists() });
      toast({ tone: 'success', message: 'Case transitioned' });
    },
    retry: false,
  });
}

/**
 * Optimistic status flip for a single case — used by inline status edits and bulk status actions.
 * Returns a function for the undo path that restores the previous status.
 */
export function applyOptimisticStatus(
  queryClient: ReturnType<typeof useQueryClient>,
  id: string,
  nextStatus: string,
): { rollback: () => void; prevStatus: string | null } {
  const detail = queryClient.getQueryData<CaseDto>(caseQueryKeys.detail(id));
  const lists = queryClient.getQueriesData<CaseSummary[]>({ queryKey: caseQueryKeys.lists() });
  let prevStatus: string | null = detail?.status ?? null;
  for (const [, data] of lists) {
    if (!data) continue;
    const row = data.find((r) => r.id === id);
    if (row && prevStatus == null) prevStatus = row.status;
  }
  const rollback = patchCaseCaches(
    queryClient,
    id,
    (prev) => ({ ...prev, status: nextStatus }),
    (prev) => ({ ...prev, status: nextStatus }),
  );
  return { rollback, prevStatus };
}
