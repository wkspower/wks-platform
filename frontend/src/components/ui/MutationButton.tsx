import { Check, Loader2, X } from 'lucide-react';
import {
  forwardRef,
  useEffect,
  useRef,
  useState,
  type ButtonHTMLAttributes,
  type ReactNode,
} from 'react';

import { cn } from '@/lib/cn';

import { Button } from './Button';

/**
 * Story 2.7 — 4-state slice of the TaskLifecycleButton state machine (UX spec §TaskLifecycleButton
 * lines 716-744). The 5-state owner with the {@code processing} state ships in Story 2.8 — at that
 * point this component is either renamed or wrapped. The TS literal-union here is forward-
 * compatible: 2.8 widens the union to add {@code 'processing'}.
 *
 * Presentational: parents drive transitions via the {@code state} prop (typically derived from
 * RHF's {@code formState.isSubmitting} + a TanStack Query mutation's {@code isPending} /
 * {@code isSuccess} / {@code isError}). The component itself owns no state machine.
 */
export type MutationButtonState = 'idle' | 'confirming' | 'confirmed' | 'failed';

const CONFIRMED_FADE_MS = 2_000;

export interface MutationButtonProps extends Omit<
  ButtonHTMLAttributes<HTMLButtonElement>,
  'children'
> {
  state: MutationButtonState;
  /** Idle-state children. */
  children: ReactNode;
  /** Label for confirming state — parent supplies localised string. */
  confirmingLabel?: string;
  /** Label for confirmed state — parent supplies localised string. */
  confirmedLabel?: string;
  /** Reason-bearing label for failed state — parent supplies localised string. */
  failedLabel?: string;
  /** Optional retry slot; renders only on `failed`. */
  retryAction?: ReactNode;
}

export const MutationButton = forwardRef<HTMLButtonElement, MutationButtonProps>(
  function MutationButton(
    {
      state,
      children,
      confirmingLabel = 'Confirming…',
      confirmedLabel = 'Confirmed',
      failedLabel = 'Failed',
      retryAction,
      className,
      disabled,
      ...rest
    },
    ref,
  ) {
    // P6 — AC6 says "Holds visible 2s then fades to subtle". After 2s in `confirmed`, drop the
    // green chip and re-enable the button so the parent can either stay (e.g. "Save and view") or
    // trigger the next action. Reset on any state change away from confirmed.
    const [confirmedFaded, setConfirmedFaded] = useState(false);
    const announceKey = useRef(0);
    useEffect(() => {
      setConfirmedFaded(false);
      if (state !== 'confirmed') return;
      const handle = window.setTimeout(() => setConfirmedFaded(true), CONFIRMED_FADE_MS);
      return () => clearTimeout(handle);
    }, [state]);
    // Bump the announce key on every state transition so the sr-only span re-renders even when
    // the visible text is unchanged — some AT engines coalesce identical adjacent updates.
    useEffect(() => {
      announceKey.current += 1;
    }, [state]);

    // P24 — hold `polite` for the lifetime of the live region; switching politeness mid-flight
    // makes some AT engines drop region tracking. Failed state is still loud because the text
    // changes, the region just doesn't switch to assertive.
    const announce =
      state === 'confirming'
        ? confirmingLabel
        : state === 'confirmed'
          ? confirmedLabel
          : state === 'failed'
            ? failedLabel
            : '';

    const stateClass =
      state === 'confirming'
        ? 'bg-[var(--ring)] text-white animate-pulse'
        : state === 'confirmed'
          ? confirmedFaded
            ? ''
            : 'bg-emerald-600 text-white hover:bg-emerald-600'
          : state === 'failed'
            ? 'bg-[var(--destructive)] text-white hover:bg-[var(--destructive)]'
            : '';

    const isVisuallyConfirmed = state === 'confirmed' && !confirmedFaded;
    const buttonDisabled = disabled || state === 'confirming' || isVisuallyConfirmed;

    return (
      <div className={cn('inline-flex items-center gap-2')}>
        <Button
          ref={ref}
          type={rest.type ?? 'submit'}
          aria-busy={state === 'confirming' ? true : undefined}
          disabled={buttonDisabled}
          data-state={confirmedFaded && state === 'confirmed' ? 'idle' : state}
          className={cn(stateClass, className)}
          {...rest}
        >
          {state === 'confirming' && <Loader2 className="size-4 animate-spin" aria-hidden />}
          {isVisuallyConfirmed && <Check className="size-4" aria-hidden />}
          {state === 'failed' && <X className="size-4" aria-hidden />}
          {(state === 'idle' || (state === 'confirmed' && confirmedFaded)) && children}
          {state === 'confirming' && confirmingLabel}
          {isVisuallyConfirmed && confirmedLabel}
          {state === 'failed' && failedLabel}
          <span key={announceKey.current} className="sr-only" aria-live="polite">
            {announce}
          </span>
        </Button>
        {state === 'failed' && retryAction}
      </div>
    );
  },
);
