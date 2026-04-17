import type { ApiErrorEnvelope, ApiSuccessEnvelope } from '@/types/api';

import { sessionBus } from './sessionBus';

const LOGIN_PATH = '/api/auth/login';

export class ApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly field: string | null;
  readonly correlationId: string | null;

  constructor(init: {
    status: number;
    code: string;
    message: string;
    field?: string | null;
    correlationId?: string | null;
  }) {
    super(init.message);
    this.name = 'ApiError';
    this.status = init.status;
    this.code = init.code;
    this.field = init.field ?? null;
    this.correlationId = init.correlationId ?? null;
  }
}

export interface ApiResult<T> {
  data: T;
}

/**
 * Single fetch entry point for the entire frontend. Enforces:
 *  - credentials: 'include' so the WKS_SESSION cookie rides along
 *  - JSON content type when a body is present
 *  - envelope parsing — { data, meta } on 2xx, { error, meta } otherwise
 *  - typed ApiError throw on non-2xx, carrying X-Correlation-Id
 *  - session-expiry bus emission on 401 (skipped for the login call itself,
 *    where 401 is just a credential failure rather than session expiry)
 */
export async function apiFetch<T>(path: string, init: RequestInit = {}): Promise<ApiResult<T>> {
  const headers = new Headers(init.headers);
  const hasBody = init.body !== undefined && init.body !== null;
  const isFormData = typeof FormData !== 'undefined' && init.body instanceof FormData;
  if (hasBody && !isFormData && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(path, {
    ...init,
    headers,
    credentials: 'include',
  });

  const correlationId = response.headers.get('X-Correlation-Id');

  if (response.status === 401 && path !== LOGIN_PATH) {
    sessionBus.emit({ requestPath: path });
  }

  if (response.ok) {
    if (response.status === 204) {
      return { data: undefined as T };
    }
    const body = (await response.json()) as ApiSuccessEnvelope<T>;
    return { data: body.data };
  }

  let errorBody: ApiErrorEnvelope | null = null;
  try {
    errorBody = (await response.json()) as ApiErrorEnvelope;
  } catch {
    errorBody = null;
  }

  throw new ApiError({
    status: response.status,
    code: errorBody?.error.code ?? `WKS-API-${response.status}`,
    message: errorBody?.error.message ?? response.statusText,
    field: errorBody?.error.field ?? null,
    correlationId,
  });
}
