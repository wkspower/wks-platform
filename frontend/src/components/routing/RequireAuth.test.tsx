import type { ReactElement } from 'react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { renderWithProviders } from '@/test/renderWithProviders';

import { RequireAuth } from './RequireAuth';

function shellAt(path: string, ui: ReactElement, opts?: Parameters<typeof renderWithProviders>[1]) {
  return renderWithProviders(
    <Routes>
      <Route path="/login" element={<div>login page</div>} />
      <Route path="/admin" element={<RequireAuth requiredRole="admin"><div>admin content</div></RequireAuth>} />
      <Route path="/cases" element={<RequireAuth>{ui}</RequireAuth>} />
    </Routes>,
    { initialPath: path, ...opts },
  );
}

describe('RequireAuth', () => {
  it('renders a spinner while status=pending', () => {
    const { getByRole } = shellAt('/cases', <div>cases</div>, {
      initialAuth: { status: 'pending' },
    });
    expect(getByRole('status')).toBeInTheDocument();
  });

  it('redirects to /login?returnTo=<path> when unauthenticated', () => {
    const { getByText } = shellAt('/cases', <div>cases</div>, {
      initialAuth: { status: 'unauthenticated' },
    });
    expect(getByText('login page')).toBeInTheDocument();
  });

  it('renders ForbiddenScreen when role is missing', () => {
    const { getByRole } = shellAt('/admin', <div>x</div>, {
      initialAuth: {
        user: { id: 'u', email: 'u@x', roles: ['business_user'] },
        status: 'authenticated',
      },
    });
    expect(getByRole('heading', { level: 1 })).toHaveTextContent(/don't have access/i);
  });

  it('renders children when authenticated and role present', () => {
    const { getByText } = shellAt('/admin', <div>x</div>, {
      initialAuth: {
        user: { id: 'u', email: 'u@x', roles: ['admin'] },
        status: 'authenticated',
      },
    });
    expect(getByText('admin content')).toBeInTheDocument();
  });
});
