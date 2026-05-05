import {
  forwardRef,
  useCallback,
  useEffect,
  useLayoutEffect,
  useMemo,
  useRef,
  useState,
  type KeyboardEvent as ReactKeyboardEvent,
} from 'react';

import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/Popover';
import { t } from '@/i18n';
import { cn } from '@/lib/cn';
import { formatDate, formatDateTime } from '@/lib/formatDate';
import type { StageState, StageView } from '@/types/case';
import type { StageDefinitionView } from '@/types/caseType';

/**
 * Story 3.3 — Stage Timeline component.
 *
 * Pure presentational. Backend is the sole source of truth for `state` (AC1 / D1 invariant). The
 * component never infers `ACTIVE` / `SKIPPED` from any other field; it does not call `useCase`,
 * does not subscribe to SSE, does not mutate.
 *
 * Layout: horizontal at >= 960px pane width; vertical stepper below 960px AND for any case with
 * `stages.length > MAX_HORIZONTAL_STAGES` (Q1 default = 12). Pane width is observed via
 * `ResizeObserver` — never `window.innerWidth` (split-pane width !== viewport width).
 *
 * Animation: NONE in Phase 0. The `experimental_animate` prop is exposed for Story 4.3 to flip on
 * later when the SSE bridge lands. `prefers-reduced-motion: reduce` suppresses the active-node
 * pulse class.
 */

/** Q1 default — locked 2026-05-05. Backend `WKS-CFG-034` validator parked as Story 3.8. */
export const MAX_HORIZONTAL_STAGES = 12;

/** Pane-width breakpoint between horizontal layout and vertical stepper. */
export const HORIZONTAL_LAYOUT_MIN_WIDTH = 960;

/** Hover delay before the popover opens (AC7). */
const HOVER_DELAY_MS = 200;

export interface StageTimelineProps {
  /** Ordered list (by ordinal ASC) — comes from `caseDto.stages` directly. */
  stages: StageView[];
  /**
   * Optional pass-through for the declared schema. When supplied, used as a fallback for stage
   * `displayName` resolution (the wire `StageView.displayName` is normally already populated by
   * the backend mapper).
   */
  caseTypeStageDefs?: StageDefinitionView[];
  /**
   * Story 4.3 wires this to `true` once the SSE bridge lands, flipping on the cross-fade animation
   * grammar from `ux-stage-timeline.md` §5. Default `false` in Phase 0 — visual transitions are
   * instant per Q3 default. Underscore prefix mirrors the spec's `experimental_` notation.
   */
  experimental_animate?: boolean;
}

type LayoutMode = 'horizontal' | 'vertical';

/**
 * Returns the lowercased CSS-class-name token for a state. Wire is uppercase — class names are
 * lowercase per `ux-stage-timeline.md` §11.
 */
function stateClass(state: StageState): string {
  return state.toLowerCase();
}

/**
 * Format a duration between two ISO-8601 timestamps as a coarse human-readable string. Used for
 * the completed-stage popover (`In stage for 1d 4h`). Coarse on purpose — exact seconds would feel
 * busy and the BFSI-style use cases never need sub-minute precision.
 */
function formatDuration(fromIso: string, toIso: string): string {
  const ms = new Date(toIso).getTime() - new Date(fromIso).getTime();
  if (!Number.isFinite(ms) || ms <= 0) return '0m';
  const minutes = Math.floor(ms / 60000);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  if (days >= 1) {
    const remHours = hours - days * 24;
    return remHours > 0 ? `${days}d ${remHours}h` : `${days}d`;
  }
  if (hours >= 1) {
    const remMin = minutes - hours * 60;
    return remMin > 0 ? `${hours}h ${remMin}m` : `${hours}h`;
  }
  return `${Math.max(1, minutes)}m`;
}

