import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import { SESSION_EXPIRED, sessionBus } from '@/api/sessionBus';
import { useAuthStore } from '@/stores/authStore';

export interface UseSessionExpiry {
  expired: boolean;
  dismiss: () => void;
  triggerLogin: () => void;
}

/**
 * Subscribes to the session bus. Returns a small surface the AppShell
 * uses to render an inline banner when an authenticated request returned
 * 401. Dismiss is in-memory only — the next 401 re-raises the banner.
 */
export function useSessionExpiry(): UseSessionExpiry {
  const [expired, setExpired] = useState(false);
  const navigate = useNavigate();
  const logout = useAuthStore((s) => s.logout);

  useEffect(() => {
    const handler = (): void => setExpired(true);
    sessionBus.addEventListener(SESSION_EXPIRED, handler);
    return () => sessionBus.removeEventListener(SESSION_EXPIRED, handler);
  }, []);

  const dismiss = useCallback(() => setExpired(false), []);

  const triggerLogin = useCallback(() => {
    setExpired(false);
    void logout();
    const returnTo = window.location.pathname + window.location.search;
    navigate(`/login?returnTo=${encodeURIComponent(returnTo)}`);
  }, [logout, navigate]);

  return { expired, dismiss, triggerLogin };
}
