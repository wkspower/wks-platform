import { Navigate } from 'react-router-dom';

import { useAuthStore } from '@/stores/authStore';

export function RootRedirect() {
  const status = useAuthStore((s) => s.status);
  if (status === 'pending') {
    return (
      <div
        className="grid min-h-screen place-items-center bg-background"
        role="status"
        aria-live="polite"
      >
        <div
          className="size-8 animate-spin rounded-full border-2 border-muted border-t-primary"
          aria-hidden="true"
        />
      </div>
    );
  }
  if (status === 'authenticated') return <Navigate to="/cases" replace />;
  return <Navigate to="/login" replace />;
}
