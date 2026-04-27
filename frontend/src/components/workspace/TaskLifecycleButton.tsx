import { useQueryClient } from '@tanstack/react-query';
import { useEffect, useReducer, useRef } from 'react';

import { ApiError } from '@/api/client';
import { Button } from '@/components/ui/Button';
import { MutationButton, type MutationButtonState } from '@/components/ui/MutationButton';
import { useCompleteTask } from '@/hooks/useTasks';
import { t } from '@/i18n';
import { caseQueryKeys, taskQueryKeys } from '@/lib/queryKeys';
import type { ConflictReason, TaskActionResponse, TaskDto } from '@/types/task';

const PROCESSING_TIMER_MS = 2_000;
const TAKING_LONGER_TIMER_MS = 30_000;

/**
 * Story 2.8 AC2 / AC3 / AC4 — universal mutation lifecycle for backend mutations whose true
 * completion is signalled async (Phase 2 SSE) or may genuinely take >2s. Owns its 5-state machine
 * internally via {@code useReducer}; composes {@link MutationButton} for the four shared visual
 * states (idle / confirming / confirmed / failed) and adds the {@code processing} visual + the
 * task-specific {@code [Retry]} / {@code [View Updated Case]} / {@code [Refresh case]} actions.
 *
 * <p>Phase 1 (Story 2.8) — REST-only. The 2s `processing` timer is armed at `confirming` entry
 * but cleared the instant the REST response lands; Phase 1 therefore never enters `processing`.
 * Phase 2 (Story 4.3) — SSE flips the source-of-truth: {@code VITE_WKS_SSE_ENABLED=true} arms
 * `processing` pre-emptively while the component waits for the SSE confirmation event.
 */
export interface TaskLifecycleButtonProps {
  task: TaskDto;
  onCompleted?: (response: TaskActionResponse) => void;
  onConflict?: (reason: ConflictReason) => void;
}

type State =
  | { kind: 'idle' }
  | { kind: 'confirming' }
  | { kind: 'processing' }
  | { kind: 'confirmed' }
  | {
      kind: 'failed';
      message: string;
      conflict: boolean;
      conflictReason: ConflictReason | null;
      retryable: boolean;
    };

type Action =
  | { type: 'click' }
  | { type: 'rest_success' }
  | {
      type: 'rest_failure';
      message: string;
      conflict: boolean;
      conflictReason: ConflictReason | null;
      retryable: boolean;
    }
  | { type: 'processing_timer' }
  | { type: 'reset' };

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case 'click':
      if (state.kind !== 'idle' && state.kind !== 'failed') return state;
      return { kind: 'confirming' };
    case 'rest_success':
      if (state.kind !== 'confirming' && state.kind !== 'processing') return state;
      return { kind: 'confirmed' };
    case 'rest_failure':
      return {
        kind: 'failed',
        message: action.message,
        conflict: action.conflict,
        conflictReason: action.conflictReason,
        retryable: action.retryable,
      };
    case 'processing_timer':
      if (state.kind !== 'confirming') return state;
      return { kind: 'processing' };
    case 'reset':
      return { kind: 'idle' };
  }
}

function classifyError(err: unknown): {
  message: string;
  conflict: boolean;
  conflictReason: ConflictReason | null;
  retryable: boolean;
} {
  if (err instanceof ApiError) {
    if (err.status === 409) {
      // Phase 0: the engine returns a generic message without the actor's display name. The copy
      // degrades to the no-name variant per AC5.
      return {
        message: t('task.conflict.completedByUnknown'),
        conflict: true,
        conflictReason: 'already_completed',
        retryable: false,
      };
    }
    if (err.status === 403 || err.status === 404) {
      return {
        message: err.message,
        conflict: false,
        conflictReason: null,
        retryable: false,
      };
    }
    // 5xx: retryable.
    return {
      message: err.message,
      conflict: false,
      conflictReason: null,
      retryable: err.status >= 500,
    };
  }
  // Network / unknown — treat as retryable.
  return {
    message: err instanceof Error ? err.message : String(err),
    conflict: false,
    conflictReason: null,
    retryable: true,
  };
}

