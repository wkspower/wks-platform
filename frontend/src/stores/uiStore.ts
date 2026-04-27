import { create } from 'zustand';

import type { Priority } from '@/types/priority';

const SIDEBAR_KEY = 'wks.ui.sidebar.collapsed';
const FILTERS_KEY = 'wks.ui.cases.filters';

function readCollapsed(): boolean {
  if (typeof window === 'undefined') return false;
  try {
    return window.localStorage.getItem(SIDEBAR_KEY) === 'true';
  } catch {
    return false;
  }
}

function writeCollapsed(value: boolean): void {
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.setItem(SIDEBAR_KEY, value ? 'true' : 'false');
  } catch {
    // Ignore — Safari private mode etc. State stays in memory only.
  }
}

export interface CaseListFilters {
  caseTypeIds: string[];
  statusIds: string[];
  priorities: Priority[];
}

const EMPTY_FILTERS: CaseListFilters = { caseTypeIds: [], statusIds: [], priorities: [] };

function readFilters(): CaseListFilters {
  if (typeof window === 'undefined') return EMPTY_FILTERS;
  try {
    const raw = window.localStorage.getItem(FILTERS_KEY);
    if (!raw) return EMPTY_FILTERS;
    const parsed = JSON.parse(raw) as Partial<CaseListFilters>;
    return {
      caseTypeIds: Array.isArray(parsed.caseTypeIds) ? parsed.caseTypeIds : [],
      statusIds: Array.isArray(parsed.statusIds) ? parsed.statusIds : [],
      priorities: Array.isArray(parsed.priorities) ? (parsed.priorities as Priority[]) : [],
    };
  } catch {
    return EMPTY_FILTERS;
  }
}

function writeFilters(value: CaseListFilters): void {
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.setItem(FILTERS_KEY, JSON.stringify(value));
  } catch {
    // Ignore — same Safari private-mode rationale as above.
  }
}

export interface UiState {
  sidebarCollapsed: boolean;
  toggleSidebar: () => void;
  setSidebarCollapsed: (value: boolean) => void;

  caseListFilters: CaseListFilters;
  setCaseListFilters: (next: CaseListFilters) => void;
  clearCaseListFilters: () => void;

  /**
   * Story 2.7 AC8 — case ids freshly created in this session. The case-list row uses this to
   * paint a {@code border-l-3 border-primary} highlight + an aria-live announcement (one-shot
   * per id). Each id auto-clears 6 seconds after push; explicit clear is supported for tests.
   */
  recentlyCreatedCaseIds: ReadonlySet<string>;
  pushRecentlyCreated: (id: string) => void;
  clearRecentlyCreated: (id: string) => void;
}

const RECENTLY_CREATED_TTL_MS = 6_000;

// P19 — track per-id timer handles so we can cancel pending expiries on explicit clear,
// store reset, or page visibility change. Without this, N pending timers stack up under bulk
// creates and SSR-like environments leak ids forever. Uses `number` (DOM `setTimeout` return
// type) since the timer is scheduled via `window.setTimeout`.
const recentlyCreatedTimers = new Map<string, number>();

function cancelRecentlyCreatedTimer(id: string): void {
  const handle = recentlyCreatedTimers.get(id);
  if (handle !== undefined) {
    clearTimeout(handle);
    recentlyCreatedTimers.delete(id);
  }
}

export const useUiStore = create<UiState>((set, get) => ({
  sidebarCollapsed: readCollapsed(),
  toggleSidebar() {
    const next = !get().sidebarCollapsed;
    writeCollapsed(next);
    set({ sidebarCollapsed: next });
  },
  setSidebarCollapsed(value) {
    writeCollapsed(value);
    set({ sidebarCollapsed: value });
  },

  caseListFilters: readFilters(),
  setCaseListFilters(next) {
    writeFilters(next);
    set({ caseListFilters: next });
  },
  clearCaseListFilters() {
    writeFilters(EMPTY_FILTERS);
    set({ caseListFilters: EMPTY_FILTERS });
  },

  recentlyCreatedCaseIds: new Set<string>(),
  pushRecentlyCreated(id) {
    const current = get().recentlyCreatedCaseIds;
    if (current.has(id)) return;
    const next = new Set(current);
    next.add(id);
    set({ recentlyCreatedCaseIds: next });
    if (typeof window === 'undefined') return;
    cancelRecentlyCreatedTimer(id);
    const handle = window.setTimeout(() => {
      recentlyCreatedTimers.delete(id);
      const after = get().recentlyCreatedCaseIds;
      if (!after.has(id)) return;
      const reduced = new Set(after);
      reduced.delete(id);
      set({ recentlyCreatedCaseIds: reduced });
    }, RECENTLY_CREATED_TTL_MS);
    recentlyCreatedTimers.set(id, handle);
  },
  clearRecentlyCreated(id) {
    cancelRecentlyCreatedTimer(id);
    const current = get().recentlyCreatedCaseIds;
    if (!current.has(id)) return;
    const next = new Set(current);
    next.delete(id);
    set({ recentlyCreatedCaseIds: next });
  },
}));

// Drop pending highlight timers when the tab is hidden — leaving them alive risks announcing or
// flashing rows on a page the user can't see, and the highlight is a freshness affordance that
// loses meaning across a backgrounded session.
if (typeof document !== 'undefined') {
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState !== 'hidden') return;
    for (const id of Array.from(recentlyCreatedTimers.keys())) {
      useUiStore.getState().clearRecentlyCreated(id);
    }
  });
}
