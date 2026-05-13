import * as Tooltip from '@radix-ui/react-tooltip';
import { cva } from 'class-variance-authority';
import {
  ChevronLeft,
  ChevronRight,
  FolderKanban,
  ListTodo,
  Settings,
  Terminal,
} from 'lucide-react';
import type { ComponentType, SVGProps } from 'react';
import { NavLink, useLocation } from 'react-router-dom';

import { Button } from '@/components/ui/Button';
import { t } from '@/i18n';
import { cn } from '@/lib/cn';
import { useAuthStore } from '@/stores/authStore';
import { useUiStore } from '@/stores/uiStore';
import type { AuthUser } from '@/types/auth';

interface NavItemSpec {
  to: string;
  labelKey: string;
  icon: ComponentType<SVGProps<SVGSVGElement>>;
  visible?: (user: AuthUser | null) => boolean;
}

function hasDeveloperAccess(user: AuthUser | null): boolean {
  return Boolean(user?.roles.includes('developer'));
}

const NAV_ITEMS: NavItemSpec[] = [
  { to: '/cases', labelKey: 'nav.cases', icon: FolderKanban },
  { to: '/tasks', labelKey: 'nav.tasks', icon: ListTodo },
  {
    to: '/admin',
    labelKey: 'nav.admin',
    icon: Settings,
    visible: (u) => Boolean(u?.roles.includes('admin')),
  },
  { to: '/dev', labelKey: 'nav.developer', icon: Terminal, visible: hasDeveloperAccess },
];

const itemVariants = cva(
  'group flex items-center gap-3 rounded-[var(--radius-md)] px-3 py-2 text-sm font-medium transition-colors duration-[var(--motion-fast)] outline-none focus-visible:ring-2 focus-visible:ring-[var(--secondary)] focus-visible:ring-offset-2 focus-visible:ring-offset-[var(--sidebar-bg)]',
  {
    variants: {
      state: {
        idle: 'text-white/55 hover:bg-white/10 hover:text-white/90',
        // Active state pre-polish was bg-white/5 (invisible on near-black sidebar).
        // Bump opacity to 10 and pair the cyan rule with a strong icon tone so the
        // signal isn't carried by the rule alone.
        active:
          'bg-white/10 text-white border-l-[3px] border-[var(--secondary)] pl-[calc(0.75rem-3px)] [&_svg]:text-[var(--secondary)]',
      },
    },
    defaultVariants: { state: 'idle' },
  },
);

/**
 * Brand mark — a minimal "W" inside a rounded square, painted with the cyan
 * accent so the collapsed sidebar still reads as branded chrome rather than
 * an orphan letter. SVG keeps it crisp at any size; no asset request.
 */
function BrandMark({ className, ...props }: SVGProps<SVGSVGElement> & { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      role="img"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      {...props}
    >
      <rect width="24" height="24" rx="5" fill="var(--secondary)" />
      <path
        d="M5 7l2.6 10h2.2L12 10l2.2 7h2.2L19 7h-2.3l-1.6 6.6L13.1 7h-2.2L9 13.6 7.4 7H5z"
        fill="var(--brand-navy)"
      />
    </svg>
  );
}

function isActive(currentPath: string, itemPath: string): boolean {
  if (currentPath === itemPath) return true;
  return currentPath.startsWith(itemPath + '/');
}

export function DarkSidebar() {
  const user = useAuthStore((s) => s.user);
  const collapsed = useUiStore((s) => s.sidebarCollapsed);
  const toggleSidebar = useUiStore((s) => s.toggleSidebar);
  const location = useLocation();
  const items = NAV_ITEMS.filter((item) => !item.visible || item.visible(user));

  return (
    <Tooltip.Provider delayDuration={150}>
      <nav
        aria-label={t('nav.primary')}
        className={cn(
          'flex h-full flex-col border-r border-[var(--secondary)]/30 bg-[var(--sidebar-bg)] text-white/55 transition-[width] duration-[var(--motion-normal)]',
          collapsed ? 'w-12' : 'w-64',
        )}
      >
        <div className="flex h-12 items-center justify-center border-b border-white/5 px-3">
          {collapsed ? (
            <BrandMark className="size-6" aria-label={t('app.brandName')} />
          ) : (
            <div className="flex items-center gap-2">
              <BrandMark className="size-5" aria-hidden />
              <span className="font-heading text-sm font-bold tracking-wide text-white">
                {t('app.brandShort')}
              </span>
            </div>
          )}
        </div>
        <ul className="flex flex-1 flex-col gap-1 p-2" role="list">
          {items.map((item) => {
            const active = isActive(location.pathname, item.to);
            const Icon = item.icon;
            const label = t(item.labelKey);
            const link = (
              <NavLink
                to={item.to}
                aria-label={label}
                aria-current={active ? 'page' : undefined}
                className={cn(itemVariants({ state: active ? 'active' : 'idle' }))}
              >
                <Icon className="size-4 shrink-0" aria-hidden="true" />
                <span className={collapsed ? 'sr-only' : 'truncate'}>{label}</span>
              </NavLink>
            );
            return (
              <li key={item.to}>
                {collapsed ? (
                  <Tooltip.Root>
                    <Tooltip.Trigger asChild>{link}</Tooltip.Trigger>
                    <Tooltip.Content
                      side="right"
                      sideOffset={8}
                      className="rounded-[var(--radius-md)] bg-[var(--card)] px-2 py-1 text-xs text-[var(--foreground)] shadow-[var(--shadow-md)]"
                    >
                      {label}
                    </Tooltip.Content>
                  </Tooltip.Root>
                ) : (
                  link
                )}
              </li>
            );
          })}
        </ul>
        <div className="p-2">
          <Button
            variant="ghost"
            size="icon"
            onClick={toggleSidebar}
            aria-label={collapsed ? t('nav.expand') : t('nav.collapse')}
            className="w-full text-white/55 hover:bg-white/5 hover:text-white"
          >
            {collapsed ? (
              <ChevronRight className="size-4" aria-hidden="true" />
            ) : (
              <ChevronLeft className="size-4" aria-hidden="true" />
            )}
          </Button>
        </div>
      </nav>
    </Tooltip.Provider>
  );
}
