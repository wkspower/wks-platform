import { Navigate } from 'react-router-dom';

import { Spinner } from '@/components/ui/Spinner';
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
        <Spinner className="size-8" />
      </div>
    );
  }
  if (status === 'authenticated') return <Navigate to="/cases" replace />;
  return <Navigate to="/login" replace />;
}
