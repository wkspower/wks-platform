import type { LicenseStatus } from '@/api/license';
import { DeferredBadge } from '@/components/license/DeferredBadge';
import { Button } from '@/components/ui/Button';
import { ErrorState } from '@/components/ui/ErrorState';
import { useLicenseStatus } from '@/hooks/useLicenseStatus';
import { t } from '@/i18n';
import { formatDate } from '@/lib/formatDate';

/**
 * Feature keys whose implementation is deferred to a future story.
 * These rows show a Deferred badge in the feature table to set honest expectations.
 * Story 7-6 AC-5.
 */
const DEFERRED_FEATURES: Record<string, string> = {
  'audit.export': '15-7-audit-export-polish',
  'audit.checksums': '15-N-audit-checksums',
};

// ---------------------------------------------------------------------------
// Tier badge — color-coded with mandatory text label (AC4: no color-only)
// ---------------------------------------------------------------------------

const TIER_STYLES: Record<string, string> = {
  oss: 'bg-[var(--muted)] text-[var(--muted-foreground)]',
  team: 'bg-[var(--secondary)]/15 text-[var(--secondary)]',
  enterprise: 'bg-[var(--primary)]/15 text-[var(--primary)]',
  demo: 'bg-[var(--warning)]/15 text-[var(--warning)]',
};

function TierBadge({ tier }: { tier: string }) {
  const style = TIER_STYLES[tier] ?? TIER_STYLES['oss'];
  const label = t(`license.tier.${tier}` as Parameters<typeof t>[0]) ?? tier.toUpperCase();
  return (
    <span
      className={`inline-flex items-center rounded-[var(--radius-sm)] px-2 py-0.5 text-xs font-semibold ${style}`}
    >
      {label}
    </span>
  );
}

// ---------------------------------------------------------------------------
// State chip
// ---------------------------------------------------------------------------

const STATE_STYLES: Record<string, string> = {
  valid: 'bg-[var(--success)]/15 text-[var(--success)]',
  oss: 'bg-[var(--muted)] text-[var(--muted-foreground)]',
  expired: 'bg-[var(--warning)]/15 text-[var(--warning)]',
  degraded: 'bg-[var(--warning)]/15 text-[var(--warning)]',
};

function StateChip({ state }: { state: LicenseStatus['state'] }) {
  const style = STATE_STYLES[state] ?? STATE_STYLES['oss'];
  const label = t(`license.state.${state}` as Parameters<typeof t>[0]) ?? state;
  return (
    <span
      className={`inline-flex items-center rounded-[var(--radius-sm)] px-2 py-0.5 text-xs font-medium ${style}`}
    >
      {label}
    </span>
  );
}

// ---------------------------------------------------------------------------
// Label/value row
// ---------------------------------------------------------------------------

function LabelValue({ label, value, title }: { label: string; value: string; title?: string }) {
  return (
    <div className="flex flex-col gap-[var(--space-1)] sm:flex-row sm:items-baseline sm:gap-[var(--space-4)]">
      <dt className="w-40 shrink-0 text-sm font-medium text-[var(--muted-foreground)]">{label}</dt>
      <dd className="text-sm text-[var(--foreground)]" title={title}>
        {value}
      </dd>
    </div>
  );
}

// ---------------------------------------------------------------------------
// LicenseStatusPage
// ---------------------------------------------------------------------------

export function LicenseStatusPage() {
  const { status, features, loading, error, retry } = useLicenseStatus();

  if (loading) {
    return (
      <section aria-label={t('page.admin.license.title')}>
        <h1 className="font-heading text-2xl font-semibold">{t('page.admin.license.title')}</h1>
        <p className="mt-[var(--space-4)] text-sm text-[var(--muted-foreground)]">
          {t('license.status.loading')}
        </p>
      </section>
    );
  }

  if (error || status === null) {
    return (
      <section aria-label={t('page.admin.license.title')}>
        <h1 className="font-heading text-2xl font-semibold">{t('page.admin.license.title')}</h1>
        <ErrorState
          className="mt-[var(--space-6)]"
          headline={t('license.status.error')}
          action={
            <Button variant="secondary" onClick={retry}>
              {t('license.status.retry')}
            </Button>
          }
        />
      </section>
    );
  }

  const holderDisplay = status.licenseHolder ?? t('license.status.holder.oss');
  const expiresDisplay = status.expiresAt
    ? formatDate(status.expiresAt)
    : t('license.status.expires.never');
  const fingerprintShort = status.publicKeyFingerprint.slice(0, 16);
  const fingerprintFull = status.publicKeyFingerprint;

  return (
    <section aria-label={t('page.admin.license.title')}>
      <h1 className="font-heading text-2xl font-semibold">{t('page.admin.license.title')}</h1>

      {/* Status card */}
      <div className="mt-[var(--space-6)] rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--card)] p-[var(--space-6)]">
        <div className="mb-[var(--space-4)] flex items-center gap-[var(--space-3)]">
          <TierBadge tier={status.tier} />
          <StateChip state={status.state} />
        </div>
        <dl className="flex flex-col gap-[var(--space-3)]">
          <LabelValue label={t('license.status.holder')} value={holderDisplay} />
          <LabelValue label={t('license.status.expires')} value={expiresDisplay} />
          <LabelValue
            label={t('license.status.fingerprint')}
            value={fingerprintShort}
            title={fingerprintFull}
          />
        </dl>
      </div>

      {/* Feature list */}
      {features !== null && (
        <div className="mt-[var(--space-6)]">
          <h2 className="font-heading text-lg font-semibold">
            {t('license.status.features.title')}
          </h2>
          <div className="mt-[var(--space-4)] overflow-x-auto rounded-[var(--radius-md)] border border-[var(--border)]">
            <table className="w-full text-sm" aria-label={t('license.status.features.tableLabel')}>
              <thead className="bg-[var(--muted)] text-[var(--muted-foreground)]">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">
                    {t('license.status.features.col.feature')}
                  </th>
                  <th className="px-4 py-3 text-left font-medium">
                    {t('license.status.features.col.description')}
                  </th>
                  <th className="px-4 py-3 text-left font-medium">
                    {t('license.status.features.col.tiers')}
                  </th>
                  <th className="px-4 py-3 text-center font-medium">
                    {t('license.status.features.col.active')}
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[var(--border)] bg-[var(--card)]">
                {features.features.map((f) => {
                  const deferredTrackingStory = DEFERRED_FEATURES[f.key];
                  return (
                    <tr key={f.key}>
                      <td className="px-4 py-3">
                        <code className="rounded bg-[var(--muted)] px-1 py-0.5 text-xs">
                          {f.key}
                        </code>
                      </td>
                      <td className="px-4 py-3 text-[var(--foreground)]">
                        {f.description}
                        {deferredTrackingStory !== undefined && (
                          <DeferredBadge trackingStory={deferredTrackingStory} />
                        )}
                      </td>
                      <td className="px-4 py-3 text-[var(--muted-foreground)]">
                        {f.bundleTiers.join(', ')}
                      </td>
                      <td
                        className="px-4 py-3 text-center"
                        aria-label={
                          f.enabled
                            ? t('license.status.features.enabled')
                            : t('license.status.features.disabled')
                        }
                      >
                        {f.enabled ? '✓' : '✗'}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </section>
  );
}
