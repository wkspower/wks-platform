import { HttpResponse, http } from 'msw';
import { afterEach, describe, expect, it, vi } from 'vitest';

import { server } from '@/test/server';

import { ApiError, apiFetch } from './client';
import { SESSION_EXPIRED, sessionBus, type SessionExpiredEvent } from './sessionBus';

afterEach(() => {
  // setup.ts already calls server.resetHandlers; remove any leftover bus
  // listeners between tests so emit-counts are accurate.
  sessionBus.dispatchEvent(new Event('reset'));
});

describe('apiFetch', () => {
  it('returns the data envelope on 2xx', async () => {
    server.use(
      http.get('/api/anything', () =>
        HttpResponse.json({ data: { id: 'abc' }, meta: {} }, { status: 200 }),
      ),
    );
    const result = await apiFetch<{ id: string }>('/api/anything');
    expect(result.data).toEqual({ id: 'abc' });
  });

  it('always sends credentials: include and JSON content-type for bodies', async () => {
    let received: Request | null = null;
    server.use(
      http.post('/api/echo', ({ request }) => {
        received = request;
        return HttpResponse.json({ data: null, meta: {} }, { status: 200 });
      }),
    );
    await apiFetch('/api/echo', { method: 'POST', body: JSON.stringify({ x: 1 }) });
    expect(received).not.toBeNull();
    expect(received!.credentials).toBe('include');
    expect(received!.headers.get('Content-Type')).toBe('application/json');
  });

  it('throws a typed ApiError on non-2xx with the envelope fields', async () => {
    server.use(
      http.get('/api/bad', () =>
        HttpResponse.json(
          { error: { code: 'WKS-API-422', message: 'Bad', field: 'email' }, meta: {} },
          { status: 422, headers: { 'X-Correlation-Id': 'corr-1' } },
        ),
      ),
    );
    await expect(apiFetch('/api/bad')).rejects.toMatchObject({
      name: 'ApiError',
      status: 422,
      code: 'WKS-API-422',
      message: 'Bad',
      field: 'email',
      correlationId: 'corr-1',
    });
  });

  it('emits session-expired on 401 for non-login paths', async () => {
    server.use(
      http.get('/api/cases', () =>
        HttpResponse.json(
          { error: { code: 'WKS-API-401', message: 'unauth', field: null }, meta: {} },
          { status: 401 },
        ),
      ),
    );
    const handler = vi.fn();
    sessionBus.addEventListener(SESSION_EXPIRED, handler as EventListener);
    await expect(apiFetch('/api/cases')).rejects.toBeInstanceOf(ApiError);
    sessionBus.removeEventListener(SESSION_EXPIRED, handler as EventListener);
    expect(handler).toHaveBeenCalledTimes(1);
    const event = handler.mock.calls[0]?.[0] as SessionExpiredEvent;
    expect(event.detail.requestPath).toBe('/api/cases');
  });

  it('does NOT emit session-expired on 401 for the login path', async () => {
    server.use(
      http.post('/api/auth/login', () =>
        HttpResponse.json(
          { error: { code: 'WKS-API-401', message: 'invalid', field: null }, meta: {} },
          { status: 401 },
        ),
      ),
    );
    const handler = vi.fn();
    sessionBus.addEventListener(SESSION_EXPIRED, handler as EventListener);
    await expect(
      apiFetch('/api/auth/login', { method: 'POST', body: JSON.stringify({ email: 'a' }) }),
    ).rejects.toBeInstanceOf(ApiError);
    sessionBus.removeEventListener(SESSION_EXPIRED, handler as EventListener);
    expect(handler).not.toHaveBeenCalled();
  });

  it('returns undefined data on 204 No Content (logout)', async () => {
    const result = await apiFetch<void>('/api/auth/logout', { method: 'POST' });
    expect(result.data).toBeUndefined();
  });
});
