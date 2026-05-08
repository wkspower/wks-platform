import { useEffect, useState } from 'react';

import {
  getLicenseFeatures,
  getLicenseStatus,
  type LicenseFeaturesDto,
  type LicenseStatus,
} from '@/api/license';

export interface UseLicenseStatusResult {
  status: LicenseStatus | null;
  features: LicenseFeaturesDto | null;
  loading: boolean;
  error: boolean;
}

/**
 * Fetches both {@code GET /api/license/status} and {@code GET /api/license/features} in parallel
 * on mount. No polling — the admin page is a point-in-time view, not a live banner.
 *
 * Provides {@code retry()} by re-incrementing a counter that triggers the effect.
 */
export function useLicenseStatus(): UseLicenseStatusResult & { retry: () => void } {
  const [status, setStatus] = useState<LicenseStatus | null>(null);
  const [features, setFeatures] = useState<LicenseFeaturesDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    const { signal } = controller;

    setLoading(true);
    setError(false);

    Promise.all([getLicenseStatus(signal), getLicenseFeatures(signal)])
      .then(([s, f]) => {
        setStatus(s);
        setFeatures(f);
        setLoading(false);
      })
      .catch(() => {
        if (!signal.aborted) {
          setError(true);
          setLoading(false);
        }
      });

    return () => {
      controller.abort();
    };
  }, [retryCount]);

  const retry = () => setRetryCount((c) => c + 1);

  return { status, features, loading, error, retry };
}
