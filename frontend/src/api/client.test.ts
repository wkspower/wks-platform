import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { ApiError, apiFetch } from './client';
import { SESSION_EXPIRED, sessionBus, type SessionExpiredEvent } from './sessionBus';

interface MockResponseInit {
  status?: number;
  body?: unknown;
  headers?: Record<string, string>;
}

function mockResponse({ status = 200, body, headers = {} }: MockResponseInit): Response {
  const responseHeaders = new Headers(headers);
  if (body !== undefined && !responseHeaders.has('Content-Type')) {
    responseHeaders.set('Content-Type', 'application/json');
  }
  const responseBody = body === undefined ? null : JSON.stringify(body);
  return new Response(responseBody, { status, headers: responseHeaders });
}

describe('apiFetch', () => {
  const fetchSpy = vi.spyOn(globalThis, 'fetch');

  beforeEach(() => {
    fetchSpy.mockReset();
  });

  afterEach(() => {
    fetchSpy.mockReset();
  });

  it('returns the data envelope on 2xx', async () => {
    fetchSpy.mockResolvedValueOnce(
      mockResponse({ status: 200, body: { data: { id: 'abc' }, meta: {} } }),
    );
    const result = await apiFetch<{ id: string }>('/api/auth/me');
    expect(result.data).toEqual({ id: 'abc' });
  });

  it('always sends credentials: include', async () => {
    fetchSpy.mockResolvedValueOnce(mockResponse({ status: 200, body: { data: null, meta: {} } }));
    await apiFetch('/api/anything');
    const init = fetchSpy.mock.calls[0]?.[1] as RequestInit;
    expect(init.credentials).toBe('include');
  });

  it('sets Content-Type when sending a JSON body', async () => {
    fetchSpy.mockResolvedValueOnce(mockResponse({ status: 200, body: { data: null, meta: {} } }));
    await apiFetch('/api/auth/login', { method: 'POST', body: JSON.stringify({ email: 'x' }) });
    const headers = (fetchSpy.mock.calls[0]?.[1] as RequestInit).headers as Headers;
    expect(headers.get('Content-Type')).toBe('application/json');
  });

  it('throws a typed ApiError on non-2xx with the envelope fields', async () => {
    fetchSpy.mockResolvedValueOnce(
      mockResponse({
        status: 422,
        body: { error: { code: 'WKS-API-422', message: 'Bad', field: 'email' }, meta: {} },
        headers: { 'X-Correlation-Id': 'corr-1' },
      }),
    );
    await expect(apiFetch('/api/something')).rejects.toMatchObject({
      name: 'ApiError',
      status: 422,
      code: 'WKS-API-422',
      message: 'Bad',
      field: 'email',
      correlationId: 'corr-1',
    });
  });

  it('emits session-expired on 401 for non-login paths', async () => {
    fetchSpy.mockResolvedValueOnce(
      mockResponse({
        status: 401,
        body: { error: { code: 'WKS-API-401', message: 'Unauthorized' }, meta: {} },
      }),
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
    fetchSpy.mockResolvedValueOnce(
      mockResponse({
        status: 401,
        body: { error: { code: 'WKS-API-401', message: 'Invalid email or password' }, meta: {} },
      }),
    );
    const handler = vi.fn();
    sessionBus.addEventListener(SESSION_EXPIRED, handler as EventListener);
    await expect(apiFetch('/api/auth/login', { method: 'POST' })).rejects.toBeInstanceOf(ApiError);
    sessionBus.removeEventListener(SESSION_EXPIRED, handler as EventListener);
    expect(handler).not.toHaveBeenCalled();
  });

  it('returns undefined data on 204 No Content', async () => {
    fetchSpy.mockResolvedValueOnce(mockResponse({ status: 204 }));
    const result = await apiFetch<void>('/api/auth/logout', { method: 'POST' });
    expect(result.data).toBeUndefined();
  });
});
