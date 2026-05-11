import { useQuery } from '@tanstack/react-query';
import { useParams } from 'react-router-dom';

import {
  fetchMappingInspector,
  fetchRecentSignals,
  type ElementMappingRow,
  type RecentSignalView,
} from '@/api/mappingInspector';
import { Table, TBody, Td, Th, THead, Tr } from '@/components/ui/Table';
import { t } from '@/i18n';

const MAPPING_ANCHOR_ID = 'routing-block';
const POLL_INTERVAL_MS = 5_000;

/**
 * Story 4.6 AC4 — read-only admin Mapping Inspector page. Two panels:
 *   1. Mapping table — projected from /mapping-inspector (one row per declared element).
 *   2. Recent signals — projected from /recent-signals, polled every 5 s.
 * Rows with errorCode === 'WKS-MAP-404' get a warning treatment + a "view mapping" deep
 * link that smooth-scrolls to the mapping table.
 *
 * No edit affordance — per epics §4.6 the inspector is read-only; mapping edits live in
 * the Dev Console (Epic 11).
 */
export function MappingInspectorPage() {
  const { caseTypeId } = useParams<{ caseTypeId: string }>();
  const id = caseTypeId ?? '';

  const mappingQuery = useQuery({
    queryKey: ['mappingInspector', 'mapping', id],
    queryFn: ({ signal }) => fetchMappingInspector(id, signal),
    enabled: id !== '',
  });

  const signalsQuery = useQuery({
    queryKey: ['mappingInspector', 'signals', id],
    queryFn: ({ signal }) => fetchRecentSignals(id, signal),
    enabled: id !== '',
    refetchInterval: POLL_INTERVAL_MS,
    refetchIntervalInBackground: false,
  });

  const handleDeepLink = (e: React.MouseEvent) => {
    e.preventDefault();
    const target = document.getElementById(MAPPING_ANCHOR_ID);
    if (target) {
      target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  if (mappingQuery.isLoading) {
    return (
      <section aria-label={t('admin.mappingInspector.title')}>
        <h1 className="font-heading text-2xl font-semibold">{t('admin.mappingInspector.title')}</h1>
        <p className="mt-[var(--space-4)] text-sm text-[var(--muted-foreground)]">
          {t('admin.mappingInspector.loading')}
        </p>
      </section>
    );
  }

  if (mappingQuery.isError) {
    return (
      <section aria-label={t('admin.mappingInspector.title')}>
        <h1 className="font-heading text-2xl font-semibold">{t('admin.mappingInspector.title')}</h1>
        <p className="mt-[var(--space-4)] text-sm text-[var(--warning)]">
          {t('admin.mappingInspector.error')}
        </p>
      </section>
    );
  }

  const mapping = mappingQuery.data;
  const signals = signalsQuery.data?.signals ?? [];

  return (
    <section aria-label={t('admin.mappingInspector.title')}>
      <header className="mb-[var(--space-6)]">
        <h1 className="font-heading text-2xl font-semibold">{t('admin.mappingInspector.title')}</h1>
        <p className="mt-[var(--space-1)] text-sm text-[var(--muted-foreground)]">
          {t('admin.mappingInspector.subtitle')}
        </p>
        {mapping && (
          <div className="mt-[var(--space-3)] flex flex-wrap items-center gap-[var(--space-3)] text-sm">
            <code className="rounded bg-[var(--muted)] px-2 py-0.5 text-xs">
              {mapping.caseTypeId}
            </code>
            <span className="text-[var(--muted-foreground)]">
              {t('admin.mappingInspector.activeVersion')}{' '}
              <span className="font-medium text-[var(--foreground)]">{mapping.version}</span>
            </span>
          </div>
        )}
        <div
          role="note"
          className="mt-[var(--space-4)] rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--muted)] px-[var(--space-3)] py-[var(--space-2)] text-xs text-[var(--muted-foreground)]"
        >
          {t('admin.mappingInspector.noEdit')}
        </div>
      </header>

      {/* Panel 1: mapping table */}
      <div id={MAPPING_ANCHOR_ID} className="mb-[var(--space-8)] scroll-mt-[var(--space-6)]">
        <h2 className="font-heading text-lg font-semibold">
          {t('admin.mappingInspector.mapping.title')}
        </h2>
        {!mapping || mapping.emptyMapping ? (
          <p className="mt-[var(--space-3)] rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--card)] px-[var(--space-4)] py-[var(--space-3)] text-sm text-[var(--muted-foreground)]">
            {t('admin.mappingInspector.empty')}
          </p>
        ) : (
          mapping.attachments.map((attachment) => (
            <div key={attachment.name} className="mt-[var(--space-4)]">
              <h3 className="text-sm font-medium text-[var(--foreground)]">
                {t('admin.mappingInspector.attachmentLabel')}{' '}
                <code className="rounded bg-[var(--muted)] px-1 py-0.5 text-xs">
                  {attachment.bpmnSource}
                </code>
              </h3>
              <div className="mt-[var(--space-2)] rounded-[var(--radius-md)] border border-[var(--border)]">
                <Table>
                  <THead>
                    <Tr>
                      <Th>{t('admin.mappingInspector.mapping.col.element')}</Th>
                      <Th>{t('admin.mappingInspector.mapping.col.effect')}</Th>
                      <Th>{t('admin.mappingInspector.mapping.col.target')}</Th>
                      <Th>{t('admin.mappingInspector.mapping.col.rule')}</Th>
                    </Tr>
                  </THead>
                  <TBody>
                    {attachment.elements.map((row: ElementMappingRow, idx: number) => (
                      <Tr key={`${attachment.name}-${row.bpmnElement}-${idx}`}>
                        <Td>
                          <code className="text-xs">{row.bpmnElement}</code>
                        </Td>
                        <Td>{row.wksEffect}</Td>
                        <Td>{row.target ?? '—'}</Td>
                        <Td>
                          <code className="text-xs text-[var(--muted-foreground)]">
                            {row.rule ?? '—'}
                          </code>
                        </Td>
                      </Tr>
                    ))}
                  </TBody>
                </Table>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Panel 2: recent signals */}
      <div>
        <h2 className="font-heading text-lg font-semibold">
          {t('admin.mappingInspector.signals.title')}
        </h2>
        {signals.length === 0 ? (
          <p className="mt-[var(--space-3)] rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--card)] px-[var(--space-4)] py-[var(--space-3)] text-sm text-[var(--muted-foreground)]">
            {t('admin.mappingInspector.signals.empty')}
          </p>
        ) : (
          <div className="mt-[var(--space-3)] rounded-[var(--radius-md)] border border-[var(--border)]">
            <Table>
              <THead>
                <Tr>
                  <Th>{t('admin.mappingInspector.signals.col.timestamp')}</Th>
                  <Th>{t('admin.mappingInspector.signals.col.kind')}</Th>
                  <Th>{t('admin.mappingInspector.signals.col.source')}</Th>
                  <Th>{t('admin.mappingInspector.signals.col.decision')}</Th>
                  <Th>{t('admin.mappingInspector.signals.col.effect')}</Th>
                </Tr>
              </THead>
              <TBody>
                {signals.map((s: RecentSignalView, idx: number) => {
                  const isMiss = s.errorCode != null;
                  return (
                    <Tr
                      key={`${s.timestamp}-${idx}`}
                      data-state={isMiss ? 'warning' : undefined}
                      className={isMiss ? 'bg-[var(--warning)]/10' : undefined}
                      data-testid={isMiss ? 'recent-signal-row-miss' : 'recent-signal-row'}
                    >
                      <Td>
                        <time dateTime={s.timestamp} className="text-xs">
                          {s.timestamp}
                        </time>
                      </Td>
                      <Td>
                        <code className="text-xs">{s.kind}</code>
                      </Td>
                      <Td>
                        <code className="text-xs">{s.source}</code>
                      </Td>
                      <Td>
                        {s.errorCode === 'WKS-MAP-404' ? (
                          <a
                            href={`#${MAPPING_ANCHOR_ID}`}
                            onClick={handleDeepLink}
                            className="text-[var(--secondary)] underline hover:text-[var(--secondary)]/80"
                            data-testid="map-404-deep-link"
                          >
                            {s.decision}{' '}
                            <span className="text-xs">
                              {t('admin.mappingInspector.signals.viewMapping')}
                            </span>
                          </a>
                        ) : (
                          s.decision
                        )}
                      </Td>
                      <Td>{s.effect ?? '—'}</Td>
                    </Tr>
                  );
                })}
              </TBody>
            </Table>
          </div>
        )}
      </div>
    </section>
  );
}
