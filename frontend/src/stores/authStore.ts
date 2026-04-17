import { create } from 'zustand';

import { getMe, login as loginRequest, logout as logoutRequest } from '@/api/auth';
import { ApiError } from '@/api/client';
import type { AuthUser } from '@/types/auth';

export type AuthStatus = 'pending' | 'authenticated' | 'unauthenticated';

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
      const message =
        err instanceof ApiError ? err.message : 'Unexpected error during login';
      set({ user: null, status: 'unauthenticated', error: message });
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
    // React 19 StrictMode runs effects twice in dev. The store guards
    // against a second hydrate while the first is still in flight by
    // checking the canonical pending->authenticated/unauthenticated
    // transition: re-entering hydrate() during 'pending' is a no-op.
    if (get().status !== 'pending' && get().user !== null) return;
    try {
      const user = await getMe();
      set({ user, status: 'authenticated', error: null });
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        set({ user: null, status: 'unauthenticated', error: null });
        return;
      }
      // eslint-disable-next-line no-console
      console.warn('[auth] hydrate failed', err);
      set({ user: null, status: 'unauthenticated', error: null });
    }
  },
}));
