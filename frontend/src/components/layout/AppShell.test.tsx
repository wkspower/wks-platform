import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';

import { sessionBus } from '@/api/sessionBus';
import { renderWithProviders } from '@/test/renderWithProviders';

import { AppShell } from './AppShell';

function shellAt(path = '/cases') {
  return renderWithProviders(
    <Routes>
      <Route element={<AppShell />}>
        <Route path="/cases" element={<div>Cases content</div>} />
      </Route>
    </Routes>,
    {
      initialPath: path,
      initialAuth: {
        user: { id: 'u', email: 'u@x', roles: ['admin'] },
        status: 'authenticated',
      },
    },
  );
}

describe('AppShell', () => {
  it('renders sidebar nav and skip link', () => {
    const { getByRole, getByText } = shellAt();
    expect(getByRole('navigation', { name: /primary navigation/i })).toBeInTheDocument();
    expect(getByText(/skip to main content/i)).toBeInTheDocument();
    expect(getByText('Cases content')).toBeInTheDocument();
  });

  it('skip link is the first focusable element on the page', () => {
    const { container } = shellAt();
    const skip = container.querySelector('a[href="#main"]');
    expect(skip).not.toBeNull();
    // First link in DOM order should be the skip link.
    const firstLink = container.querySelector('a');
    expect(firstLink).toBe(skip);
  });

  it('shows session-expired banner when bus emits, hides on dismiss', async () => {
    const { findByRole, getByLabelText, queryByRole } = shellAt();
    const user = userEvent.setup();
    sessionBus.emit({ requestPath: '/api/cases' });
    const banner = await findByRole('alert');
    expect(banner).toHaveTextContent(/session has expired/i);
    await user.click(getByLabelText(/dismiss/i));
    // After dismiss, banner is no longer in the DOM.
    expect(queryByRole('alert')).toBeNull();
  });

  it('RouteErrorBoundary resets when the user navigates to a different route', async () => {
    const errSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    function Boom(): never {
      throw new Error('kaboom');
    }
    const { getByRole, queryByRole, findByText } = renderWithProviders(
      <Routes>
        <Route element={<AppShell />}>
          <Route path="/cases" element={<Boom />} />
          <Route path="/tasks" element={<div>Tasks content</div>} />
        </Route>
      </Routes>,
      {
        initialPath: '/cases',
        initialAuth: {
          user: { id: 'u', email: 'u@x', roles: ['admin'] },
          status: 'authenticated',
        },
      },
    );
    // Boundary catches the thrown error on /cases
    expect(getByRole('heading', { level: 2 })).toHaveTextContent(/something unexpected/i);
    // Navigating via sidebar should clear the boundary (pathname-keyed remount)
    await userEvent.setup().click(getByRole('link', { name: /tasks/i }));
    expect(await findByText('Tasks content')).toBeInTheDocument();
    expect(queryByRole('heading', { level: 2 })).toBeNull();
    errSpy.mockRestore();
  });
});