/** Produce the inline timestamp slot for each state. Returns `null` for `pending`. */
function inlineTimestamp(stage: StageView): string | null {
  switch (stage.state) {
    case 'COMPLETED':
      if (stage.enteredAt && stage.exitedAt) {
        return t('stageTimeline.timestamp.enteredAndExited', {
          entered: formatDate(stage.enteredAt),
          exited: formatDate(stage.exitedAt),
        });
      }
      return stage.exitedAt
        ? t('stageTimeline.timestamp.exited', { date: formatDate(stage.exitedAt) })
        : null;
    case 'ACTIVE':
      return stage.enteredAt
        ? t('stageTimeline.timestamp.currentSince', { date: formatDate(stage.enteredAt) })
        : null;
    case 'SKIPPED':
      return stage.exitedAt
        ? t('stageTimeline.timestamp.skippedOn', { date: formatDate(stage.exitedAt) })
        : t('stageTimeline.popover.skipped');
    case 'PENDING':
    default:
      return null;
  }
}

/** Translate the wire-state to the human-readable label used in aria announcements + popovers. */
function stateLabel(state: StageState): string {
  switch (state) {
    case 'COMPLETED':
      return t('stageTimeline.state.completed');
    case 'ACTIVE':
      return t('stageTimeline.state.active');
    case 'SKIPPED':
      return t('stageTimeline.state.skipped');
    case 'PENDING':
    default:
      return t('stageTimeline.state.pending');
  }
}

/**
 * Per-state popover body. `pending` returns nothing — pending stages should feel quiet, not invite
 * speculation (`ux-stage-timeline.md` §6.1).
 */
function popoverExtras(stage: StageView): string | null {
  switch (stage.state) {
    case 'COMPLETED':
      if (stage.enteredAt && stage.exitedAt) {
        return t('stageTimeline.popover.completed.duration', {
          duration: formatDuration(stage.enteredAt, stage.exitedAt),
        });
      }
      return null;
    case 'ACTIVE':
      return stage.enteredAt
        ? t('stageTimeline.popover.active.currentSince', {
            timestamp: formatDateTime(stage.enteredAt),
          })
        : null;
    case 'SKIPPED':
      // Q2 lock 2026-05-05 — show "Skipped" only. Richer payload deferred to Epic 4.
      return t('stageTimeline.popover.skipped');
    case 'PENDING':
    default:
      return null;
  }
}

/**
 * ResizeObserver-based pane width hook. Story spec calls for the same idiom as
 * `lib/layoutBreakpoints.ts` — that file holds constants, not a hook, so we implement the hook
 * locally and keep the breakpoint constant exported above. Returns `null` until the first
 * observation lands, so SSR / pre-mount renders don't lock into the wrong layout.
 */
function usePaneWidth(): {
  ref: (node: HTMLElement | null) => void;
  width: number | null;
} {
  const [width, setWidth] = useState<number | null>(null);
  // `globalThis.ResizeObserver` keeps the ESLint `no-undef` rule happy without adding a global.
  const RO = (globalThis as { ResizeObserver?: typeof globalThis.ResizeObserver }).ResizeObserver;
  type ROInstance = InstanceType<NonNullable<typeof RO>>;
  const observerRef = useRef<ROInstance | null>(null);

  const ref = useCallback(
    (node: HTMLElement | null) => {
      if (observerRef.current) {
        observerRef.current.disconnect();
        observerRef.current = null;
      }
      if (!node) {
        setWidth(null);
        return;
      }
      if (!RO) {
        setWidth(node.getBoundingClientRect().width);
        return;
      }
      setWidth(node.getBoundingClientRect().width);
      const observer = new RO((entries) => {
        const entry = entries[0];
        if (entry) setWidth(entry.contentRect.width);
      });
      observer.observe(node);
      observerRef.current = observer;
    },
    [RO],
  );

  useEffect(
    () => () => {
      observerRef.current?.disconnect();
    },
    [],
  );

  return { ref, width };
}

/** Inconsistent-state defensive `console.warn` (AC10 / WKS-UI-2001). */
function warnIfInconsistent(stages: StageView[]): void {
  const activeCount = stages.filter((s) => s.state === 'ACTIVE').length;
  if (activeCount > 1) {
    // eslint-disable-next-line no-console
    console.warn(`WKS-UI-2001: inconsistent stage state — ${activeCount} ACTIVE stages for case`);
  }
}

