import { HttpResponse, http } from 'msw';

import type { ApiSuccessEnvelope } from '@/types/api';
import type { AuthUser } from '@/types/auth';

const ADMIN: AuthUser = {
  id: '11111111-1111-4111-8111-111111111111',
  email: 'admin@wkspower.local',
  roles: ['admin'],
};

function envelope<T>(data: T, headers: Record<string, string> = {}): Response {
  return HttpResponse.json<ApiSuccessEnvelope<T>>(
    { data, meta: {} },
    { status: 200, headers: { 'X-Correlation-Id': 'test-correlation', ...headers } },
  );
}

/**
 * Default MSW handlers shared by every test. Per-test overrides are
 * registered via server.use(...) in the test body — DO NOT mutate this
 * list at runtime.
 */
export const defaultHandlers = [
  http.post('/api/auth/login', async ({ request }) => {
    const body = (await request.json()) as { email?: string; password?: string };
    if (body?.email === 'admin@wkspower.local' && body?.password === 'admin') {
      return envelope(ADMIN);
    }
    return HttpResponse.json(
      { error: { code: 'WKS-API-401', message: 'Invalid credentials', field: null }, meta: {} },
      { status: 401 },
    );
  }),

  http.post('/api/auth/logout', () => new HttpResponse(null, { status: 204 })),

  http.get('/api/auth/me', () =>
    HttpResponse.json(
      { error: { code: 'WKS-API-401', message: 'Not authenticated', field: null }, meta: {} },
      { status: 401 },
    ),
  ),
];

export { ADMIN as defaultAdminUser };
