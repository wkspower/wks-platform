import {
  ChevronsLeft,
  ChevronsRight,
  FileText,
  Inbox,
  LayoutGrid,
  Settings,
  Sparkles,
} from 'lucide-react';
import { NavLink } from 'react-router-dom';

import { Avatar } from '@/components/ui/Avatar';
import { IconButton } from '@/components/ui/IconButton';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/Tooltip';
import { cn } from '@/lib/cn';
import { useAuthStore } from '@/stores/authStore';
import { useUiStore } from '@/stores/uiStore';

interface NavEntry {
  to: string;
  label: string;
  Icon: typeof FileText;
  roles?: string[];
}

const items: NavEntry[] = [
  { to: '/dashboard', label: 'Dashboard', Icon: LayoutGrid },
  { to: '/cases', label: 'Cases', Icon: FileText },
  { to: '/tasks', label: 'My tasks', Icon: Inbox },
  { to: '/admin', label: 'Admin', Icon: Settings, roles: ['admin'] },
];

export function Sidebar() {
  const collapsed = useUiStore((s) => s.sidebarCollapsed);
  const toggle = useUiStore((s) => s.toggleSidebar);
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);

  const visible = items.filter((i) => !i.roles || i.roles.some((r) => user?.roles.includes(r)));

  return (
    <nav className="flex h-full flex-col text-sidebar-fg" aria-label="Main navigation">
      {/* Workspace switcher */}
      <div className="flex h-12 items-center gap-2 px-3 border-b border-border">
        <div
          className="grid size-7 place-items-center rounded-md text-white font-semibold text-[12px]"
          style={{ background: 'linear-gradient(135deg, var(--primary), var(--secondary))' }}
        >
          W
        </div>
        {!collapsed && (
          <div className="flex-1 min-w-0">
            <div className="font-heading font-semibold text-[13px] truncate">WKS Platform</div>
            <div className="text-[11px] text-foreground-subtle truncate">Workspace</div>
          </div>
        )}
      </div>

      {/* Nav items */}
      <ul className="flex-1 px-2 py-2 space-y-0.5">
        {visible.map(({ to, label, Icon }) => (
          <li key={to}>
            <NavLink
              to={to}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-2 rounded-md px-2 h-8 text-[13px] transition-colors',
                  'hover:bg-sidebar-item-hover',
                  isActive && 'bg-sidebar-item-active text-sidebar-fg-active font-medium',
                  collapsed && 'justify-center',
                )
              }
            >
              <Icon className="size-4 shrink-0" />
              {!collapsed && <span className="truncate">{label}</span>}
            </NavLink>
          </li>
        ))}
      </ul>

      {/* AI hint (Attio-style) */}
      {!collapsed && (
        <div className="mx-2 mb-2 rounded-md border border-border bg-surface p-2.5">
          <div className="flex items-center gap-1.5 text-[12px] font-medium">
            <Sparkles className="size-3.5 text-[var(--primary)]" />
            Quick tip
          </div>
          <div className="text-[11px] text-foreground-muted mt-1">
            Press <kbd className="rounded border border-border bg-surface-hover px-1">⌘K</kbd> to search anywhere.
          </div>
        </div>
      )}

      {/* User footer */}
      <div className="border-t border-border px-2 py-2">
        <div className={cn('flex items-center gap-2 px-1', collapsed && 'justify-center')}>
          <Avatar name={user?.email ?? null} size="md" />
          {!collapsed && (
            <div className="flex-1 min-w-0">
              <div className="text-[12px] truncate">{user?.email}</div>
              <button
                type="button"
                onClick={() => void logout()}
                className="text-[11px] text-foreground-subtle hover:text-foreground"
              >
                Sign out
              </button>
            </div>
          )}
        </div>
        <Tooltip>
          <TooltipTrigger asChild>
            <IconButton onClick={toggle} className="mt-2 mx-auto block" aria-label="Toggle sidebar">
              {collapsed ? <ChevronsRight className="size-4" /> : <ChevronsLeft className="size-4" />}
            </IconButton>
          </TooltipTrigger>
          <TooltipContent side="right">{collapsed ? 'Expand' : 'Collapse'}</TooltipContent>
        </Tooltip>
      </div>
    </nav>
  );
}
