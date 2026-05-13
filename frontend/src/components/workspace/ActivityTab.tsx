import { useEffect, useState } from 'react';

import { getCaseAuditEvents } from '@/api/auditEvents';
import { t } from '@/i18n';
import { useAuthStore } from '@/stores/authStore';
import type { AuditEventList } from '@/types/auditEvent';

import { ActivityTabRow } from './ActivityTabRow';

/**
 * Story 9-2 — chronological audit feed for the open case. Replaces the placeholder at {@code
 * CaseDetailPanel.tsx:240}.
 *
 * <p>Sprint 12 ships limit-only (default 50); "Show more" / infinite scroll is Sprint 13+ polish.
 * The feed re-fetches on caseId change; cross-tab live updates are out of scope (real-time
 * push is Epic 9's later sprint, not 9-2).
 */
export interface ActivityTabProps {
  caseId: string;
}

export function ActivityTab({ caseId }: ActivityTabProps) {
  const [data, setData] = useState<AuditEventList | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const currentUserId = useAuthStore((state) => state.user?.id ?? null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    getCaseAuditEvents(caseId)
      .then((result) => {
        if (cancelled) return;
        setData(result);
        setLoading(false);
      })
      .catch((err) => {
        if (cancelled) return;
        // Confidence-frame copy per project memory: never "failed/broken". The empty fallback
        // here is the same "No activity yet on this case" line the empty state shows, so a
        // transient network blip degrades to a calm empty rather than a red error banner.
        setError(err instanceof Error ? err.message : String(err));
        setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [caseId]);

  if (loading) {
    return (
      <div
        data-testid="activity-loading"
        className="flex items-center justify-center py-6"
        role="status"
        aria-live="polite"
      >
        <div className="size-5 animate-spin rounded-full border-2 border-[var(--primary)] border-t-transparent" />
        <span className="sr-only">{t('activity.loading.label')}</span>
      </div>
    );
  }

  if (error || !data || data.items.length === 0) {
    return (
      <div
        data-testid="activity-empty"
        className="flex flex-col items-center justify-center py-12 text-center"
      >
        <p className="text-sm text-[var(--muted-foreground)]">{t('activity.feed.empty.label')}</p>
      </div>
    );
  }

  return (
    <div data-testid="activity-tab" className="flex flex-col gap-3 py-3">
      <ul
        className="flex flex-col gap-2"
        role="list"
        aria-label={t('activity.feed.listLabel')}
      >
        {data.items.map((event) => (
          <ActivityTabRow key={event.id} event={event} currentUserId={currentUserId} />
        ))}
      </ul>
      {data.truncated && (
        <p
          data-testid="activity-truncated"
          className="text-center text-xs text-[var(--muted-foreground)]"
        >
          {t('activity.feed.truncated.label', { limit: String(data.items.length) })}
        </p>
      )}
    </div>
  );
}
