import { describe, expect, it } from 'vitest';
import userEvent from '@testing-library/user-event';

import { renderWithProviders } from '@/test/renderWithProviders';

import { DarkSidebar } from './DarkSidebar';

describe('DarkSidebar', () => {
  it('shows Cases and Tasks for any authenticated user, hides Admin/Developer', () => {
    const { getByLabelText, queryByLabelText } = renderWithProviders(<DarkSidebar />, {
      initialAuth: {
        user: { id: 'u', email: 'u@x', roles: ['business_user'] },
        status: 'authenticated',
      },
    });
    expect(getByLabelText('Cases')).toBeInTheDocument();
    expect(getByLabelText('Tasks')).toBeInTheDocument();
    expect(queryByLabelText('Admin')).toBeNull();
    expect(queryByLabelText('Developer')).toBeNull();
  });

  it('shows Admin only when role admin is present', () => {
    const { getByLabelText } = renderWithProviders(<DarkSidebar />, {
      initialAuth: {
        user: { id: 'u', email: 'u@x', roles: ['admin'] },
        status: 'authenticated',
      },
    });
    expect(getByLabelText('Admin')).toBeInTheDocument();
  });

  it('shows Developer only when role developer is present', () => {
    const { getByLabelText } = renderWithProviders(<DarkSidebar />, {
      initialAuth: {
        user: { id: 'u', email: 'u@x', roles: ['developer'] },
        status: 'authenticated',
      },
    });
    expect(getByLabelText('Developer')).toBeInTheDocument();
  });

  it('marks the active nav item with aria-current=page when path matches', () => {
    const { getByLabelText } = renderWithProviders(<DarkSidebar />, {
      initialPath: '/cases',
      initialAuth: {
        user: { id: 'u', email: 'u@x', roles: ['admin'] },
        status: 'authenticated',
      },
    });
    expect(getByLabelText('Cases')).toHaveAttribute('aria-current', 'page');
  });

  it('toggle persists collapsed state to localStorage', async () => {
    localStorage.removeItem('wks.ui.sidebar.collapsed');
    const user = userEvent.setup();
    const { getByLabelText } = renderWithProviders(<DarkSidebar />, {
      initialAuth: {
        user: { id: 'u', email: 'u@x', roles: ['admin'] },
        status: 'authenticated',
      },
    });
    await user.click(getByLabelText(/collapse sidebar/i));
    expect(localStorage.getItem('wks.ui.sidebar.collapsed')).toBe('true');
  });
});
