import { useCallback, useEffect, useState } from 'react';

import { getLicenseStatus, type LicenseStatus } from '@/api/license';

const POLL_INTERVAL_MS = 60_000;

/**
 * Returns `true` when the banner should be visible (expired or degraded state AND not dismissed).
 *
 * The dismiss key is scoped by state so that a state transition (e.g. expired → valid after
 * a license renewal, then back to expired for a different reason) re-shows the banner even
 * in the same browser session.
 */
function dismissKey(state: string): string {
  return `wks.licenseBanner.dismissed.${state}`;
}

export interface UseLicenseBannerResult {
  /** The current license state string, or {@code null} while the initial fetch is in flight. */
  state: LicenseStatus['state'] | null;
  /** ISO-8601 expiry string when state is {@code "expired"}, otherwise {@code null}. */
  expiredAt: string | null;
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
 * Banner is only shown for {@code "expired"} and {@code "degraded"} states. The {@code "oss"}
 * state is the expected default for users without a license file — showing a banner there
 * would be noise.
 */
export function useLicenseBanner(): UseLicenseBannerResult {
  const [licenseState, setLicenseState] = useState<LicenseStatus['state'] | null>(null);
  const [expiredAt, setExpiredAt] = useState<string | null>(null);
  const [isDismissed, setIsDismissed] = useState(false);

  const poll = useCallback(async () => {
    try {
      const status = await getLicenseStatus();
      setLicenseState(status.state);
      setExpiredAt(status.expiredAt);
      // Re-check sessionStorage: if the state changed, the old dismiss key won't match,
      // so the banner will re-appear for the new state.
      const dismissed = sessionStorage.getItem(dismissKey(status.state)) === '1';
      setIsDismissed(dismissed);
    } catch {
      // Network or auth error — don't change current state; banner stays as-is.
    }
  }, []);

  // Initial fetch
  useEffect(() => {
    void poll();
  }, [poll]);

  // Polling interval
  useEffect(() => {
    const id = setInterval(() => void poll(), POLL_INTERVAL_MS);
    return () => clearInterval(id);
  }, [poll]);

  const dismiss = useCallback(() => {
    if (licenseState) {
      sessionStorage.setItem(dismissKey(licenseState), '1');
    }
    setIsDismissed(true);
  }, [licenseState]);

  return { state: licenseState, expiredAt, isDismissed, dismiss };
}
