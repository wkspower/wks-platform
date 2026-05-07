import userEvent from '@testing-library/user-event';
import { HttpResponse, http } from 'msw';
import { describe, expect, it } from 'vitest';

import type { LicenseStatus } from '@/api/license';
import { renderWithProviders } from '@/test/renderWithProviders';
import { server } from '@/test/server';
import type { ApiSuccessEnvelope } from '@/types/api';

import { LicenseBanner } from './LicenseBanner';

function licenseMswHandler(status: LicenseStatus) {
  return http.get('/api/license/status', () =>
    HttpResponse.json<ApiSuccessEnvelope<LicenseStatus>>(
      { data: status, meta: {} },
      { status: 200 },
    ),
  );
}

describe('LicenseBanner', () => {
  it('renders nothing when state is "oss"', async () => {
    server.use(licenseMswHandler({ state: 'oss', tier: 'oss', expiredAt: null }));
    const { container } = renderWithProviders(<LicenseBanner />);
    // Wait a tick for the fetch to settle
    await new Promise((r) => setTimeout(r, 0));
    expect(container.querySelector('[role="alert"]')).toBeNull();
  });

  it('renders nothing when state is "valid"', async () => {
    server.use(licenseMswHandler({ state: 'valid', tier: 'enterprise', expiredAt: null }));
    const { container } = renderWithProviders(<LicenseBanner />);
    await new Promise((r) => setTimeout(r, 0));
    expect(container.querySelector('[role="alert"]')).toBeNull();
  });

  it('renders banner with expiry date when state is "expired"', async () => {
    server.use(
      licenseMswHandler({ state: 'expired', tier: 'oss', expiredAt: '2025-12-31T00:00:00Z' }),
    );
    const { findByRole } = renderWithProviders(<LicenseBanner />);
    const alert = await findByRole('alert');
    expect(alert).toBeInTheDocument();
    expect(alert).toHaveTextContent(/operating in OSS mode/i);
  });

  it('renders banner without date when state is "degraded"', async () => {
    server.use(licenseMswHandler({ state: 'degraded', tier: 'oss', expiredAt: null }));
    const { findByRole } = renderWithProviders(<LicenseBanner />);
    const alert = await findByRole('alert');
    expect(alert).toBeInTheDocument();
    expect(alert).toHaveTextContent(/could not be verified/i);
  });

  it('hides the banner after clicking dismiss', async () => {
    const user = userEvent.setup();
    server.use(
      licenseMswHandler({ state: 'expired', tier: 'oss', expiredAt: '2025-01-01T00:00:00Z' }),
    );
    const { findByRole, queryByRole, getByLabelText } = renderWithProviders(<LicenseBanner />);
    expect(await findByRole('alert')).toBeInTheDocument();
    await user.click(getByLabelText(/dismiss/i));
    expect(queryByRole('alert')).toBeNull();
  });

  it('has role="alert" and aria-live="assertive"', async () => {
    server.use(licenseMswHandler({ state: 'degraded', tier: 'oss', expiredAt: null }));
    const { findByRole } = renderWithProviders(<LicenseBanner />);
    const alert = await findByRole('alert');
    expect(alert).toHaveAttribute('aria-live', 'assertive');
  });
});
