import { act, renderHook, waitFor } from '@testing-library/react';
import { HttpResponse, http } from 'msw';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import type { LicenseStatus } from '@/api/license';
import { server } from '@/test/server';
import type { ApiSuccessEnvelope } from '@/types/api';

import { useLicenseBanner } from './useLicenseBanner';

const FAKE_FINGERPRINT = 'a3b4c5d6e7f801234567890abcdef0123456789abcdef0123456789abcdef01';

function handler(status: LicenseStatus) {
  return http.get('/api/license/status', () =>
    HttpResponse.json<ApiSuccessEnvelope<LicenseStatus>>(
      { data: status, meta: {} },
      { status: 200 },
    ),
  );
}

describe('useLicenseBanner', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('returns null state initially, then resolves to the fetched state', async () => {
    server.use(
      handler({
        state: 'degraded',
        tier: 'oss',
        expiredAt: null,
        expiresAt: null,
        licenseHolder: null,
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { result } = renderHook(() => useLicenseBanner());
    expect(result.current.state).toBeNull();
    await waitFor(() => expect(result.current.state).toBe('degraded'));
  });

  it('exposes expiredAt for expired state', async () => {
    server.use(
      handler({
        state: 'expired',
        tier: 'oss',
        expiredAt: '2025-12-31T00:00:00Z',
        expiresAt: '2025-12-31T00:00:00Z',
        licenseHolder: null,
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { result } = renderHook(() => useLicenseBanner());
    await waitFor(() => expect(result.current.state).toBe('expired'));
    expect(result.current.expiredAt).toBe('2025-12-31T00:00:00Z');
  });

  it('isDismissed is false by default', async () => {
    server.use(
      handler({
        state: 'expired',
        tier: 'oss',
        expiredAt: '2025-12-31T00:00:00Z',
        expiresAt: '2025-12-31T00:00:00Z',
        licenseHolder: null,
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { result } = renderHook(() => useLicenseBanner());
    await waitFor(() => expect(result.current.state).toBe('expired'));
    expect(result.current.isDismissed).toBe(false);
  });

  it('dismiss() sets isDismissed to true and writes sessionStorage', async () => {
    server.use(
      handler({
        state: 'expired',
        tier: 'oss',
        expiredAt: '2025-12-31T00:00:00Z',
        expiresAt: '2025-12-31T00:00:00Z',
        licenseHolder: null,
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { result } = renderHook(() => useLicenseBanner());
    await waitFor(() => expect(result.current.state).toBe('expired'));

    act(() => result.current.dismiss());

    expect(result.current.isDismissed).toBe(true);
    expect(sessionStorage.getItem('wks.licenseBanner.dismissed.expired')).toBe('1');
  });

  it('isDismissed reads from sessionStorage on state transition', async () => {
    // Pre-seed dismiss for "degraded"
    sessionStorage.setItem('wks.licenseBanner.dismissed.degraded', '1');
    server.use(
      handler({
        state: 'degraded',
        tier: 'oss',
        expiredAt: null,
        expiresAt: null,
        licenseHolder: null,
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { result } = renderHook(() => useLicenseBanner());
    await waitFor(() => expect(result.current.state).toBe('degraded'));
    expect(result.current.isDismissed).toBe(true);
  });

  it('dismiss key is state-scoped — different states do not share dismiss', async () => {
    // Dismiss "expired", then check "degraded" is not dismissed
    sessionStorage.setItem('wks.licenseBanner.dismissed.expired', '1');
    server.use(
      handler({
        state: 'degraded',
        tier: 'oss',
        expiredAt: null,
        expiresAt: null,
        licenseHolder: null,
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { result } = renderHook(() => useLicenseBanner());
    await waitFor(() => expect(result.current.state).toBe('degraded'));
    expect(result.current.isDismissed).toBe(false);
  });

  // Story 7-4 additions —————————————————————————————————————————————————————

  it('returns isOss=true when state is "oss"', async () => {
    server.use(
      handler({
        state: 'oss',
        tier: 'oss',
        expiredAt: null,
        expiresAt: null,
        licenseHolder: null,
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { result } = renderHook(() => useLicenseBanner());
    await waitFor(() => expect(result.current.state).toBe('oss'));
    expect(result.current.isOss).toBe(true);
    expect(result.current.isExpiringSoon).toBe(false);
  });

  it('returns isExpiringSoon=true when valid with expiresAt within 30 days', async () => {
    const soon = new Date(Date.now() + 15 * 24 * 60 * 60 * 1000).toISOString();
    server.use(
      handler({
        state: 'valid',
        tier: 'enterprise',
        expiredAt: null,
        expiresAt: soon,
        licenseHolder: 'Acme Corp',
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { result } = renderHook(() => useLicenseBanner());
    await waitFor(() => expect(result.current.state).toBe('valid'));
    expect(result.current.isExpiringSoon).toBe(true);
    expect(result.current.isOss).toBe(false);
  });

  it('returns isExpiringSoon=false when valid with expiresAt beyond 30 days', async () => {
    const far = new Date(Date.now() + 60 * 24 * 60 * 60 * 1000).toISOString();
    server.use(
      handler({
        state: 'valid',
        tier: 'enterprise',
        expiredAt: null,
        expiresAt: far,
        licenseHolder: 'Acme Corp',
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { result } = renderHook(() => useLicenseBanner());
    await waitFor(() => expect(result.current.state).toBe('valid'));
    expect(result.current.isExpiringSoon).toBe(false);
  });

  it('expiring-soon dismiss uses "expiring_soon" sessionStorage key', async () => {
    const soon = new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toISOString();
    server.use(
      handler({
        state: 'valid',
        tier: 'enterprise',
        expiredAt: null,
        expiresAt: soon,
        licenseHolder: 'Acme Corp',
        publicKeyFingerprint: FAKE_FINGERPRINT,
      }),
    );
    const { result } = renderHook(() => useLicenseBanner());
    await waitFor(() => expect(result.current.isExpiringSoon).toBe(true));

    act(() => result.current.dismiss());

    expect(result.current.isDismissed).toBe(true);
    expect(sessionStorage.getItem('wks.licenseBanner.dismissed.expiring_soon')).toBe('1');
  });
});