export function TaskLifecycleButton({ task, onCompleted, onConflict }: TaskLifecycleButtonProps) {
  const queryClient = useQueryClient();
  const [state, dispatch] = useReducer(reducer, { kind: 'idle' } as State);
  const completeTask = useCompleteTask();
  const processingTimerRef = useRef<number | null>(null);
  const takingLongerTimerRef = useRef<number | null>(null);
  const [takingLonger, setTakingLonger] = useReducer(
    (_prev: boolean, next: boolean) => next,
    false,
  );
  // Story 4.3 will flip this to true; keep wiring in place so 4.3 only flips the env flag.
  const sseEnabled = import.meta.env.VITE_WKS_SSE_ENABLED === 'true';

  // Arm the processing timer on `confirming` entry; clear it on any other state.
  useEffect(() => {
    if (state.kind === 'confirming') {
      const handle = window.setTimeout(() => {
        dispatch({ type: 'processing_timer' });
      }, PROCESSING_TIMER_MS);
      processingTimerRef.current = handle;
      return () => {
        clearTimeout(handle);
        processingTimerRef.current = null;
      };
    }
    return undefined;
  }, [state.kind]);

  // Arm the 30s "Taking longer than expected" timer on `processing` entry.
  useEffect(() => {
    if (state.kind === 'processing') {
      const handle = window.setTimeout(() => setTakingLonger(true), TAKING_LONGER_TIMER_MS);
      takingLongerTimerRef.current = handle;
      return () => {
        clearTimeout(handle);
        takingLongerTimerRef.current = null;
        setTakingLonger(false);
      };
    }
    setTakingLonger(false);
    return undefined;
  }, [state.kind]);

  // Reset to idle a brief moment after `confirmed` so the user can act again or move on. The
  // green chip fade is owned by MutationButton (2s); we don't auto-reset here in Phase 1 because
  // task completion typically navigates the user to the next task or the case detail refetch
  // surfaces a new pending task.
  // Phase 2 (SSE) arrival → `confirmed` is dispatched by the SSE listener; same handling.

  function fire(): void {
    dispatch({ type: 'click' });
    completeTask.mutate(
      { taskId: task.id, caseId: task.caseId },
      {
        onSuccess: (response) => {
          dispatch({ type: 'rest_success' });
          onCompleted?.(response);
        },
        onError: (err) => {
          const classified = classifyError(err);
          dispatch({
            type: 'rest_failure',
            ...classified,
          });
          if (classified.conflict && classified.conflictReason) {
            onConflict?.(classified.conflictReason);
          }
        },
      },
    );
    // Phase 2 (SSE on): keep `processing` armed; success arrives via SSE. Phase 1 (SSE off): the
    // REST onSuccess above lands well before the 2s timer in the common case.
    void sseEnabled;
  }

  const buttonState: MutationButtonState =
    state.kind === 'idle'
      ? 'idle'
      : state.kind === 'confirming'
        ? 'confirming'
        : state.kind === 'processing'
          ? 'processing'
          : state.kind === 'confirmed'
            ? 'confirmed'
            : 'failed';

  const failedReason = state.kind === 'failed' ? state.message : null;
  const failedLabel = failedReason
    ? t('task.failed', { reason: failedReason })
    : t('task.failed', { reason: '' });

  const retryRef = useRef<HTMLButtonElement | null>(null);
  const refreshRef = useRef<HTMLButtonElement | null>(null);

  // AC11 — focus the recovery action when state lands in `failed`. Conflict path focuses
  // [Refresh case]; retryable failures focus [Retry].
  useEffect(() => {
    if (state.kind !== 'failed') return;
    const handle = requestAnimationFrame(() => {
      if (state.conflict) {
        refreshRef.current?.focus();
      } else if (state.retryable) {
        retryRef.current?.focus();
      }
    });
    return () => cancelAnimationFrame(handle);
  }, [state]);

  const retryAction =
    state.kind === 'failed' && state.retryable && !state.conflict ? (
      <Button
        ref={retryRef}
        type="button"
        variant="ghost"
        onClick={() => {
          dispatch({ type: 'reset' });
          fire();
        }}
      >
        {t('task.retry')}
      </Button>
    ) : null;

  const refreshAction =
    state.kind === 'failed' && state.conflict ? (
      <Button
        ref={refreshRef}
        type="button"
        variant="ghost"
        onClick={() => {
          queryClient.invalidateQueries({ queryKey: taskQueryKeys.byCase(task.caseId) });
          queryClient.invalidateQueries({ queryKey: caseQueryKeys.detail(task.caseId) });
          dispatch({ type: 'reset' });
        }}
      >
        {t('task.refreshCase')}
      </Button>
    ) : null;

  const viewUpdatedCaseAction =
    state.kind === 'failed' && state.retryable ? (
      <Button
        type="button"
        variant="ghost"
        onClick={() => {
          queryClient.invalidateQueries({ queryKey: caseQueryKeys.detail(task.caseId) });
        }}
      >
        {t('task.viewUpdatedCase')}
      </Button>
    ) : null;

  const idleLabel = task.actionLabel ?? task.name;

  return (
    <div className="inline-flex items-center gap-2">
      <MutationButton
        state={buttonState}
        confirmingLabel={t('task.confirming')}
        processingLabel={t('task.processing')}
        confirmedLabel={t('task.confirmed')}
        failedLabel={failedLabel}
        onClick={() => {
          if (state.kind === 'idle') fire();
        }}
        retryAction={
          <>
            {retryAction}
            {refreshAction}
            {viewUpdatedCaseAction}
          </>
        }
      >
        {idleLabel}
      </MutationButton>
      {state.kind === 'processing' && takingLonger ? (
        <span className="text-xs text-[var(--muted-foreground)]" aria-live="polite">
          {t('task.processing.takingLonger')}
        </span>
      ) : null}
    </div>
  );
}
