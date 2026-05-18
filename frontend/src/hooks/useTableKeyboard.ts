import { useEffect } from 'react';

export interface TableKeyboardHandlers {
  /** True when an inline editor or modal is active — handler short-circuits. */
  disabled?: boolean;
  /** True when the drawer is open — Esc closes it instead of clearing selection. */
  drawerOpen?: boolean;
  onNext: () => void;
  onPrev: () => void;
  onOpen: () => void;
  onStartEdit: () => void;
  onToggleSelect: () => void;
  onRangeSelect: () => void;
  onFocusSearch: () => void;
  onCloseDrawer: () => void;
  onClearSelection: () => void;
  onShowShortcuts: () => void;
}

function isEditableTarget(target: EventTarget | null): boolean {
  if (!(target instanceof HTMLElement)) return false;
  const tag = target.tagName;
  if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return true;
  if (target.isContentEditable) return true;
  return false;
}

export function useTableKeyboard(h: TableKeyboardHandlers): void {
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (h.disabled) return;
      if (e.isComposing) return;

      // The `/` key focuses search even when typing isn't happening in an input.
      // All other shortcuts skip if focus is in an editable target.
      const editable = isEditableTarget(e.target);

      // `/` — focus search (works from anywhere except editable targets)
      if (e.key === '/' && !editable && !e.metaKey && !e.ctrlKey) {
        e.preventDefault();
        h.onFocusSearch();
        return;
      }

      // `?` — shortcuts overlay
      if (e.key === '?' && !editable) {
        e.preventDefault();
        h.onShowShortcuts();
        return;
      }

      // Esc — drawer close has priority, then clear selection. Works even in inputs (drawer).
      if (e.key === 'Escape') {
        if (h.drawerOpen) {
          h.onCloseDrawer();
          return;
        }
        if (editable) return;
        h.onClearSelection();
        return;
      }

      if (editable) return;

      switch (e.key) {
        case 'j':
        case 'J':
        case 'ArrowDown':
          e.preventDefault();
          h.onNext();
          return;
        case 'k':
        case 'K':
        case 'ArrowUp':
          e.preventDefault();
          h.onPrev();
          return;
        case 'Enter':
          e.preventDefault();
          h.onOpen();
          return;
        case 'e':
        case 'E':
          e.preventDefault();
          h.onStartEdit();
          return;
        case 'x':
          if (e.shiftKey) {
            e.preventDefault();
            h.onRangeSelect();
          } else {
            e.preventDefault();
            h.onToggleSelect();
          }
          return;
        case 'X':
          e.preventDefault();
          h.onRangeSelect();
          return;
      }
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [h]);
}
