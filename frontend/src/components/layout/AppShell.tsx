import { Outlet } from 'react-router-dom';

import { RouteErrorBoundary } from '@/components/errors/RouteErrorBoundary';

import { DarkSidebar } from './DarkSidebar';
import { SessionExpiryBanner } from './SessionExpiryBanner';
import { SkipLink } from './SkipLink';
import { TopBar } from './TopBar';

export function AppShell() {
  return (
    <div className="grid min-h-screen grid-cols-[auto_1fr]">
      <SkipLink />
      <DarkSidebar />
      <div className="flex min-w-0 flex-col">
        <SessionExpiryBanner />
        <TopBar />
        <main
          id="main"
          className="flex-1 bg-[var(--background)] p-[var(--space-6)] text-[var(--foreground)]"
        >
          <RouteErrorBoundary>
            <Outlet />
          </RouteErrorBoundary>
        </main>
      </div>
    </div>
  );
}
