import userEvent from '@testing-library/user-event';
import { HttpResponse, http } from 'msw';
import { describe, expect, it } from 'vitest';

import type { LicenseStatus } from '@/api/license';
import { renderWithProviders } from '@/test/renderWithProviders';
import { server } from '@/test/server';
import type { ApiSuccessEnvelope } from '@/types/api';

import { LicenseBanner } from './LicenseBanner';

const FAKE_FINGERPRINT = 'a3b4c5d6e7f801234567890abcdef0123456789abcdef0123456789abcdef01';

function licenseMswHandler(status: LicenseStatus) {
  return http.get('/api/license/status', () =>
    HttpResponse.json<ApiSuccessEnvelope<LicenseStatus>>(
      { data: status, meta: {} },
      { status: 200 },
    ),
  );
}

describe('LicenseBanner', () => {
  it('renders OSS info banner when state is "oss"', async () => {
    server.use(
      licenseMswHandler({
        state: 'oss',
        tier: 'oss',
        expiredAt: null,
        expiresAt: null,
        licenseHolder: null,
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { findByRole } = renderWithProviders(<LicenseBanner />);
    const alert = await findByRole('alert');
    expect(alert).toBeInTheDocument();
    expect(alert).toHaveTextContent(/OSS mode/i);
    expect(alert).toHaveAttribute('aria-live', 'polite');
  });

  it('renders nothing when state is "valid" (no expiry)', async () => {
    server.use(
      licenseMswHandler({
        state: 'valid',
        tier: 'enterprise',
        expiredAt: null,
        expiresAt: null,
        licenseHolder: 'Acme Corp',
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { container } = renderWithProviders(<LicenseBanner />);
    await new Promise((r) => setTimeout(r, 0));
    expect(container.querySelector('[role="alert"]')).toBeNull();
  });

  it('renders banner with expiry date when state is "expired"', async () => {
    server.use(
      licenseMswHandler({
        state: 'expired',
        tier: 'oss',
        expiredAt: '2025-12-31T00:00:00Z',
        expiresAt: '2025-12-31T00:00:00Z',
        licenseHolder: null,
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { findByRole } = renderWithProviders(<LicenseBanner />);
    const alert = await findByRole('alert');
    expect(alert).toBeInTheDocument();
    expect(alert).toHaveTextContent(/operating in OSS mode/i);
    expect(alert).toHaveTextContent('Dec 31, 2025');
  });

  it('renders banner without date when state is "degraded"', async () => {
    server.use(
      licenseMswHandler({
        state: 'degraded',
        tier: 'oss',
        expiredAt: null,
        expiresAt: null,
        licenseHolder: null,
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { findByRole } = renderWithProviders(<LicenseBanner />);
    const alert = await findByRole('alert');
    expect(alert).toBeInTheDocument();
    expect(alert).toHaveTextContent(/could not be verified/i);
  });

  it('hides the banner after clicking dismiss', async () => {
    const user = userEvent.setup();
    server.use(
      licenseMswHandler({
        state: 'expired',
        tier: 'oss',
        expiredAt: '2025-01-01T00:00:00Z',
        expiresAt: '2025-01-01T00:00:00Z',
        licenseHolder: null,
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { findByRole, queryByRole, getByLabelText } = renderWithProviders(<LicenseBanner />);
    expect(await findByRole('alert')).toBeInTheDocument();
    await user.click(getByLabelText(/dismiss/i));
    expect(queryByRole('alert')).toBeNull();
  });

  it('has role="alert" and aria-live="assertive" for degraded state', async () => {
    server.use(
      licenseMswHandler({
        state: 'degraded',
        tier: 'oss',
        expiredAt: null,
        expiresAt: null,
        licenseHolder: null,
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { findByRole } = renderWithProviders(<LicenseBanner />);
    const alert = await findByRole('alert');
    expect(alert).toHaveAttribute('aria-live', 'assertive');
  });

  it('renders expiring-soon warning when state is valid with expiresAt within 30 days', async () => {
    // 15 days from now
    const soon = new Date(Date.now() + 15 * 24 * 60 * 60 * 1000).toISOString();
    server.use(
      licenseMswHandler({
        state: 'valid',
        tier: 'enterprise',
        expiredAt: null,
        expiresAt: soon,
        licenseHolder: 'Acme Corp',
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { findByRole } = renderWithProviders(<LicenseBanner />);
    const alert = await findByRole('alert');
    expect(alert).toBeInTheDocument();
    expect(alert).toHaveAttribute('aria-live', 'assertive');
    expect(alert).toHaveTextContent(/renew to avoid service interruption/i);
  });

  it('renders nothing for valid state with expiresAt beyond 30 days', async () => {
    // 60 days from now
    const far = new Date(Date.now() + 60 * 24 * 60 * 60 * 1000).toISOString();
    server.use(
      licenseMswHandler({
        state: 'valid',
        tier: 'enterprise',
        expiredAt: null,
        expiresAt: far,
        licenseHolder: 'Acme Corp',
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { container } = renderWithProviders(<LicenseBanner />);
    await new Promise((r) => setTimeout(r, 0));
    expect(container.querySelector('[role="alert"]')).toBeNull();
  });

  it('expiring-soon banner can be dismissed', async () => {
    const user = userEvent.setup();
    const soon = new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toISOString();
    server.use(
      licenseMswHandler({
        state: 'valid',
        tier: 'enterprise',
        expiredAt: null,
        expiresAt: soon,
        licenseHolder: 'Acme Corp',
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { findByRole, queryByRole, getByLabelText } = renderWithProviders(<LicenseBanner />);
    expect(await findByRole('alert')).toBeInTheDocument();
    await user.click(getByLabelText(/dismiss/i));
    expect(queryByRole('alert')).toBeNull();
  });
});
