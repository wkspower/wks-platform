import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { sessionBus } from '@/api/sessionBus';
import { renderWithProviders } from '@/test/renderWithProviders';

import { SessionExpiryBanner } from './SessionExpiryBanner';

describe('SessionExpiryBanner', () => {
  it('renders nothing when not expired', () => {
    const { container } = renderWithProviders(<SessionExpiryBanner />);
    expect(container.querySelector('[role="alert"]')).toBeNull();
  });

  it('renders on session-expired, hides on dismiss, re-raises on next emit', async () => {
    const user = userEvent.setup();
    const { findByRole, getByLabelText, queryByRole } = renderWithProviders(
      <SessionExpiryBanner />,
    );
    sessionBus.emit({ requestPath: '/api/x' });
    expect(await findByRole('alert')).toHaveTextContent(/session has expired/i);
    await user.click(getByLabelText(/dismiss/i));
    expect(queryByRole('alert')).toBeNull();
    sessionBus.emit({ requestPath: '/api/y' });
    expect(await findByRole('alert')).toBeInTheDocument();
  });
});
