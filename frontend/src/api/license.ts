import { apiFetch } from './client';

export interface LicenseStatus {
  state: 'valid' | 'oss' | 'expired' | 'degraded';
  tier: string;
  expiredAt: string | null;
}

/**
 * Fetches the current license status from {@code GET /api/license/status}.
 *
 * The backend returns a standard WKS envelope — {@code apiFetch} unwraps it.
 * State semantics:
 *  - {@code "valid"}    — non-expired license loaded; no banner shown.
 *  - {@code "oss"}      — no license file present; no banner shown (expected default).
 *  - {@code "expired"}  — valid signature but past expiry; banner with date shown.
 *  - {@code "degraded"} — file present but unverifiable; banner without date shown.
 */
export async function getLicenseStatus(signal?: AbortSignal): Promise<LicenseStatus> {
  const result = await apiFetch<LicenseStatus>('/api/license/status', { signal });
  return result.data;
}
