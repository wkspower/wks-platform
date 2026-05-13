import { HttpResponse, http } from 'msw';
import { describe, expect, it } from 'vitest';

import type { LicenseFeaturesDto, LicenseStatus } from '@/api/license';
import { renderWithProviders } from '@/test/renderWithProviders';
import { server } from '@/test/server';
import type { ApiSuccessEnvelope } from '@/types/api';

import { LicenseStatusPage } from './LicenseStatusPage';

// ---------------------------------------------------------------------------
// MSW handler helpers
// ---------------------------------------------------------------------------

const FAKE_FINGERPRINT = 'a3b4c5d6e7f801234567890abcdef0123456789abcdef0123456789abcdef01';

function licenseStatusHandler(status: LicenseStatus) {
  return http.get('/api/license/status', () =>
    HttpResponse.json<ApiSuccessEnvelope<LicenseStatus>>(
      { data: status, meta: {} },
      { status: 200 },
    ),
  );
}

function licenseFeaturesHandler(dto: LicenseFeaturesDto) {
  return http.get('/api/license/features', () =>
    HttpResponse.json<ApiSuccessEnvelope<LicenseFeaturesDto>>(
      { data: dto, meta: {} },
      { status: 200 },
    ),
  );
}

// A realistic "enterprise" status
const enterpriseStatus: LicenseStatus = {
  state: 'valid',
  tier: 'enterprise',
  expiredAt: null,
  expiresAt: '2027-06-01T00:00:00Z',
  licenseHolder: 'Acme Corp',
  publicKeyFingerprint: FAKE_FINGERPRINT,
};

