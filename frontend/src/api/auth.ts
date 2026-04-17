import type { AuthUser } from '@/types/auth';

import { apiFetch } from './client';

export async function login(email: string, password: string): Promise<AuthUser> {
  const result = await apiFetch<AuthUser>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
  return result.data;
}

export async function logout(): Promise<void> {
  await apiFetch<void>('/api/auth/logout', { method: 'POST' });
}

export async function getMe(signal?: AbortSignal): Promise<AuthUser> {
  const result = await apiFetch<AuthUser>('/api/auth/me', { signal });
  return result.data;
}
