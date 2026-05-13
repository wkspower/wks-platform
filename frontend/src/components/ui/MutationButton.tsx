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

import { Button, type ButtonProps } from './Button';

/**
 * Story 2.7 — 4-state slice of the lifecycle. Story 2.8 widened the union to add {@code
 * 'processing'} (5-state superset owned by {@code TaskLifecycleButton} for backend mutations that
 * may genuinely take >2s or whose true completion is signalled async via SSE — Story 4.3). This
 * component remains the 4-state presentational primitive; consumers like the case-creation form
 * (synchronous backend) stay on it. {@code TaskLifecycleButton} composes it for the four shared
 * states and adds the {@code processing} visual itself.
 *
 * Presentational: parents drive transitions via the {@code state} prop (typically derived from
 * RHF's {@code formState.isSubmitting} + a TanStack Query mutation's {@code isPending} /
 * {@code isSuccess} / {@code isError}). The component itself owns no state machine.
 */
export type MutationButtonState = 'idle' | 'confirming' | 'processing' | 'confirmed' | 'failed';

const CONFIRMED_FADE_MS = 2_000;

export interface MutationButtonProps extends Omit<
  ButtonHTMLAttributes<HTMLButtonElement>,
  'children'
> {
  /** Story 6.1 — optional visual variant, forwarded to the inner Button. */
  variant?: ButtonProps['variant'];
  state: MutationButtonState;
  /** Idle-state children. */
  children: ReactNode;
  /** Label for confirming state — parent supplies localised string. */
  confirmingLabel?: string;
  /** Label for processing state — Story 2.8; only shown when `state === 'processing'`. */
  processingLabel?: string;
  /** Label for confirmed state — parent supplies localised string. */
  confirmedLabel?: string;
  /** Reason-bearing label for failed state — parent supplies localised string. */
  failedLabel?: string;
  /** Optional retry slot; renders only on `failed`. */
  retryAction?: ReactNode;
  /**
   * Optional override for the `aria-live` announcement. When provided, the live region announces
   * this string instead of the visible-state label — used by `TaskLifecycleButton` to surface the
   * "Press Enter to refresh." hint on the conflict path without changing the visible button text.
   */
  announcement?: string;
}

export const MutationButton = forwardRef<HTMLButtonElement, MutationButtonProps>(
  function MutationButton(
    {
      state,
      children,
      confirmingLabel = 'Confirming…',
      processingLabel = 'Processing…',
      confirmedLabel = 'Confirmed',
      failedLabel = 'Failed',
      retryAction,
      announcement,
      className,
      disabled,
      variant,
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
      announcement ??
      (state === 'confirming'
        ? confirmingLabel
        : state === 'processing'
          ? processingLabel
          : state === 'confirmed'
            ? confirmedLabel
            : state === 'failed'
              ? failedLabel
              : '');

    const stateClass =
      state === 'confirming'
        ? 'bg-[var(--ring)] text-white animate-pulse'
        : state === 'processing'
          ? // Slower pulse to differentiate from `confirming`; same indigo accent.
            'bg-[var(--ring)] text-white animate-pulse [animation-duration:2s]'
          : state === 'confirmed'
            ? confirmedFaded
              ? ''
              : 'bg-[var(--success)] text-white hover:bg-[var(--success)]'
            : state === 'failed'
              ? 'bg-[var(--destructive)] text-white hover:bg-[var(--destructive)]'
              : '';

    const isVisuallyConfirmed = state === 'confirmed' && !confirmedFaded;
    const buttonDisabled =
      disabled || state === 'confirming' || state === 'processing' || isVisuallyConfirmed;

    return (
      <div className={cn('inline-flex items-center gap-2')}>
        <Button
          ref={ref}
          type={rest.type ?? 'submit'}
          variant={variant}
          aria-busy={state === 'confirming' || state === 'processing' ? true : undefined}
          aria-disabled={buttonDisabled || undefined}
          disabled={buttonDisabled}
          data-state={confirmedFaded && state === 'confirmed' ? 'idle' : state}
          className={cn(stateClass, className)}
          {...rest}
        >
          {state === 'confirming' && <Loader2 className="size-4 animate-spin" aria-hidden />}
          {state === 'processing' && <Loader2 className="size-4 animate-spin" aria-hidden />}
          {isVisuallyConfirmed && <Check className="size-4" aria-hidden />}
          {state === 'failed' && <X className="size-4" aria-hidden />}
          {(state === 'idle' || (state === 'confirmed' && confirmedFaded)) && children}
          {state === 'confirming' && confirmingLabel}
          {state === 'processing' && processingLabel}
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
