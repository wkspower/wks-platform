import { useQuery } from '@tanstack/react-query';
import {
  Activity as ActivityIcon,
  ArrowRightCircle,
  Edit3,
  FileUp,
  PlusCircle,
  Settings2,
} from 'lucide-react';

import { getCaseAuditEvents } from '@/api/auditEvents';
import { Spinner } from '@/components/ui/Spinner';
import { formatDateTime, formatRelativeTime } from '@/lib/formatDate';
import type { AuditEventView } from '@/types/auditEvent';
import type { CaseDto } from '@/types/case';

export function ActivityTab({ dto }: { dto: CaseDto }) {
  const { data, isLoading } = useQuery({
    queryKey: ['audit', dto.id, 50],
    queryFn: () => getCaseAuditEvents(dto.id, 50),
  });

  if (isLoading) {
    return (
      <div className="grid place-items-center py-16">
        <Spinner />
      </div>
    );
  }
  if (!data || data.items.length === 0) {
    return (
      <div className="px-6 py-12 text-center text-foreground-muted text-[13px]">
        No activity yet.
      </div>
    );
  }

  return (
    <div className="px-6 py-5">
      {data.truncated && (
        <p className="mb-3 text-[12px] text-foreground-muted">
          Showing the 50 most recent events.
        </p>
      )}
      <ol className="relative pl-6 border-l border-border">
        {data.items.map((e) => (
          <ActivityRow key={e.id} event={e} dto={dto} />
        ))}
      </ol>
    </div>
  );
}

function ActivityRow({ event, dto }: { event: AuditEventView; dto: CaseDto }) {
  const Icon = iconFor(event.eventType);
  const actor = describeActor(event);
  const summary = describeEvent(event, dto);
  return (
    <li className="relative pb-5">
      <span className="absolute -left-[31px] grid size-6 place-items-center rounded-full bg-surface border border-border text-foreground-muted">
        <Icon className="size-3" />
      </span>
      <div className="text-[13px]">{summary}</div>
      <div className="text-[11px] text-foreground-subtle mt-0.5" title={formatDateTime(event.occurredAt)}>
        {actor} · {formatRelativeTime(event.occurredAt)}
      </div>
    </li>
  );
}

function iconFor(eventType: string) {
  if (eventType === 'case.created') return PlusCircle;
  if (eventType === 'case.status.changed') return ArrowRightCircle;
  if (eventType === 'case.data.edit') return Edit3;
  if (eventType === 'case.document.uploaded') return FileUp;
  if (eventType.startsWith('case.')) return Settings2;
  return ActivityIcon;
}

function describeActor(event: AuditEventView): string {
  switch (event.source.type) {
    case 'USER':
      return `User ${event.source.payload.actorId.slice(0, 8)}`;
    case 'AUTO_RULE':
      return `Auto-rule ${event.source.payload.ruleId}`;
    case 'BACKEND':
      return event.source.payload.adapterName;
    case 'EXECUTION_UNMAPPED':
      return `Unmapped (${event.source.payload.originAdapter})`;
  }
}

function describeEvent(event: AuditEventView, dto: CaseDto): string {
  if (event.eventType === 'case.created') return 'Case created';
  if (event.eventType === 'case.status.changed') {
    const newS = dto.caseType.statuses.find((s) => s.id === event.result)?.displayName ?? event.result;
    const prev = event.previousResult
      ? dto.caseType.statuses.find((s) => s.id === event.previousResult)?.displayName ?? event.previousResult
      : null;
    return prev ? `Status changed from ${prev} to ${newS}` : `Status set to ${newS}`;
  }
  if (event.eventType === 'case.data.edit') {
    const fname = dto.caseType.fields.find((f) => f.id === event.fieldId)?.displayName ?? event.fieldId;
    return `Field “${fname}” ${event.result.toLowerCase()}`;
  }
  if (event.eventType === 'case.document.uploaded') {
    return `Uploaded document “${event.result}”`;
  }
  return event.eventType;
}
