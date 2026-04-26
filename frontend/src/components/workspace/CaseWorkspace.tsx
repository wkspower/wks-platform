import {
  cloneElement,
  isValidElement,
  useEffect,
  useMemo,
  useRef,
  type ReactElement,
  type ReactNode,
} from 'react';

import { Button } from '@/components/ui/Button';
import { useViewport } from '@/hooks/useViewport';
import { t } from '@/i18n';
import { cn } from '@/lib/cn';
import {
  MIN_VIEWPORT_FOR_DETAIL,
  MIN_VIEWPORT_WHEN_SIDEBAR_COLLAPSED,
} from '@/lib/layoutBreakpoints';
import { useUiStore } from '@/stores/uiStore';
import type { CaseRow } from '@/types/case';

import { CaseDetailPanel } from './CaseDetailPanel';

export interface CaseWorkspaceProps {
  filterBar: ReactNode;
  list: ReactNode;
  selectedCaseId: string | null;
  onSelectionChange: (id: string | null) => void;
  sortedRows: CaseRow[];
}

function viewportAllowsDetail(width: number, sidebarCollapsed: boolean): boolean {
  return sidebarCollapsed
    ? width >= MIN_VIEWPORT_WHEN_SIDEBAR_COLLAPSED
    : width >= MIN_VIEWPORT_FOR_DETAIL;
}

function focusListRow(rowId: string): boolean {
  if (typeof document === 'undefined') return false;
  const el = document.querySelector<HTMLElement>(`[data-row-id="${CSS.escape(rowId)}"]`);
  if (!el) return false;
  el.focus();
  return true;
}

function focusFirstListRow(): void {
  if (typeof document === 'undefined') return;
  const first = document.querySelector<HTMLElement>('[data-row-id]');
  if (first) {
    first.focus();
    return;
  }
  // AC7 fallback: when the list has no rows, focus the workspace heading so screen-reader
  // users land on a meaningful landmark instead of document.body.
  const heading = document.querySelector<HTMLElement>('[data-workspace-heading]');
  heading?.focus();
}

/**
 * Returns true when the keydown originated inside a Radix overlay (Popover, DropdownMenu,
 * Tooltip portal) or inside a `role="menu" | "listbox"` widget. The workspace-level Esc/J/K
 * handler must yield to those overlays so:
 *
 * - Esc closes the overlay first instead of jumping straight to closing the detail panel.
 * - J/K typed inside an open menu stays within the menu's typeahead instead of stepping the
 *   case selection out from under the user.
 */
function isInsideOverlay(target: HTMLElement | null): boolean {
  // `target` may be the Document node itself (synthetic keydowns dispatched against
  // `document` arrive that way in jsdom) — in which case `closest` is unavailable.
  if (!target || typeof target.closest !== 'function') return false;
  return Boolean(
    target.closest(
      '[data-radix-popper-content-wrapper],[role="menu"],[role="menuitem"],[role="listbox"],[role="option"],[role="dialog"]',
    ),
  );
}

