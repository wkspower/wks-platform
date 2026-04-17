import type { ReactNode } from 'react';
import { useEffect } from 'react';

import { t } from '@/i18n';
import { useAuthStore } from '@/stores/authStore';

interface AuthProviderProps {
  children: ReactNode;
}

/**
 * Owns the single hydrate() side effect on app mount. Renders a static
 * brand-spinner while auth status is 'pending' so leaf screens never
 * render against an unknown user. Routing (RequireAuth / RootRedirect)
 * decides where to send the user once status resolves.
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const status = useAuthStore((s) => s.status);
  const hydrate = useAuthStore((s) => s.hydrate);

  useEffect(() => {
    void hydrate();
  }, [hydrate]);

  if (status === 'pending') {
    return (
      <div
        className="grid min-h-screen place-items-center bg-background text-foreground"
        role="status"
        aria-live="polite"
      >
        <div className="flex flex-col items-center gap-4">
          <div
            className="size-10 animate-spin rounded-full border-2 border-muted border-t-primary"
            aria-hidden="true"
          />
          <p className="font-heading text-lg">{t('app.brandName')}</p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
