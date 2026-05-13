import { Outlet, useLocation } from 'react-router-dom';

import { RouteErrorBoundary } from '@/components/errors/RouteErrorBoundary';

import { BannerStack } from './BannerStack';
import { DarkSidebar } from './DarkSidebar';
import { SkipLink } from './SkipLink';

export function AppShell() {
  const location = useLocation();
  return (
    <div className="grid min-h-screen grid-cols-[auto_1fr]">
      <SkipLink />
      <DarkSidebar />
      <div className="flex min-w-0 flex-col">
        <BannerStack />
        <main
          id="main"
          tabIndex={-1}
          className="flex-1 bg-[var(--background)] p-[var(--space-6)] text-[var(--foreground)] focus:outline-none"
        >
          {/* Keying the boundary on pathname remounts it on route change, so a
              thrown error on one route clears automatically when the user
              navigates to another via the sidebar. Without this the boundary
              traps the user on the fallback card until a full reload. */}
          <RouteErrorBoundary key={location.pathname}>
            <Outlet />
          </RouteErrorBoundary>
        </main>
      </div>
    </div>
  );
}
