import { useQueryClient } from '@tanstack/react-query';
import { useReducer } from 'react';

import { ApiError } from '@/api/client';
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogTitle,
} from '@/components/ui/AlertDialog';
import { Button } from '@/components/ui/Button';
import { useCompleteTask } from '@/hooks/useTasks';
import { t } from '@/i18n';
import { outcomeAffordance } from '@/lib/archetypes';
import { taskQueryKeys } from '@/lib/queryKeys';
import type { ConflictReason, TaskActionResponse, TaskDto } from '@/types/task';

/**
 * Story 6.2 AC1 — multi-button outcome picker dialog. Renders when
 * {@code outcomeMappings} is non-empty; replaces the single-CTA {@link TaskLifecycleButton}
 * interaction with a modal containing one button per declared outcome.
 *
 * <p>When {@code outcomeMappings} is empty the dialog should not be rendered — the single-CTA
 * path in {@link TaskLifecycleButton} is preserved (no regression).
 *
 * <p>Phase-0 decision: this dialog collects NO form fields alongside the outcome. The
 * {@code outcome} key is the only load-bearing wire field (AC2). Form-collected outcome fields
 * are deferred to a future story (6-3 or 5-x form-extension).
 */
export interface TaskCompletionDialogProps {
  task: TaskDto;
  /** Key → stageTransition map from the case-type view's outcomeMappings. Non-empty by contract. */
  outcomeMappings: Record<string, string>;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onCompleted?: (response: TaskActionResponse) => void;
  onConflict?: (reason: ConflictReason) => void;
}

type State =
  | { kind: 'idle' }
  | { kind: 'submitting'; outcomeKey: string }
  | { kind: 'failed'; message: string; outcomeKey: string };

type Action =
  | { type: 'submit'; outcomeKey: string }
  | { type: 'success' }
  | { type: 'failure'; message: string; outcomeKey: string }
  | { type: 'reset' };

function reducer(state: State, action: Action): State {
  switch (action.type) {
    case 'submit':
      if (state.kind !== 'idle') return state;
      return { kind: 'submitting', outcomeKey: action.outcomeKey };
    case 'success':
      return { kind: 'idle' };
    case 'failure':
      return { kind: 'failed', message: action.message, outcomeKey: action.outcomeKey };
    case 'reset':
      return { kind: 'idle' };
  }
}

function classifyError(err: unknown): {
  message: string;
  conflict: boolean;
  conflictReason: ConflictReason | null;
} {
  if (err instanceof ApiError) {
    if (err.status === 409) {
      return {
        message: t('task.conflict.completedByUnknown'),
        conflict: true,
        conflictReason: 'already_completed',
      };
    }
    return { message: err.message, conflict: false, conflictReason: null };
  }
  return {
    message: err instanceof Error ? err.message : String(err),
    conflict: false,
    conflictReason: null,
  };
}

/** Title-case a camelCase or kebab-case outcome key for use as a fallback label. */
function titleCaseKey(key: string): string {
  return key
    .replace(/([A-Z])/g, ' $1')
    .replace(/-/g, ' ')
    .replace(/^./, (s) => s.toUpperCase())
    .trim();
}

/** Resolve i18n label for a canonical outcome key, falling back to Title-cased key. */
function outcomeLabel(key: string): string {
  const i18nKey = `task.outcome.${key}`;
  const resolved = t(i18nKey as Parameters<typeof t>[0]);
  // If the key is returned unchanged (no translation), fall back to Title-cased version.
  return resolved === i18nKey ? titleCaseKey(key) : resolved;
}

const CONFIRMED_FADE_MS = 2_000;

export function TaskCompletionDialog({
  task,
  outcomeMappings,
  open,
  onOpenChange,
  onCompleted,
  onConflict,
}: TaskCompletionDialogProps) {
  const queryClient = useQueryClient();
  const [state, dispatch] = useReducer(reducer, { kind: 'idle' } as State);
  const completeTask = useCompleteTask();

  // Story 6.2 — filter out whitespace-only / blank keys so a malformed mapping cannot render
  // an unlabeled button. Empty (after filtering) → render nothing; the caller should fall back
  // to the single-CTA TaskLifecycleButton path.
  const outcomeKeys = Object.keys(outcomeMappings).filter((k) => k.trim().length > 0);

  // Empty-state guard — never render an outcome picker with zero buttons.
  if (outcomeKeys.length === 0) {
    return null;
  }

  function fire(outcomeKey: string): void {
    if (state.kind === 'submitting') return;
    completeTask.reset();
    dispatch({ type: 'submit', outcomeKey });
    completeTask.mutate(
      { taskId: task.id, caseId: task.caseId, outcome: outcomeKey },
      {
        onSuccess: (response) => {
          dispatch({ type: 'success' });
          onOpenChange(false);
          // Invalidate task list after the dialog closes so the confirmed state is visible.
          window.setTimeout(() => {
            queryClient.invalidateQueries({ queryKey: taskQueryKeys.byCase(task.caseId) });
          }, CONFIRMED_FADE_MS);
          onCompleted?.(response);
        },
        onError: (err) => {
          const classified = classifyError(err);
          dispatch({
            type: 'failure',
            message: classified.message,
            outcomeKey,
          });
          if (classified.conflict && classified.conflictReason) {
            onConflict?.(classified.conflictReason);
          }
        },
      },
    );
  }

  const isSubmitting = state.kind === 'submitting';

  return (
    <AlertDialog open={open} onOpenChange={onOpenChange}>
      <AlertDialogContent>
        <AlertDialogTitle>{t('task.outcomes.title')}</AlertDialogTitle>
        <AlertDialogDescription className="sr-only">{task.name}</AlertDialogDescription>

        {state.kind === 'failed' ? (
          <p className="text-sm text-[var(--destructive)]" role="alert">
            {state.message}
          </p>
        ) : null}

        <div className="flex flex-col gap-2 pt-2">
          {outcomeKeys.map((key, index) => {
            const affordance = outcomeAffordance(task.archetype, index);
            const isThisSubmitting = state.kind === 'submitting' && state.outcomeKey === key;
            return (
              <Button
                key={key}
                type="button"
                variant={affordance.tone === 'primary' ? 'default' : 'ghost'}
                disabled={isSubmitting}
                data-outcome-key={key}
                data-terminal-accent={affordance.terminalAccent ? 'true' : undefined}
                onClick={() => fire(key)}
              >
                {isThisSubmitting ? t('task.confirming') : outcomeLabel(key)}
              </Button>
            );
          })}
        </div>

        <div className="flex justify-end pt-2">
          <Button
            type="button"
            variant="ghost"
            disabled={isSubmitting}
            onClick={() => {
              dispatch({ type: 'reset' });
              onOpenChange(false);
            }}
          >
            {t('task.outcomes.cancel')}
          </Button>
        </div>
      </AlertDialogContent>
    </AlertDialog>
  );
}
