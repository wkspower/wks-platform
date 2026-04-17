import { create } from 'zustand';

const STORAGE_KEY = 'wks.ui.sidebar.collapsed';

function readCollapsed(): boolean {
  try {
    return localStorage.getItem(STORAGE_KEY) === 'true';
  } catch {
    return false;
  }
}

function writeCollapsed(value: boolean): void {
  try {
    localStorage.setItem(STORAGE_KEY, value ? 'true' : 'false');
  } catch {
    // Ignore — Safari private mode etc. State stays in memory only.
  }
}

export interface UiState {
  sidebarCollapsed: boolean;
  toggleSidebar: () => void;
  setSidebarCollapsed: (value: boolean) => void;
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
}));
