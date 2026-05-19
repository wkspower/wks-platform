import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { ApiError, apiFetch } from './client';
import { SESSION_EXPIRED, sessionBus } from './sessionBus';

function jsonResponse(body: unknown, init: ResponseInit = {}): Response {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { 'Content-Type': 'application/json' },
    ...init,
  });
}

describe('apiFetch', () => {
  let fetchSpy = vi.spyOn(globalThis, 'fetch');

  beforeEach(() => {
    fetchSpy = vi.spyOn(globalThis, 'fetch');
  });

  afterEach(() => {
    fetchSpy.mockRestore();
  });

  it('parses a success envelope and returns data', async () => {
    fetchSpy.mockResolvedValueOnce(jsonResponse({ data: { id: 'c1', name: 'Loan 7' }, meta: {} }));

    const result = await apiFetch<{ id: string; name: string }>('/api/cases/c1');

    expect(result.data).toEqual({ id: 'c1', name: 'Loan 7' });
    expect(fetchSpy).toHaveBeenCalledWith(
      '/api/cases/c1',
      expect.objectContaining({ credentials: 'include' }),
    );
  });

  it('returns undefined data on 204', async () => {
    fetchSpy.mockResolvedValueOnce(new Response(null, { status: 204 }));

    const result = await apiFetch<void>('/api/cases/c1', { method: 'DELETE' });

    expect(result.data).toBeUndefined();
  });

  it('sets JSON content type when a non-FormData body is provided', async () => {
    fetchSpy.mockResolvedValueOnce(jsonResponse({ data: null, meta: {} }));

    await apiFetch('/api/cases', { method: 'POST', body: JSON.stringify({ x: 1 }) });

    const init = fetchSpy.mock.calls[0]![1] as RequestInit;
    const headers = init.headers as Headers;
    expect(headers.get('Content-Type')).toBe('application/json');
  });

  it('throws ApiError on non-2xx and populates code + message + correlation id', async () => {
    fetchSpy.mockResolvedValueOnce(
      jsonResponse(
        { error: { code: 'WKS-API-404', message: 'Not found' }, meta: {} },
        { status: 404, headers: { 'Content-Type': 'application/json', 'X-Correlation-Id': 'cid-1' } },
      ),
    );

    await expect(apiFetch('/api/cases/missing')).rejects.toMatchObject({
      name: 'ApiError',
      status: 404,
      code: 'WKS-API-404',
      message: 'Not found',
      correlationId: 'cid-1',
    });
  });

  it('exposes envelope multi-error array on validation aggregates', async () => {
    fetchSpy.mockResolvedValueOnce(
      jsonResponse(
        {
          error: {
            code: 'WKS-CFG-000',
            message: 'Configuration invalid',
            errors: [
              { code: 'WKS-CFG-001', message: 'name missing', field: 'name' },
              { code: 'WKS-CFG-008', message: 'status not declared', field: 'status' },
            ],
          },
          meta: {},
        },
        { status: 422 },
      ),
    );

    try {
      await apiFetch('/api/admin/deploy', { method: 'POST', body: '{}' });
      expect.fail('should have thrown');
    } catch (err) {
      expect(err).toBeInstanceOf(ApiError);
      const apiErr = err as ApiError;
      expect(apiErr.envelopeErrors).toHaveLength(2);
      expect(apiErr.envelopeErrors![0]).toMatchObject({ code: 'WKS-CFG-001', field: 'name' });
    }
  });

  it('emits session-expired bus on 401 for non-login paths', async () => {
    const listener = vi.fn();
    sessionBus.addEventListener(SESSION_EXPIRED, listener as EventListener);
    fetchSpy.mockResolvedValueOnce(
      jsonResponse({ error: { code: 'WKS-API-401', message: 'auth' }, meta: {} }, { status: 401 }),
    );

    await expect(apiFetch('/api/cases')).rejects.toBeInstanceOf(ApiError);

    expect(listener).toHaveBeenCalledTimes(1);
    const event = listener.mock.calls[0]![0] as CustomEvent<{ requestPath: string }>;
    expect(event.detail.requestPath).toBe('/api/cases');

    sessionBus.removeEventListener(SESSION_EXPIRED, listener as EventListener);
  });

  it('does NOT emit session-expired on 401 for the login path', async () => {
    const listener = vi.fn();
    sessionBus.addEventListener(SESSION_EXPIRED, listener as EventListener);
    fetchSpy.mockResolvedValueOnce(
      jsonResponse({ error: { code: 'WKS-API-401', message: 'bad creds' }, meta: {} }, { status: 401 }),
    );

    await expect(apiFetch('/api/auth/login', { method: 'POST', body: '{}' })).rejects.toBeInstanceOf(
      ApiError,
    );

    expect(listener).not.toHaveBeenCalled();
    sessionBus.removeEventListener(SESSION_EXPIRED, listener as EventListener);
  });

  it('falls back to a synthetic code when the error body is not JSON', async () => {
    fetchSpy.mockResolvedValueOnce(
      new Response('<html>oops</html>', { status: 500, headers: { 'Content-Type': 'text/html' } }),
    );

    await expect(apiFetch('/api/cases')).rejects.toMatchObject({
      status: 500,
      code: 'WKS-API-500',
    });
  });
});
