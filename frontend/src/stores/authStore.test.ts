import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { ApiError } from '@/api/client';

import { LOGIN_ERROR_GENERIC, useAuthStore } from './authStore';

function jsonResponse(body: unknown, init: ResponseInit = {}): Response {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { 'Content-Type': 'application/json' },
    ...init,
  });
}

describe('authStore', () => {
  let fetchSpy = vi.spyOn(globalThis, 'fetch');

  beforeEach(() => {
    fetchSpy = vi.spyOn(globalThis, 'fetch');
    // Reset store between tests — Zustand singletons leak across tests.
    useAuthStore.setState({ user: null, status: 'pending', error: null });
  });

  afterEach(() => {
    fetchSpy.mockRestore();
  });

  it('login on success sets user + status=authenticated', async () => {
    const user = { id: 'u1', email: 'admin@wks.test', roles: ['ROLE_ADMIN'] };
    fetchSpy.mockResolvedValueOnce(jsonResponse({ data: user, meta: {} }));

    await useAuthStore.getState().login('admin@wks.test', 'pw');

    const s = useAuthStore.getState();
    expect(s.user).toEqual(user);
    expect(s.status).toBe('authenticated');
    expect(s.error).toBeNull();
  });

  it('login on 401 leaves error=null (auth failure is not a generic error)', async () => {
    fetchSpy.mockResolvedValueOnce(
      jsonResponse({ error: { code: 'WKS-API-401', message: 'bad creds' }, meta: {} }, { status: 401 }),
    );

    await expect(useAuthStore.getState().login('admin@wks.test', 'wrong')).rejects.toBeInstanceOf(ApiError);

    const s = useAuthStore.getState();
    expect(s.user).toBeNull();
    expect(s.status).toBe('unauthenticated');
    expect(s.error).toBeNull();
  });

  it('login on 500 sets error=LOGIN_ERROR_GENERIC', async () => {
    fetchSpy.mockResolvedValueOnce(
      jsonResponse({ error: { code: 'WKS-RTM-500', message: 'oops' }, meta: {} }, { status: 500 }),
    );

    await expect(useAuthStore.getState().login('a@b.c', 'pw')).rejects.toBeInstanceOf(ApiError);

    expect(useAuthStore.getState().error).toBe(LOGIN_ERROR_GENERIC);
  });

  it('logout clears state even if the network call fails', async () => {
    useAuthStore.setState({
      user: { id: 'u1', email: 'a@b.c', roles: [] },
      status: 'authenticated',
      error: null,
    });
    fetchSpy.mockRejectedValueOnce(new Error('network down'));

    await useAuthStore.getState().logout();

    const s = useAuthStore.getState();
    expect(s.user).toBeNull();
    expect(s.status).toBe('unauthenticated');
  });
});
