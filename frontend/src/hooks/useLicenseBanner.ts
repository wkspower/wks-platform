import { useCallback, useEffect, useRef, useState } from 'react';

import { getLicenseStatus, type LicenseStatus } from '@/api/license';

const POLL_INTERVAL_MS = 60_000;

/**
 * Returns the sessionStorage key for the given banner state.
 * Dismiss is scoped by state so that a state transition re-shows the banner even in the same
 * browser session.
 */
function dismissKey(state: string): string {
  return `wks.licenseBanner.dismissed.${state}`;
}

function trySessionSet(key: string, value: string): void {
  try {
    sessionStorage.setItem(key, value);
  } catch {
    // Safari private mode — ignored; in-memory dismiss state still takes effect
  }
}

/**
 * Returns the number of calendar days from now until the given ISO-8601 instant.
 * Negative when the instant is in the past.
 */
function daysUntil(isoInstant: string): number {
  return Math.ceil((new Date(isoInstant).getTime() - Date.now()) / (1000 * 60 * 60 * 24));
}

export interface UseLicenseBannerResult {
  /** The current license state string, or {@code null} while the initial fetch is in flight. */
  state: LicenseStatus['state'] | null;
  /** ISO-8601 expiry string when state is {@code "expired"}, otherwise {@code null}. */
  expiredAt: string | null;
  /**
   * ISO-8601 expiry string when the license is expiring soon (within 30 days), otherwise
   * {@code null}.
   */
  expiresAt: string | null;
  /** Whether the current license state is OSS (no license file present). */
  isOss: boolean;
  /** Whether the license is valid but expiring within 30 days. */
  isExpiringSoon: boolean;
  /** Whether the user dismissed the banner this session. */
  isDismissed: boolean;
  /** Suppress the banner for the rest of the browser session (sessionStorage-backed). */
  dismiss: () => void;
}

/**
 * Fetches {@code GET /api/license/status} on mount and re-fetches every 60 seconds.
 *
 * The backend polls its license file every 30 seconds, so a 60-second frontend interval
 * catches the next reload cycle without hammering the API.
 *
 * Banner states shown (in priority order):
 *  - {@code "expired"} — amber/warning, existing behavior preserved.
 *  - {@code "degraded"} — amber/warning, existing behavior preserved.
 *  - {@code "expiring_soon"} — state "valid" with expiresAt within 30 days.
 *  - {@code "oss"} — info banner (Story 7-4: OSS mode is now surfaced so operators know
 *    the platform is running without a license file).
 */
export function useLicenseBanner(): UseLicenseBannerResult {
  const [licenseState, setLicenseState] = useState<LicenseStatus['state'] | null>(null);
  const [expiredAt, setExpiredAt] = useState<string | null>(null);
  const [expiresAt, setExpiresAt] = useState<string | null>(null);
  const [isOss, setIsOss] = useState(false);
  const [isExpiringSoon, setIsExpiringSoon] = useState(false);
  const [isDismissed, setIsDismissed] = useState(false);

  // Holds the latest licenseState for use inside the dismiss callback without stale closure.
  const licenseStateRef = useRef<LicenseStatus['state'] | null>(null);
  licenseStateRef.current = licenseState;
  const isExpiringSoonRef = useRef(false);
  isExpiringSoonRef.current = isExpiringSoon;

  useEffect(() => {
    const controller = new AbortController();
    const { signal } = controller;

    async function fetchStatus(): Promise<void> {
      try {
        const status = await getLicenseStatus(signal);
        setLicenseState(status.state);
        setExpiredAt(status.expiredAt);
        setExpiresAt(status.expiresAt ?? null);

        const expiringSoon =
          status.state === 'valid' &&
          status.expiresAt !== null &&
          daysUntil(status.expiresAt) <= 30;
        setIsExpiringSoon(expiringSoon);
        setIsOss(status.state === 'oss');

        if (status.state === 'valid' && !expiringSoon) {
          // Clear stale dismiss keys so a subsequent degraded/expired/expiring-soon state
          // re-shows the banner.
          sessionStorage.removeItem('wks.licenseBanner.dismissed.expired');
          sessionStorage.removeItem('wks.licenseBanner.dismissed.degraded');
          sessionStorage.removeItem('wks.licenseBanner.dismissed.expiring_soon');
          setIsDismissed(false);
        } else {
          // For expiring-soon use a dedicated dismiss key distinct from 'valid'.
          const bannerKey = expiringSoon ? 'expiring_soon' : status.state;
          const dismissed = sessionStorage.getItem(dismissKey(bannerKey)) === '1';
          setIsDismissed(dismissed);
        }
      } catch {
        // Network or auth error — don't change current state; banner stays as-is.
      }
    }

    void fetchStatus();
    const id = setInterval(() => void fetchStatus(), POLL_INTERVAL_MS);

    return () => {
      controller.abort();
      clearInterval(id);
    };
  }, []);

  const dismiss = useCallback(() => {
    const current = licenseStateRef.current;
    if (current) {
      const bannerKey = isExpiringSoonRef.current ? 'expiring_soon' : current;
      trySessionSet(dismissKey(bannerKey), '1');
    }
    setIsDismissed(true);
  }, []);

  return { state: licenseState, expiredAt, expiresAt, isOss, isExpiringSoon, isDismissed, dismiss };
}
