import userEvent from '@testing-library/user-event';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

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
  it('renders sidebar nav, top bar header, and skip link', () => {
    const { getByRole, getByText } = shellAt();
    expect(getByRole('navigation', { name: /primary navigation/i })).toBeInTheDocument();
    expect(getByRole('banner')).toBeInTheDocument(); // <header> = banner role
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
});
