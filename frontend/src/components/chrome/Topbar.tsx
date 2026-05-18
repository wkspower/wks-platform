import { Bell, Plus, Search } from 'lucide-react';
import { useLocation, useMatch } from 'react-router-dom';

import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { IconButton } from '@/components/ui/IconButton';
import { useAuthStore } from '@/stores/authStore';

function useBreadcrumbs(): { label: string; href?: string }[] {
  const { pathname } = useLocation();
  const caseMatch = useMatch('/cases/:caseId');
  const formMatch = useMatch('/cases/:caseId/forms/:formId');

  if (formMatch)
    return [
      { label: 'Cases', href: '/cases' },
      { label: formMatch.params.caseId ?? '', href: `/cases/${formMatch.params.caseId}` },
      { label: 'Form' },
    ];
  if (caseMatch)
    return [
      { label: 'Cases', href: '/cases' },
      { label: caseMatch.params.caseId ?? '' },
    ];
  if (pathname.startsWith('/cases')) return [{ label: 'Cases' }];
  if (pathname.startsWith('/tasks')) return [{ label: 'My tasks' }];
  if (pathname.startsWith('/admin/license')) return [{ label: 'Admin', href: '/admin' }, { label: 'License' }];
  if (pathname.startsWith('/admin/mapping-inspector'))
    return [{ label: 'Admin', href: '/admin' }, { label: 'Mapping inspector' }];
  if (pathname.startsWith('/admin')) return [{ label: 'Admin' }];
  if (pathname.startsWith('/dashboard')) return [{ label: 'Dashboard' }];
  return [];
}

export function Topbar({ onOpenPalette }: { onOpenPalette: () => void }) {
  const crumbs = useBreadcrumbs();
  const user = useAuthStore((s) => s.user);

  return (
    <header className="flex h-full items-center justify-between px-3 gap-3">
      <nav className="flex items-center gap-1 text-[13px] min-w-0 overflow-hidden">
        {crumbs.map((c, i) => (
          <span key={i} className="flex items-center gap-1 min-w-0">
            {i > 0 && <span className="text-foreground-subtle">/</span>}
            {c.href ? (
              <a href={c.href} className="text-foreground-muted hover:text-foreground truncate max-w-[200px]">
                {c.label}
              </a>
            ) : (
              <span className="font-medium truncate max-w-[280px]">{c.label}</span>
            )}
          </span>
        ))}
      </nav>

      <div className="flex items-center gap-1.5">
        <button
          type="button"
          onClick={onOpenPalette}
          className="hidden md:inline-flex items-center gap-2 h-7 px-2.5 rounded-md border border-border bg-surface hover:bg-surface-hover text-[12px] text-foreground-muted"
        >
          <Search className="size-3.5" />
          <span>Search…</span>
          <kbd className="ml-3 rounded bg-surface-hover px-1 text-[10px] text-foreground-subtle">⌘K</kbd>
        </button>
        <IconButton aria-label="Notifications">
          <Bell className="size-4" />
        </IconButton>
        <Button size="sm" variant="primary" className="hidden sm:inline-flex">
          <Plus className="size-3.5" /> New
        </Button>
        <Avatar name={user?.email ?? null} size="md" className="ml-1" />
      </div>
    </header>
  );
}