// All 4 Phase-0 EE features
const allFeatures: LicenseFeaturesDto = {
  tier: 'enterprise',
  features: [
    {
      key: 'auth.sso',
      description: 'Single sign-on via SAML or OIDC',
      bundleTiers: ['enterprise', 'demo'],
      enabled: true,
    },
    {
      key: 'white-label',
      description: 'Custom branding (logo, colours, domain)',
      bundleTiers: ['enterprise', 'demo'],
      enabled: true,
    },
    {
      key: 'audit.export',
      description: 'Export audit log to CSV / SIEM',
      bundleTiers: ['team', 'enterprise', 'demo'],
      enabled: true,
    },
    {
      key: 'audit.checksums',
      description: 'Tamper-evident SHA-256 checksums on audit entries',
      bundleTiers: ['enterprise', 'demo'],
      enabled: false,
    },
  ],
};

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe('LicenseStatusPage', () => {
  it('renders loading state before fetch completes', () => {
    // Install a handler that never resolves so the component stays in loading
    server.use(http.get('/api/license/status', () => new Promise(() => undefined)));
    server.use(http.get('/api/license/features', () => new Promise(() => undefined)));
    const { getByText } = renderWithProviders(<LicenseStatusPage />);
    expect(getByText(/loading/i)).toBeInTheDocument();
  });

  it('renders tier badge with text label', async () => {
    server.use(licenseStatusHandler(enterpriseStatus), licenseFeaturesHandler(allFeatures));
    const { findByText } = renderWithProviders(<LicenseStatusPage />);
    // Badge must contain the text "Enterprise" (not color-only)
    const badge = await findByText('Enterprise');
    expect(badge).toBeInTheDocument();
  });

  it('renders license holder from status response', async () => {
    server.use(licenseStatusHandler(enterpriseStatus), licenseFeaturesHandler(allFeatures));
    const { findByText } = renderWithProviders(<LicenseStatusPage />);
    expect(await findByText('Acme Corp')).toBeInTheDocument();
  });

  it('renders feature list with enabled/disabled accessible labels', async () => {
    server.use(licenseStatusHandler(enterpriseStatus), licenseFeaturesHandler(allFeatures));
    const { findAllByRole } = renderWithProviders(<LicenseStatusPage />);
    const table = await findAllByRole('table');
    expect(table.length).toBeGreaterThan(0);

    // Find cells with aria-label of "Enabled" or "Disabled"
    const enabledCells = document.querySelectorAll('[aria-label="Enabled"]');
    const disabledCells = document.querySelectorAll('[aria-label="Disabled"]');
    expect(enabledCells.length + disabledCells.length).toBe(allFeatures.features.length);
  });

  it('renders key fingerprint shortform and full in title', async () => {
    server.use(licenseStatusHandler(enterpriseStatus), licenseFeaturesHandler(allFeatures));
    const { findByText } = renderWithProviders(<LicenseStatusPage />);
    // Short form is first 16 chars
    const shortDisplay = await findByText(FAKE_FINGERPRINT.slice(0, 16));
    expect(shortDisplay).toBeInTheDocument();
    // Full fingerprint is in the title attribute
    expect(shortDisplay).toHaveAttribute('title', FAKE_FINGERPRINT);
  });

  it('feature list row count matches feature registry (4 Phase-0 features)', async () => {
    server.use(licenseStatusHandler(enterpriseStatus), licenseFeaturesHandler(allFeatures));
    const { findAllByRole } = renderWithProviders(<LicenseStatusPage />);
    // tbody rows
    const rows = await findAllByRole('row');
    // rows includes the thead row + 4 tbody rows = 5 total
    expect(rows.length).toBe(5);
  });

  // ---------------------------------------------------------------------------
  // Story 7-6 AC-5: all three new feature rows present + Deferred badge for deferred features
  // ---------------------------------------------------------------------------

  it('showsAllThreeNewFeatureRowsWithDeferredBadge', async () => {
    // OSS license — all 4 features disabled; white-label, audit.export, audit.checksums present.
    const ossStatus: LicenseStatus = {
      state: 'oss',
      tier: 'oss',
      expiredAt: null,
      expiresAt: null,
      licenseHolder: null,
      publicKeyFingerprint: FAKE_FINGERPRINT,
    };
    const ossFeatures: LicenseFeaturesDto = {
      tier: 'oss',
      features: [
        {
          key: 'auth.sso',
          description: 'SSO/SAML authentication (FR28)',
          bundleTiers: ['enterprise', 'demo'],
          enabled: false,
        },
        {
          key: 'white-label',
          description: 'White-labeling / custom branding (FR34)',
          bundleTiers: ['enterprise', 'demo'],
          enabled: false,
        },
        {
          key: 'audit.export',
          description: 'Audit log export (FR43)',
          bundleTiers: ['enterprise', 'demo'],
          enabled: false,
        },
        {
          key: 'audit.checksums',
          description: 'Tamper-evident audit checksums (FR46)',
          bundleTiers: ['team', 'enterprise', 'demo'],
          enabled: false,
        },
      ],
    };
    server.use(licenseStatusHandler(ossStatus), licenseFeaturesHandler(ossFeatures));
    const { findAllByRole, findAllByText } = renderWithProviders(<LicenseStatusPage />);

    // All 4 feature rows should be present (1 thead row + 4 tbody = 5 total rows)
    const rows = await findAllByRole('row');
    expect(rows.length).toBe(5);

    // white-label row is present
    const whiteLabelCodes = await findAllByText('white-label');
    expect(whiteLabelCodes.length).toBeGreaterThan(0);

    // audit.export row is present
    const auditExportCodes = await findAllByText('audit.export');
    expect(auditExportCodes.length).toBeGreaterThan(0);

    // audit.checksums row is present
    const auditChecksumsCodes = await findAllByText('audit.checksums');
    expect(auditChecksumsCodes.length).toBeGreaterThan(0);

    // Deferred badges appear for audit.export and audit.checksums (NOT white-label)
    // The DeferredBadge renders text "Deferred" for each deferred feature.
    const deferredBadges = await findAllByText('Deferred');
    // 2 deferred features: audit.export and audit.checksums
    expect(deferredBadges.length).toBe(2);
  });

  it('renders OSS mode label for license holder when null', async () => {
    const ossStatus: LicenseStatus = {
      state: 'oss',
      tier: 'oss',
      expiredAt: null,
      expiresAt: null,
      licenseHolder: null,
      publicKeyFingerprint: FAKE_FINGERPRINT,
    };
    const ossFeatures: LicenseFeaturesDto = {
      tier: 'oss',
      features: allFeatures.features.map((f) => ({ ...f, enabled: false })),
    };
    server.use(licenseStatusHandler(ossStatus), licenseFeaturesHandler(ossFeatures));
    const { findByText } = renderWithProviders(<LicenseStatusPage />);
    expect(await findByText(/OSS mode/i)).toBeInTheDocument();
  });
});
