import { create } from 'zustand';

import { getMe, login as loginRequest, logout as logoutRequest } from '@/api/auth';
import { ApiError } from '@/api/client';
import type { AuthUser } from '@/types/auth';

export type AuthStatus = 'pending' | 'authenticated' | 'unauthenticated';

// 401 carries the "invalid credentials" meaning via status='unauthenticated'; the
// localized message belongs to the UI layer (LoginPage reads the thrown ApiError
// directly). Non-ApiError failures collapse to a single generic sentinel so
// store.error never surfaces backend-controlled text to future consumers.
export const LOGIN_ERROR_GENERIC = 'unexpected';

// Hydrate in-flight latch — prevents StrictMode's double-invoke (and any other
// concurrent caller) from firing a second /api/auth/me. The canonical pending→
// authenticated/unauthenticated status alone can't distinguish "not started"
// from "in flight" because both leave status='pending'.
let hydrating = false;
const HYDRATE_TIMEOUT_MS = 8_000;

export interface AuthState {
  user: AuthUser | null;
  status: AuthStatus;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  hydrate: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  status: 'pending',
  error: null,

  async login(email, password) {
    set({ error: null });
    try {
      const user = await loginRequest(email, password);
      set({ user, status: 'authenticated', error: null });
    } catch (err) {
      const isAuthFailure = err instanceof ApiError && err.status === 401;
      set({
        user: null,
        status: 'unauthenticated',
        error: isAuthFailure ? null : LOGIN_ERROR_GENERIC,
      });
      throw err;
    }
  },

  async logout() {
    try {
      await logoutRequest();
    } catch {
      // Logout is best-effort; the cookie may already be invalid.
    }
    set({ user: null, status: 'unauthenticated', error: null });
  },

  async hydrate() {
    if (hydrating) return;
    if (get().status === 'authenticated') return;
    hydrating = true;
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), HYDRATE_TIMEOUT_MS);
    try {
      const user = await getMe(controller.signal);
      set({ user, status: 'authenticated', error: null });
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        set({ user: null, status: 'unauthenticated', error: null });
        return;
      }
      // eslint-disable-next-line no-console
      console.warn('[auth] hydrate failed', err);
      set({ user: null, status: 'unauthenticated', error: null });
    } finally {
      clearTimeout(timer);
      hydrating = false;
    }
  },
}));
