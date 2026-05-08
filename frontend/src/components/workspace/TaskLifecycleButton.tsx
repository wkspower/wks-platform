import { useQueryClient } from '@tanstack/react-query';
import { useEffect, useReducer, useRef } from 'react';

import { ApiError } from '@/api/client';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/AlertDialog';
import { Button } from '@/components/ui/Button';
import { MutationButton, type MutationButtonState } from '@/components/ui/MutationButton';
import { useCompleteTask } from '@/hooks/useTasks';
import { t } from '@/i18n';
import { getArchetypeAffordance } from '@/lib/archetypes';
import { caseQueryKeys, taskQueryKeys } from '@/lib/queryKeys';
import type { ConflictReason, TaskActionResponse, TaskDto } from '@/types/task';

const PROCESSING_TIMER_MS = 2_000;
const TAKING_LONGER_TIMER_MS = 30_000;
const CONFIRMED_FADE_MS = 2_000;

/**
 * Story 2.8 AC2 / AC3 / AC4 — universal mutation lifecycle for backend mutations whose true
 * completion is signalled async (Phase 2 SSE) or may genuinely take >2s. Owns its 5-state machine
 * internally via {@code useReducer}; composes {@link MutationButton} for the four shared visual
 * states (idle / confirming / confirmed / failed) and adds the {@code processing} visual + the
 * task-specific {@code [Retry]} / {@code [View Updated Case]} / {@code [Refresh case]} actions.
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
      // Only accept failures from in-flight states. Late dispatches from a previous mutation
      // (e.g. retried after success) must not demote a `confirmed` UI back to `failed`.
      if (state.kind !== 'confirming' && state.kind !== 'processing') return state;
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

interface ConflictPayload {
  reason: ConflictReason;
  actorName: string | null;
}

function parseConflictPayload(err: ApiError): ConflictPayload {
  // The 409 envelope (GlobalExceptionHandler) carries `{ error: { code: 'WKS-RTM-409', message } }`.
  // Phase 0 messages do not carry a structured `completedBy` actor name yet; we best-effort parse a
  // trailing `by {name}` substring so Phase 1 backend upgrades light up the {name} interpolation
  // automatically. Engine reasons are derived from the message text — narrow recognised patterns
  // map to the typed ConflictReason; everything else falls to 'unknown'.
  const msg = err.message ?? '';
  const lower = msg.toLowerCase();
  let reason: ConflictReason;
  if (lower.includes('already completed') || lower.includes('already_completed')) {
    reason = 'already_completed';
  } else if (
    lower.includes('reassigned') ||
    lower.includes('claimed') ||
    lower.includes('assignee')
  ) {
    reason = 'reassigned';
  } else if (lower.includes('engine') || lower.includes('unavailable')) {
    reason = 'engine_unavailable';
  } else {
    reason = 'unknown';
  }
  // Best-effort actor parse: "...completed by {Name}" or "...completed by {Name}.".
  const byMatch = /\bby\s+([^.]+?)(?:\.|$)/i.exec(msg);
  const actorName = byMatch?.[1]?.trim() ?? null;
  return { reason, actorName };
}

function classifyError(err: unknown): {
  message: string;
  conflict: boolean;
  conflictReason: ConflictReason | null;
  retryable: boolean;
} {
  if (err instanceof ApiError) {
    if (err.status === 409) {
      const { reason, actorName } = parseConflictPayload(err);
      const message =
        reason === 'already_completed' && actorName
          ? t('task.conflict.completedByOther', { name: actorName })
          : reason === 'already_completed'
            ? t('task.conflict.completedByUnknown')
            : err.message;
      return {
        message,
        conflict: true,
        conflictReason: reason,
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
    return {
      message: err.message,
      conflict: false,
      conflictReason: null,
      retryable: err.status >= 500,
    };
  }
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
  const [takingLonger, setTakingLonger] = useReducer(
    (_prev: boolean, next: boolean) => next,
    false,
  );
  const sseEnabled = import.meta.env.VITE_WKS_SSE_ENABLED === 'true';

  useEffect(() => {
    if (state.kind === 'confirming') {
      const handle = window.setTimeout(() => {
        dispatch({ type: 'processing_timer' });
      }, PROCESSING_TIMER_MS);
      return () => clearTimeout(handle);
    }
    return undefined;
  }, [state.kind]);

  useEffect(() => {
    if (state.kind === 'processing') {
      const handle = window.setTimeout(() => setTakingLonger(true), TAKING_LONGER_TIMER_MS);
      return () => {
        clearTimeout(handle);
        setTakingLonger(false);
      };
    }
    setTakingLonger(false);
    return undefined;
  }, [state.kind]);

  // Auto-reset confirmed → idle after the fade window, and refresh the task list at the same
  // moment. Keeping byCase invalidation here (instead of in `useCompleteTask.onSettled`) lets the
  // user see the green "Confirmed" chip for the full 2s before the parent refetch unmounts the
  // button (Story 2.8 P4 — invalidation race fix).
  useEffect(() => {
    if (state.kind !== 'confirmed') return undefined;
    const handle = window.setTimeout(() => {
      queryClient.invalidateQueries({ queryKey: taskQueryKeys.byCase(task.caseId) });
      dispatch({ type: 'reset' });
    }, CONFIRMED_FADE_MS);
    return () => clearTimeout(handle);
  }, [state.kind, queryClient, task.caseId]);

  function fire(): void {
    // P16 — guard against concurrent mutations from a fast retry / double-click. Reset any
    // previous mutation result before kicking off a fresh attempt.
    if (completeTask.isPending) return;
    completeTask.reset();
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
    : t('common.lifecycle.failedNoReason');

  // P2 — AC11: conflict path announces "...Press Enter to refresh." via the same live region.
  const announcement =
    state.kind === 'failed' && state.conflict ? t('task.conflict.refreshAnnouncement') : undefined;

  const retryRef = useRef<HTMLButtonElement | null>(null);
  const refreshRef = useRef<HTMLButtonElement | null>(null);

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

  // Story 6.1 — archetype-driven affordance. All renderer decisions go through the registry;
  // no archetype literal strings appear here.
  const affordance = getArchetypeAffordance(task.archetype);
  const archetypeLabel = t(affordance.ctaLabelKey);
  // Map ctaTone to Button variant — secondary tone uses 'ghost' styling for softer visual weight.
  const archetypeVariant = affordance.ctaTone === 'secondary' ? 'ghost' : 'default';
  const needsDialog = affordance.confirmationFlow === 'confirmation-dialog';
  const isTerminal =
    affordance.postActionState === 'locked' || affordance.postActionState === 'terminal-accent';

  // The idle label comes from the archetype registry when an archetype is declared; falls back to
  // task.actionLabel → task.name for un-typed tasks (backward-compat).
  const idleLabel = task.archetype ? archetypeLabel : (task.actionLabel ?? task.name);

  const mutationButton = (
    <MutationButton
      state={buttonState}
      confirmingLabel={t('task.confirming')}
      processingLabel={t('task.processing')}
      confirmedLabel={t('task.confirmed')}
      failedLabel={failedLabel}
      announcement={announcement}
      // variant is threaded via ...rest spread in MutationButton → Button
      variant={archetypeVariant}
      onClick={() => {
        // Dialog flow: fire() is triggered by AlertDialogAction instead of the button click.
        if (needsDialog) return;
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
  );

  return (
    <div
      className="inline-flex items-center gap-2"
      data-archetype-terminal={isTerminal && state.kind === 'confirmed' ? 'true' : undefined}
    >
      {needsDialog && state.kind === 'idle' ? (
        // Story 6.1 AC3 — business_final: AlertDialog interposes before fire().
        // The trigger renders the MutationButton; confirmation fires the mutation.
        <AlertDialog>
          <AlertDialogTrigger asChild>{mutationButton}</AlertDialogTrigger>
          <AlertDialogContent>
            <AlertDialogTitle>{t('task.confirm.title')}</AlertDialogTitle>
            <AlertDialogDescription>{t('task.confirm.description')}</AlertDialogDescription>
            <div className="flex justify-end gap-2 pt-2">
              <AlertDialogCancel>{t('common.cancel')}</AlertDialogCancel>
              <AlertDialogAction
                onClick={() => {
                  fire();
                }}
              >
                {archetypeLabel}
              </AlertDialogAction>
            </div>
          </AlertDialogContent>
        </AlertDialog>
      ) : (
        mutationButton
      )}
      {state.kind === 'processing' && takingLonger ? (
        <span className="text-xs text-[var(--muted-foreground)]">
          {t('task.processing.takingLonger')}
        </span>
      ) : null}
    </div>
  );
}
