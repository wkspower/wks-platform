import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import * as authApi from '@/api/auth';
import { ApiError } from '@/api/client';

import { useAuthStore } from './authStore';

function resetStore(): void {
  useAuthStore.setState({ user: null, status: 'pending', error: null });
}

describe('authStore', () => {
  beforeEach(() => {
    resetStore();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('hydrate sets authenticated when getMe resolves', async () => {
    vi.spyOn(authApi, 'getMe').mockResolvedValue({
      id: 'u-1',
      email: 'a@b.c',
      roles: ['admin'],
    });
    await useAuthStore.getState().hydrate();
    expect(useAuthStore.getState().status).toBe('authenticated');
    expect(useAuthStore.getState().user?.email).toBe('a@b.c');
  });

  it('hydrate sets unauthenticated on 401', async () => {
    vi.spyOn(authApi, 'getMe').mockRejectedValue(
      new ApiError({ status: 401, code: 'WKS-API-401', message: 'unauth' }),
    );
    await useAuthStore.getState().hydrate();
    expect(useAuthStore.getState().status).toBe('unauthenticated');
    expect(useAuthStore.getState().user).toBeNull();
  });

  it('hydrate sets unauthenticated on network error without throwing into render', async () => {
    vi.spyOn(authApi, 'getMe').mockRejectedValue(new Error('boom'));
    await expect(useAuthStore.getState().hydrate()).resolves.toBeUndefined();
    expect(useAuthStore.getState().status).toBe('unauthenticated');
  });

  it('login on success transitions to authenticated', async () => {
    vi.spyOn(authApi, 'login').mockResolvedValue({
      id: 'u-1',
      email: 'a@b.c',
      roles: ['admin'],
    });
    await useAuthStore.getState().login('a@b.c', 'pw');
    expect(useAuthStore.getState().status).toBe('authenticated');
    expect(useAuthStore.getState().error).toBeNull();
  });

  it('login on 401 records the error message and rethrows', async () => {
    vi.spyOn(authApi, 'login').mockRejectedValue(
      new ApiError({
        status: 401,
        code: 'WKS-API-401',
        message: 'Invalid email or password',
      }),
    );
    await expect(useAuthStore.getState().login('a@b.c', 'pw')).rejects.toBeInstanceOf(ApiError);
    expect(useAuthStore.getState().status).toBe('unauthenticated');
    expect(useAuthStore.getState().error).toBe('Invalid email or password');
  });
});