interface StageNodeProps {
  stage: StageView;
  isFocused: boolean;
  layout: LayoutMode;
  onFocus: () => void;
}

/**
 * One node + (optional) connector + label. Wraps in a Popover so hover and Enter/Space open the
 * same popover content. The trigger is a `<button>` for native focus + activation semantics.
 */
const StageNode = forwardRef<HTMLButtonElement, StageNodeProps>(function StageNode(
  { stage, isFocused, layout, onFocus },
  ref,
) {
  const cls = stateClass(stage.state);
  const inline = inlineTimestamp(stage);
  const extras = popoverExtras(stage);
  const ts = stage.enteredAt ?? stage.exitedAt;

  // Hover delay (AC7): Radix Popover doesn't support hover-to-open natively; we manage `open`
  // ourselves with a delayed timer. Click / Enter / Space toggle the sticky popover.
  const [open, setOpen] = useState(false);
  const [sticky, setSticky] = useState(false);
  const hoverTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const cancelHoverTimer = () => {
    if (hoverTimer.current) {
      clearTimeout(hoverTimer.current);
      hoverTimer.current = null;
    }
  };

  const onPointerEnter = () => {
    if (sticky) return;
    cancelHoverTimer();
    hoverTimer.current = setTimeout(() => setOpen(true), HOVER_DELAY_MS);
  };
  const onPointerLeave = () => {
    cancelHoverTimer();
    if (!sticky) setOpen(false);
  };

  useEffect(() => () => cancelHoverTimer(), []);

  const toggleSticky = () => {
    setSticky((s) => {
      const next = !s;
      setOpen(next);
      return next;
    });
  };

  return (
    <li
      role="listitem"
      aria-current={stage.state === 'ACTIVE' ? 'step' : undefined}
      data-stage-state={cls}
      data-stage-id={stage.stageId}
      className={cn(
        'wks-stage-item',
        layout === 'horizontal'
          ? 'flex flex-col items-center text-center min-w-[120px] max-w-[200px] flex-1'
          : 'flex flex-row items-start gap-3 py-2',
      )}
    >
      <Popover
        open={open}
        onOpenChange={(next) => {
          setOpen(next);
          if (!next) setSticky(false);
        }}
      >
        <PopoverTrigger asChild>
          <button
            ref={ref}
            type="button"
            tabIndex={isFocused ? 0 : -1}
            onFocus={onFocus}
            onPointerEnter={onPointerEnter}
            onPointerLeave={onPointerLeave}
            onClick={(e) => {
              // Block the Radix default (which would open without sticky tracking) so we can
              // manage open + sticky in one place.
              e.preventDefault();
              toggleSticky();
            }}
            data-state={cls}
            aria-label={t('stageTimeline.aria.itemSummary', {
              ordinal: String(stage.ordinal + 1),
              name: stage.displayName,
              state: stateLabel(stage.state),
            })}
            className={cn(
              'wks-stage-node relative inline-flex shrink-0 rounded-full border-2 transition-colors',
              'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ring)] focus-visible:ring-offset-4',
              stage.state === 'COMPLETED' && 'size-3 border-[var(--success)] bg-[var(--success)]',
              stage.state === 'ACTIVE' &&
                'size-3.5 border-[var(--primary)] bg-[var(--primary)] shadow-[0_0_0_4px_color-mix(in_srgb,var(--primary)_30%,transparent)] motion-safe:animate-pulse',
              stage.state === 'SKIPPED' && 'size-3 border-[var(--border)] bg-transparent',
              stage.state === 'PENDING' && 'size-3 border-[var(--border)] bg-transparent',
            )}
          />
        </PopoverTrigger>
        <PopoverContent className="max-w-xs space-y-1 text-sm" align="center">
          <div className="font-medium">{stage.displayName}</div>
          <div className="text-[var(--muted-foreground)]">{stateLabel(stage.state)}</div>
          {ts && <div className="tabular-nums text-xs">{formatDateTime(ts)}</div>}
          {extras && <div className="text-xs text-[var(--muted-foreground)]">{extras}</div>}
        </PopoverContent>
      </Popover>

      <div
        className={cn(
          'wks-stage-meta',
          layout === 'horizontal' ? 'mt-2 px-1' : 'flex-1',
          stage.state === 'PENDING' && 'opacity-60',
          stage.state === 'SKIPPED' && 'italic opacity-70',
        )}
      >
        <div className="text-sm font-medium leading-tight">{stage.displayName}</div>
        {inline && (
          <div className="mt-0.5 text-xs tabular-nums text-[var(--muted-foreground)]">{inline}</div>
        )}
      </div>
    </li>
  );
});

