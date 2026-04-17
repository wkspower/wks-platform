import { beforeEach, describe, expect, it } from 'vitest';

import { useUiStore } from './uiStore';

describe('uiStore', () => {
  beforeEach(() => {
    localStorage.clear();
    useUiStore.setState({ sidebarCollapsed: false });
  });

  it('toggleSidebar flips the boolean and persists to localStorage', () => {
    useUiStore.getState().toggleSidebar();
    expect(useUiStore.getState().sidebarCollapsed).toBe(true);
    expect(localStorage.getItem('wks.ui.sidebar.collapsed')).toBe('true');
    useUiStore.getState().toggleSidebar();
    expect(useUiStore.getState().sidebarCollapsed).toBe(false);
    expect(localStorage.getItem('wks.ui.sidebar.collapsed')).toBe('false');
  });

  it('setSidebarCollapsed writes the literal value', () => {
    useUiStore.getState().setSidebarCollapsed(true);
    expect(localStorage.getItem('wks.ui.sidebar.collapsed')).toBe('true');
  });
});
