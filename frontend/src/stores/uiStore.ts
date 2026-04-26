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
}));
