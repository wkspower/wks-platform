import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { renderWithProviders } from '@/test/renderWithProviders';

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

describe('LoginPage — RHF + Zod retrofit (AC11)', () => {
  it('shows the localized email-format error when an invalid email is submitted', async () => {
    const user = userEvent.setup();
    const { getByLabelText, getByRole, findByText } = loginAt();
    await user.type(getByLabelText(/email/i), 'not-an-email');
    await user.type(getByLabelText(/password/i), 'something');
    await user.click(getByRole('button', { name: /sign in/i }));
    expect(await findByText(/valid email/i)).toBeInTheDocument();
  });

  it('shows the password-required error when password is empty on submit', async () => {
    const user = userEvent.setup();
    const { getByLabelText, getByRole, findByText } = loginAt();
    await user.type(getByLabelText(/email/i), 'a@b.com');
    await user.click(getByRole('button', { name: /sign in/i }));
    expect(await findByText(/password is required/i)).toBeInTheDocument();
  });

  it('email field is wired aria-invalid + aria-describedby when in error', async () => {
    const user = userEvent.setup();
    const { getByLabelText, getByRole } = loginAt();
    await user.type(getByLabelText(/email/i), 'bad');
    await user.click(getByRole('button', { name: /sign in/i }));
    const email = getByLabelText(/email/i);
    expect(email).toHaveAttribute('aria-invalid', 'true');
    expect(email.getAttribute('aria-describedby')).toBeTruthy();
  });
});
