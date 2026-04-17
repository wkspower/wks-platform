import userEvent from '@testing-library/user-event';
import { HttpResponse, delay, http } from 'msw';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { renderWithProviders } from '@/test/renderWithProviders';
import { server } from '@/test/server';

import { LoginPage } from './LoginPage';

function loginAt(path = '/login') {
  return renderWithProviders(
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/cases" element={<div>cases page</div>} />
    </Routes>,
    { initialPath: path, initialAuth: { status: 'unauthenticated' } },
  );
}

describe('LoginPage', () => {
  it('happy path: submit valid creds, redirect to /cases', async () => {
    const user = userEvent.setup();
    const { findByText, getByLabelText, getByRole } = loginAt();
    await user.type(getByLabelText(/email/i), 'admin@wkspower.local');
    await user.type(getByLabelText(/password/i), 'admin');
    await user.click(getByRole('button', { name: /sign in/i }));
    expect(await findByText('cases page')).toBeInTheDocument();
  });

  it('invalid credentials show the localized invalid message', async () => {
    const user = userEvent.setup();
    const { findByRole, getByLabelText, getByRole } = loginAt();
    await user.type(getByLabelText(/email/i), 'nope@x.com');
    await user.type(getByLabelText(/password/i), 'wrong');
    await user.click(getByRole('button', { name: /sign in/i }));
    const alert = await findByRole('alert');
    expect(alert).toHaveTextContent(/invalid email or password/i);
  });

  it('non-401 errors show the generic message', async () => {
    server.use(
      http.post('/api/auth/login', () =>
        HttpResponse.json(
          { error: { code: 'WKS-API-500', message: 'boom', field: null }, meta: {} },
          { status: 500 },
        ),
      ),
    );
    const user = userEvent.setup();
    const { findByRole, getByLabelText, getByRole } = loginAt();
    await user.type(getByLabelText(/email/i), 'a@b.c');
    await user.type(getByLabelText(/password/i), 'pw');
    await user.click(getByRole('button', { name: /sign in/i }));
    const alert = await findByRole('alert');
    expect(alert).toHaveTextContent(/something went wrong/i);
  });

  it('double-submit protection: only one network request is fired', async () => {
    let callCount = 0;
    server.use(
      http.post('/api/auth/login', async () => {
        callCount += 1;
        await delay(150);
        return HttpResponse.json(
          {
            data: { id: 'u', email: 'a@b.c', roles: ['admin'] },
            meta: {},
          },
          { status: 200 },
        );
      }),
    );
    const user = userEvent.setup();
    const { getByLabelText, getByRole, findByText } = loginAt();
    await user.type(getByLabelText(/email/i), 'a@b.c');
    await user.type(getByLabelText(/password/i), 'pw');
    const button = getByRole('button', { name: /sign in/i });
    // Click twice in immediate succession; the second click should be a no-op.
    await user.click(button);
    await user.click(button);
    expect(await findByText('cases page')).toBeInTheDocument();
    expect(callCount).toBe(1);
  });
});