interface ConnectorProps {
  /** State of the stage *to the left* of the connector (horizontal) or above it (vertical). */
  fromState: StageState;
  /** State of the stage *to the right* of the connector (horizontal) or below it (vertical). */
  toState: StageState;
  layout: LayoutMode;
}

/**
 * Connector between two stage nodes. Rule (AC1, `ux-stage-timeline.md` §2):
 * - both completed → solid 2px to next
 * - skipped boundary → dashed in-edge / solid out-edge — meaning if one of {from, to} is SKIPPED,
 *   the connector touching the skipped node is dashed; the other half stays solid.
 * - pending → solid 1.5px neutral
 *
 * Implementation: render a single line, but split visually only at skip boundaries. To keep DOM
 * small we use a 2-cell flex (left half + right half) and toggle dashed-ness per side.
 */
function Connector({ fromState, toState, layout }: ConnectorProps) {
  const fromDashed = fromState === 'SKIPPED';
  const toDashed = toState === 'SKIPPED';
  const isPending = fromState === 'PENDING' && toState === 'PENDING';
  const thickness = isPending ? 'border-[1.5px]' : 'border-2';
  const color = isPending ? 'border-[var(--border)]' : 'border-[var(--success)]';

  if (layout === 'horizontal') {
    return (
      <div aria-hidden className="flex h-0 flex-1 items-center self-start mt-[6px]">
        <div
          className={cn(
            'h-0 w-1/2 border-t',
            thickness,
            color,
            fromDashed && 'border-dashed border-[var(--border)]',
          )}
        />
        <div
          className={cn(
            'h-0 w-1/2 border-t',
            thickness,
            color,
            toDashed && 'border-dashed border-[var(--border)]',
          )}
        />
      </div>
    );
  }

  // Vertical stepper rail.
  return (
    <div aria-hidden className="ml-[5px] flex w-0 flex-col items-stretch self-start">
      <div
        className={cn(
          'h-3 w-0 border-l',
          thickness,
          color,
          fromDashed && 'border-dashed border-[var(--border)]',
        )}
      />
      <div
        className={cn(
          'h-3 w-0 border-l',
          thickness,
          color,
          toDashed && 'border-dashed border-[var(--border)]',
        )}
      />
    </div>
  );
}

/** Build the announcement text for the live region. */
function announcement(stages: StageView[]): string {
  const summary = stages
    .map((s, i) =>
      t('stageTimeline.aria.itemSummary', {
        ordinal: String(i + 1),
        name: s.displayName,
        state: stateLabel(s.state),
      }),
    )
    .join('. ');
  return t('stageTimeline.aria.announcement', {
    n: String(stages.length),
    summary,
  });
}

