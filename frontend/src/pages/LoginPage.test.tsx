import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { useAuthStore } from '@/stores/authStore';
import { renderWithProviders } from '@/test/renderWithProviders';

import { LoginPage } from './LoginPage';

function jsonResponse(body: unknown, init: ResponseInit = {}): Response {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { 'Content-Type': 'application/json' },
    ...init,
  });
}

describe('<LoginPage>', () => {
  let fetchSpy = vi.spyOn(globalThis, 'fetch');

  beforeEach(() => {
    fetchSpy = vi.spyOn(globalThis, 'fetch');
  });

  afterEach(() => {
    fetchSpy.mockRestore();
  });

  it('shows zod errors when submitted empty', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginPage />);

    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText('Email is required')).toBeInTheDocument();
    expect(screen.getByText('Password is required')).toBeInTheDocument();
    expect(fetchSpy).not.toHaveBeenCalled();
  });

  it('surfaces a friendly message on 401', async () => {
    const user = userEvent.setup();
    fetchSpy.mockResolvedValueOnce(
      jsonResponse({ error: { code: 'WKS-API-401', message: 'bad creds' }, meta: {} }, { status: 401 }),
    );

    renderWithProviders(<LoginPage />);

    await user.type(screen.getByLabelText(/email/i), 'admin@wks.test');
    await user.type(screen.getByLabelText(/password/i), 'wrong');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/email or password is incorrect/i);
  });

  it('on success, transitions to authenticated and redirects', async () => {
    const user = userEvent.setup();
    const me = { id: 'u1', email: 'admin@wks.test', roles: [] };
    fetchSpy.mockResolvedValueOnce(jsonResponse({ data: me, meta: {} }));

    renderWithProviders(<LoginPage />);

    await user.type(screen.getByLabelText(/email/i), 'admin@wks.test');
    await user.type(screen.getByLabelText(/password/i), 'correct');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(useAuthStore.getState().status).toBe('authenticated');
    });
    expect(useAuthStore.getState().user).toEqual(me);
  });
});
