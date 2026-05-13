import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';

import { ForbiddenScreen } from '@/components/errors/ForbiddenScreen';
import { Spinner } from '@/components/ui/Spinner';
import { useAuthStore } from '@/stores/authStore';

interface RequireAuthProps {
  children: ReactNode;
  requiredRole?: string;
}

export function RequireAuth({ children, requiredRole }: RequireAuthProps) {
  const status = useAuthStore((s) => s.status);
  const user = useAuthStore((s) => s.user);
  const location = useLocation();

  if (status === 'pending') {
    return (
      <div
        className="grid min-h-screen place-items-center bg-background"
        role="status"
        aria-live="polite"
      >
        <Spinner className="size-8" />
      </div>
    );
  }

  if (status === 'unauthenticated' || !user) {
    const returnTo = encodeURIComponent(location.pathname + location.search + location.hash);
    return <Navigate to={`/login?returnTo=${returnTo}`} replace />;
  }

  if (requiredRole && !user.roles.includes(requiredRole)) {
    return <ForbiddenScreen />;
  }

  return <>{children}</>;
}
