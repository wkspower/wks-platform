import { Outlet } from 'react-router-dom';
import { useEffect, useState } from 'react';

import { CommandPalette } from './CommandPalette';
import { Sidebar } from './Sidebar';
import { Topbar } from './Topbar';
import { ToastViewport } from '@/components/ui/Toaster';
import { useUiStore } from '@/stores/uiStore';

export function AppShell() {
  const sidebarCollapsed = useUiStore((s) => s.sidebarCollapsed);
  const [paletteOpen, setPaletteOpen] = useState(false);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setPaletteOpen((v) => !v);
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, []);

  return (
    <div
      className="grid h-screen overflow-hidden"
      style={{
        gridTemplateColumns: `${sidebarCollapsed ? 'var(--sidebar-w-collapsed)' : 'var(--sidebar-w)'} 1fr`,
        gridTemplateRows: 'var(--topbar-h) 1fr',
      }}
    >
      <div className="row-span-2 border-r border-border bg-sidebar-bg">
        <Sidebar />
      </div>
      <div className="border-b border-border bg-surface">
        <Topbar onOpenPalette={() => setPaletteOpen(true)} />
      </div>
      <main className="overflow-auto bg-background">
        <Outlet />
      </main>
      <CommandPalette open={paletteOpen} onOpenChange={setPaletteOpen} />
      <ToastViewport />
    </div>
  );
}