export function CaseWorkspace({
  filterBar,
  list,
  selectedCaseId,
  onSelectionChange,
  sortedRows,
}: CaseWorkspaceProps) {
  const viewport = useViewport();
  const sidebarCollapsed = useUiStore((s) => s.sidebarCollapsed);
  const setSidebarCollapsed = useUiStore((s) => s.setSidebarCollapsed);

  const allowsDetail = viewportAllowsDetail(viewport.width, sidebarCollapsed);
  const showDetail = selectedCaseId !== null && allowsDetail;
  const showAutoCloseBanner = selectedCaseId !== null && !allowsDetail;
  const variant: 'full-width' | 'narrowed' = showDetail ? 'narrowed' : 'full-width';

  const previousSelectionRef = useRef<string | null>(selectedCaseId);

  // Focus restoration on close: when selection transitions from set → null, focus the previously
  // selected row if it is still in the DOM. Falls back to the first row, then the workspace heading.
  useEffect(() => {
    const prev = previousSelectionRef.current;
    if (prev !== null && selectedCaseId === null) {
      requestAnimationFrame(() => {
        if (!focusListRow(prev)) {
          focusFirstListRow();
        }
      });
    }
    previousSelectionRef.current = selectedCaseId;
  }, [selectedCaseId]);

  // Workspace-level keyboard handler for J / K / Escape.
  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if (event.isComposing) return;
      if (event.defaultPrevented) return;
      const target = event.target as HTMLElement | null;
      if (target) {
        const tag = target.tagName;
        if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT' || target.isContentEditable) {
          return;
        }
        if (isInsideOverlay(target)) return;
      }
      if (event.metaKey || event.ctrlKey || event.altKey) return;

      if (event.key === 'Escape' && selectedCaseId !== null) {
        event.preventDefault();
        onSelectionChange(null);
        return;
      }

      if (
        (event.key === 'j' || event.key === 'k' || event.key === 'J' || event.key === 'K') &&
        selectedCaseId !== null
      ) {
        const idx = sortedRows.findIndex((r) => r.id === selectedCaseId);
        if (idx === -1) return;
        const dir = event.key === 'j' || event.key === 'J' ? 1 : -1;
        const next = idx + dir;
        if (next < 0 || next >= sortedRows.length) {
          event.preventDefault();
          return;
        }
        event.preventDefault();
        const nextRow = sortedRows[next];
        if (!nextRow) return;
        onSelectionChange(nextRow.id);
        requestAnimationFrame(() => {
          const el = document.querySelector<HTMLElement>(
            `[data-row-id="${CSS.escape(nextRow.id)}"]`,
          );
          el?.scrollIntoView({ block: 'nearest' });
        });
      }
    }
    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [selectedCaseId, sortedRows, onSelectionChange]);

  const handleRowSelect = (row: CaseRow) => {
    const willFit = viewportAllowsDetail(viewport.width, sidebarCollapsed);
    if (!willFit && !sidebarCollapsed) {
      setSidebarCollapsed(true);
    }
    onSelectionChange(row.id);
  };

  const wrappedFilterBar = useMemo(() => {
    if (!isValidElement(filterBar)) return filterBar;
    return cloneElement(filterBar as ReactElement<{ variant?: string }>, { variant });
  }, [filterBar, variant]);

  const wrappedList = useMemo(() => {
    if (!isValidElement(list)) return list;
    const props: Record<string, unknown> = {
      variant,
      onRowSelect: handleRowSelect,
    };
    if (variant === 'narrowed') {
      props.hiddenColumnIds = ['caseType'];
    }
    return cloneElement(list as ReactElement<Record<string, unknown>>, props);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [list, variant, viewport.width, sidebarCollapsed, selectedCaseId]);

  // Re-open is only meaningful when collapsing the sidebar would actually free enough room.
  // When the sidebar is already collapsed and the viewport is still below the detail threshold,
  // the click is a no-op — disable the button rather than ship a dead control.
  const reopenEnabled = !sidebarCollapsed;

  return (
    <div
      role="region"
      aria-label={t('workspace.label')}
      className={cn(
        'flex flex-1 flex-col transition-none',
        showDetail ? 'workspace-list-and-detail' : 'workspace-list-only',
      )}
    >
      {/*
        AC7 — workspace heading is always rendered (visually hidden when the list is the only
        landmark) so the focus-restoration fallback in `focusFirstListRow` has a target when no
        rows are in the DOM. CasesPage still renders its visible `<h1>` in list-only mode.
      */}
      <h1 data-workspace-heading tabIndex={-1} className="sr-only focus-visible:outline-none">
        {t('workspace.label')}
      </h1>
      {wrappedFilterBar}
      {showAutoCloseBanner ? (
        <div
          role="status"
          className="mt-2 flex items-center justify-between rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--muted)] px-3 py-2 text-sm"
        >
          <span>{t('workspace.detailHidden')}</span>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setSidebarCollapsed(true)}
            disabled={!reopenEnabled}
          >
            {t('workspace.reopen')}
          </Button>
        </div>
      ) : null}
      <div className="mt-2 flex flex-1 gap-4">
        <div
          className={cn(
            'flex flex-col',
            showDetail ? 'w-[380px] min-w-[380px] shrink-0' : 'flex-1',
          )}
        >
          {wrappedList}
        </div>
        {showDetail && selectedCaseId ? (
          <CaseDetailPanel caseId={selectedCaseId} onClose={() => onSelectionChange(null)} />
        ) : null}
      </div>
    </div>
  );
}
