import { useQuery } from '@tanstack/react-query';
import { Shield, ShieldAlert, ShieldCheck } from 'lucide-react';

import { getLicenseFeatures, getLicenseStatus } from '@/api/license';
import { Badge } from '@/components/ui/Badge';
import { Spinner } from '@/components/ui/Spinner';
import { formatDateTime } from '@/lib/formatDate';

export function LicenseStatusPage() {
  const status = useQuery({ queryKey: ['license', 'status'], queryFn: () => getLicenseStatus() });
  const features = useQuery({ queryKey: ['license', 'features'], queryFn: () => getLicenseFeatures() });

  if (status.isLoading || features.isLoading) {
    return (
      <div className="grid place-items-center py-20">
        <Spinner className="size-6" />
      </div>
    );
  }
  if (status.isError || !status.data) {
    return <div className="px-6 py-12 text-center text-[var(--destructive)]">Failed to load license.</div>;
  }
  const s = status.data;
  const Icon = s.state === 'valid' ? ShieldCheck : s.state === 'oss' ? Shield : ShieldAlert;
  const tone = s.state === 'valid' || s.state === 'oss' ? 'success' : 'danger';

  return (
    <div className="px-6 py-6 max-w-3xl">
      <h1 className="font-heading text-2xl font-semibold">License</h1>
      <p className="text-foreground-muted text-[13px] mt-0.5">Active tier and feature gates.</p>

      <div className="mt-5 rounded-lg border border-border bg-canvas p-4">
        <div className="flex items-center gap-3">
          <Icon
            className={`size-7 ${tone === 'success' ? 'text-[var(--success)]' : 'text-[var(--destructive)]'}`}
          />
          <div>
            <div className="font-semibold text-[14px] capitalize">{s.state}</div>
            <div className="text-[12px] text-foreground-muted">
              Tier <span className="font-medium text-foreground">{s.tier}</span>
              {s.licenseHolder && <> · Issued to {s.licenseHolder}</>}
              {s.expiresAt && <> · Expires {formatDateTime(s.expiresAt)}</>}
            </div>
          </div>
        </div>
      </div>

      <h2 className="mt-7 mb-2 text-[11px] uppercase tracking-wider text-foreground-subtle font-medium">
        Features ({features.data?.features.length ?? 0})
      </h2>
      <div className="rounded-lg border border-border bg-canvas overflow-hidden">
        <table className="w-full text-[13px]">
          <thead className="bg-background">
            <tr className="text-foreground-subtle text-[11px] uppercase tracking-wider">
              <th className="text-left font-medium px-3 py-2">Key</th>
              <th className="text-left font-medium px-3 py-2">Description</th>
              <th className="text-left font-medium px-3 py-2">Tiers</th>
              <th className="text-left font-medium px-3 py-2">Status</th>
            </tr>
          </thead>
          <tbody>
            {features.data?.features.map((f) => (
              <tr key={f.key} className="border-t border-divider">
                <td className="px-3 py-2 font-mono text-[12px]">{f.key}</td>
                <td className="px-3 py-2 text-foreground-muted">{f.description}</td>
                <td className="px-3 py-2">
                  <div className="flex flex-wrap gap-1">
                    {f.bundleTiers.map((t) => (
                      <Badge key={t}>{t}</Badge>
                    ))}
                  </div>
                </td>
                <td className="px-3 py-2">
                  <Badge tone={f.enabled ? 'success' : 'neutral'}>{f.enabled ? 'Enabled' : 'Disabled'}</Badge>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
