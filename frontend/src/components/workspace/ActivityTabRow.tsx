import { AlertCircle, Ban, CheckCircle2, Pencil } from 'lucide-react';

import { t } from '@/i18n';
import { formatRelativeTime } from '@/lib/formatDate';
import type { AuditEventView } from '@/types/auditEvent';

/**
 * Story 9-2 — render one chronological row in the ActivityTab feed.
 *
 * <p>Source-attribution copy follows §Design Decision 3:
 *   - USER → "You" when the actor matches the current session user (id passed via
 *     {@code currentUserId}); otherwise "User {short-id}".
 *   - BACKEND → "System ({adapterName})".
 *   - AUTO_RULE → "Automation rule {ruleId}".
 *   - EXECUTION_UNMAPPED → "Unmapped backend signal ({originAdapter})".
 *
 * <p>The icon vocabulary uses the existing {@code lucide-react} set (subtle, no colour).
 */
export interface ActivityTabRowProps {
  event: AuditEventView;
  /** Current session user id (for the "You" vs "User <short-id>" branch on USER sources). */
  currentUserId: string | null;
  /** Optional clock injection — tests use this to pin relative timestamps deterministically. */
  now?: Date;
}

function shortId(id: string): string {
  return id.length > 8 ? id.slice(-8) : id;
}

function renderSource(event: AuditEventView, currentUserId: string | null): string {
  switch (event.source.type) {
    case 'USER': {
      const { actorId } = event.source.payload;
      if (currentUserId && actorId === currentUserId) {
        return t('activity.source.you');
      }
      return t('activity.source.user', { idShort: shortId(actorId) });
    }
    case 'BACKEND':
      return t('activity.source.system', { adapter: event.source.payload.adapterName });
    case 'AUTO_RULE':
      return t('activity.source.autoRule', { ruleId: event.source.payload.ruleId });
    case 'EXECUTION_UNMAPPED':
      return t('activity.source.unmapped', { adapter: event.source.payload.originAdapter });
  }
}

function renderResult(event: AuditEventView): string {
  // Edits dominate Sprint 12; the verb phrasing is field-aware for both APPLIED and BLOCKED.
  // Non-edit event types currently fall back to the bare result string; future stories will
  // introduce per-eventType copy bundles.
  const fieldId = event.fieldId ?? '';
  switch (event.result) {
    case 'APPLIED':
      return t('activity.result.applied', { fieldId });
    case 'BLOCKED':
      return t('activity.result.blocked', { fieldId });
    case 'REJECTED':
      return t('activity.result.rejected', { fieldId });
    default:
      return event.result;
  }
}

function resultIcon(result: string) {
  switch (result) {
    case 'APPLIED':
      return CheckCircle2;
    case 'BLOCKED':
      return Ban;
    case 'REJECTED':
      return AlertCircle;
    default:
      return Pencil;
  }
}

export function ActivityTabRow({ event, currentUserId, now }: ActivityTabRowProps) {
  const Icon = resultIcon(event.result);
  const sourceCopy = renderSource(event, currentUserId);
  const resultCopy = renderResult(event);
  const relative = formatRelativeTime(event.occurredAt, now);
  return (
    <li
      data-testid={`activity-row-${event.id}`}
      className="flex items-start gap-3 rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--card)] px-3 py-2"
    >
      <Icon
        aria-hidden
        className="mt-0.5 size-4 flex-shrink-0 text-[var(--muted-foreground)]"
      />
      <div className="min-w-0 flex-1">
        <p className="text-sm">
          <span className="font-medium">{sourceCopy}</span>{' '}
          <span className="text-[var(--muted-foreground)]">{resultCopy}</span>
        </p>
        <p className="mt-0.5 text-xs text-[var(--muted-foreground)]" title={event.occurredAt}>
          {relative}
        </p>
      </div>
    </li>
  );
}
