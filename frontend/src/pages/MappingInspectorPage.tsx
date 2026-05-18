import { useQuery } from '@tanstack/react-query';
import { ArrowLeft } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';

import { fetchMappingInspector, fetchRecentSignals } from '@/api/mappingInspector';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Spinner } from '@/components/ui/Spinner';
import { formatDateTime } from '@/lib/formatDate';

export function MappingInspectorPage() {
  const { caseTypeId } = useParams<{ caseTypeId: string }>();
  const mapping = useQuery({
    queryKey: ['mapping', caseTypeId],
    queryFn: () => fetchMappingInspector(caseTypeId!),
    enabled: !!caseTypeId,
  });
  const signals = useQuery({
    queryKey: ['signals', caseTypeId],
    queryFn: () => fetchRecentSignals(caseTypeId!),
    enabled: !!caseTypeId,
  });

  if (mapping.isLoading) return <div className="grid place-items-center py-20"><Spinner /></div>;
  if (mapping.isError || !mapping.data) {
    return <div className="px-6 py-12 text-center text-[var(--destructive)]">Failed to load mapping.</div>;
  }
  const m = mapping.data;

  return (
    <div className="px-6 py-6 max-w-5xl">
      <Button asChild variant="ghost" size="xs">
        <Link to="/admin"><ArrowLeft className="size-3.5" /> Admin</Link>
      </Button>
      <h1 className="mt-2 font-heading text-2xl font-semibold">
        Mapping inspector
      </h1>
      <p className="text-foreground-muted text-[13px] mt-0.5">
        <span className="font-mono">{m.caseTypeId}</span> · version {m.version}
        {m.emptyMapping && <span className="ml-2 text-[var(--warning)]">(no mappings)</span>}
      </p>

      {m.attachments.map((a) => (
        <section key={a.name} className="mt-6">
          <h2 className="text-[13px] font-semibold mb-2">
            {a.name} <span className="text-foreground-muted font-normal">· {a.bpmnSource}</span>
          </h2>
          <div className="rounded-lg border border-border bg-canvas overflow-hidden">
            <table className="w-full text-[13px]">
              <thead className="bg-background text-foreground-subtle text-[11px] uppercase tracking-wider">
                <tr>
                  <th className="text-left font-medium px-3 py-2">BPMN element</th>
                  <th className="text-left font-medium px-3 py-2">Effect</th>
                  <th className="text-left font-medium px-3 py-2">Target</th>
                  <th className="text-left font-medium px-3 py-2">Rule</th>
                </tr>
              </thead>
              <tbody>
                {a.elements.map((e, i) => (
                  <tr key={i} className="border-t border-divider">
                    <td className="px-3 py-2 font-mono text-[12px]">{e.bpmnElement}</td>
                    <td className="px-3 py-2"><Badge tone="brand">{e.wksEffect}</Badge></td>
                    <td className="px-3 py-2 text-foreground-muted">{e.target ?? '—'}</td>
                    <td className="px-3 py-2 text-foreground-muted">{e.rule ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      ))}

      <section className="mt-7">
        <h2 className="text-[13px] font-semibold mb-2">Recent signals</h2>
        <div className="rounded-lg border border-border bg-canvas overflow-hidden">
          {signals.isLoading ? (
            <div className="grid place-items-center py-8"><Spinner /></div>
          ) : !signals.data || signals.data.signals.length === 0 ? (
            <p className="px-4 py-6 text-center text-[13px] text-foreground-muted">No recent signals.</p>
          ) : (
            <table className="w-full text-[13px]">
              <thead className="bg-background text-foreground-subtle text-[11px] uppercase tracking-wider">
                <tr>
                  <th className="text-left font-medium px-3 py-2">Time</th>
                  <th className="text-left font-medium px-3 py-2">Kind</th>
                  <th className="text-left font-medium px-3 py-2">Source</th>
                  <th className="text-left font-medium px-3 py-2">Decision</th>
                  <th className="text-left font-medium px-3 py-2">Rule</th>
                  <th className="text-left font-medium px-3 py-2">Effect</th>
                </tr>
              </thead>
              <tbody>
                {signals.data.signals.map((s, i) => (
                  <tr key={i} className="border-t border-divider">
                    <td className="px-3 py-2 text-foreground-muted">{formatDateTime(s.timestamp)}</td>
                    <td className="px-3 py-2 font-mono text-[12px]">{s.kind}</td>
                    <td className="px-3 py-2 text-foreground-muted">{s.source}</td>
                    <td className="px-3 py-2">
                      <Badge tone={s.decision === 'matched-rule' ? 'success' : 'warning'}>
                        {s.decision}
                      </Badge>
                    </td>
                    <td className="px-3 py-2 font-mono text-[12px]">{s.matchedRule ?? '—'}</td>
                    <td className="px-3 py-2">{s.effect ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </section>
    </div>
  );
}
