import { apiFetch } from './client';

export interface LicenseStatus {
  state: 'valid' | 'oss' | 'expired' | 'degraded';
  tier: string;
  expiredAt: string | null; // backward-compat — same as before; set only when EXPIRED
  expiresAt: string | null; // NEW: set for VALID + EXPIRED when expiry is known
  licenseHolder: string | null; // NEW: JWT sub claim; null in OSS/degraded states
  publicKeyFingerprint: string; // NEW: SHA-256 hex of bundled verification key; always present
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

/**
 * Wire shape for one feature entry from {@code GET /api/license/features}.
 *
 * Story 7.3: added for Story 7-4 (License Status UI) to consume.
 */
export interface LicenseFeatureView {
  /** Stable wire string for this feature (e.g. {@code "auth.sso"}). */
  key: string;
  /** Human-readable description of the feature. */
  description: string;
  /** Tier names whose bundles include this feature by default. */
  bundleTiers: string[];
  /** Whether the feature is enabled under the current active license. */
  enabled: boolean;
}

/**
 * Response body from {@code GET /api/license/features}.
 */
export interface LicenseFeaturesDto {
  /** The active tier string for the current license (e.g. {@code "oss"}, {@code "enterprise"}). */
  tier: string;
  /** All registered license-gated features with coverage and current enabled state. */
  features: LicenseFeatureView[];
}

/**
 * Fetches all registered license-gated features with their tier coverage and current enabled state.
 * Calls {@code GET /api/license/features}.
 *
 * Intended to be consumed by the License Status UI (Story 7-4).
 */
export async function getLicenseFeatures(signal?: AbortSignal): Promise<LicenseFeaturesDto> {
  const result = await apiFetch<LicenseFeaturesDto>('/api/license/features', { signal });
  return result.data;
}