export function StageTimeline({
  stages,
  caseTypeStageDefs,
  experimental_animate = false,
}: StageTimelineProps) {
  void caseTypeStageDefs; // reserved for fallback; current backend always populates displayName
  void experimental_animate; // wired by Story 4.3 — Phase 0 keeps animation a no-op

  // Defensive observability — emit before any layout work runs.
  warnIfInconsistent(stages);

  const { ref: paneRef, width } = usePaneWidth();
  const safeLength = stages?.length ?? 0;
  const layout: LayoutMode = useMemo(() => {
    if (safeLength > MAX_HORIZONTAL_STAGES) return 'vertical';
    if (width === null) return 'horizontal'; // pre-measure default
    return width >= HORIZONTAL_LAYOUT_MIN_WIDTH ? 'horizontal' : 'vertical';
  }, [safeLength, width]);

  // Roving tab-index — single composite-widget tab stop. ArrowKeys / Home / End move focus
  // between nodes within the timeline; Tab moves focus *out* (because non-focused nodes have
  // tabIndex=-1). Initial focus index is the active stage if any, else 0.
  const initialIdx = useMemo(() => {
    if (!stages) return 0;
    const activeIdx = stages.findIndex((s) => s.state === 'ACTIVE');
    return activeIdx >= 0 ? activeIdx : 0;
  }, [stages]);
  const [focusIdx, setFocusIdx] = useState(initialIdx);
  const nodeRefs = useRef<Array<HTMLButtonElement | null>>([]);

  // Keep focusIdx in sync if stages array length changes (e.g. SSE-driven update from 4.3).
  useLayoutEffect(() => {
    if (focusIdx >= safeLength) setFocusIdx(Math.max(0, safeLength - 1));
  }, [safeLength, focusIdx]);

  const moveFocus = useCallback(
    (next: number) => {
      const clamped = Math.max(0, Math.min(safeLength - 1, next));
      setFocusIdx(clamped);
      const node = nodeRefs.current[clamped];
      node?.focus();
    },
    [safeLength],
  );

  const onKeyDown = useCallback(
    (event: ReactKeyboardEvent<HTMLElement>) => {
      switch (event.key) {
        case 'ArrowRight':
        case 'ArrowDown':
          event.preventDefault();
          moveFocus(focusIdx + 1);
          break;
        case 'ArrowLeft':
        case 'ArrowUp':
          event.preventDefault();
          moveFocus(focusIdx - 1);
          break;
        case 'Home':
          event.preventDefault();
          moveFocus(0);
          break;
        case 'End':
          event.preventDefault();
          moveFocus(safeLength - 1);
          break;
        default:
          break;
      }
    },
    [focusIdx, moveFocus, safeLength],
  );

  // AC2 / AC11 — empty stages array → component returns null. No DOM, no skeleton, no placeholder.
  // Placed AFTER hooks so React's hook ordering invariants stay intact across re-renders.
  if (!stages || stages.length === 0) {
    return null;
  }

  return (
    <nav
      ref={paneRef}
      aria-label={t('stageTimeline.aria.label')}
      data-layout={layout}
      className={cn('wks-stage-timeline w-full', layout === 'horizontal' ? 'overflow-x-auto' : '')}
    >
      <ol
        role="list"
        aria-label={t('stageTimeline.aria.list')}
        onKeyDown={onKeyDown}
        className={cn(
          'flex',
          layout === 'horizontal'
            ? 'flex-row items-stretch gap-0 px-1 py-2'
            : 'flex-col gap-0 py-2',
        )}
      >
        {stages.map((stage, i) => (
          <Slot
            key={stage.stageId}
            isLast={i === stages.length - 1}
            connector={
              i < stages.length - 1 ? (
                <Connector fromState={stage.state} toState={stages[i + 1]!.state} layout={layout} />
              ) : null
            }
            layout={layout}
          >
            <StageNode
              ref={(node) => {
                nodeRefs.current[i] = node;
              }}
              stage={stage}
              isFocused={i === focusIdx}
              layout={layout}
              onFocus={() => setFocusIdx(i)}
            />
          </Slot>
        ))}
      </ol>
      {/* aria-live region for screen-reader announcement on mount + on stages change. */}
      <span aria-live="polite" className="sr-only">
        {announcement(stages)}
      </span>
    </nav>
  );
}

/**
 * Layout glue between an item and its trailing connector. Keeps the rendering loop tidy and
 * makes the `Connector` placement test-friendly (the connector always appears as a sibling of the
 * stage node in the DOM, not nested inside it).
 */
function Slot({
  children,
  connector,
  layout,
  isLast,
}: {
  children: React.ReactNode;
  connector: React.ReactNode;
  layout: LayoutMode;
  isLast: boolean;
}) {
  if (layout === 'horizontal') {
    return (
      <>
        {children}
        {!isLast && connector}
      </>
    );
  }
  // Vertical: connector renders below the node visually; placed after the <li>.
  return (
    <>
      {children}
      {!isLast && connector}
    </>
  );
}
