import { ExternalLink, Inbox } from 'lucide-react';
import { Link } from 'react-router-dom';

import { Avatar } from '@/components/ui/Avatar';
import { Badge } from '@/components/ui/Badge';
import { Spinner } from '@/components/ui/Spinner';
import { useAllTasks } from '@/hooks/useTasks';
import { formatRelativeTime } from '@/lib/formatDate';

export function TasksPage() {
  const { data, isLoading, isError } = useAllTasks();

  return (
    <div className="min-h-full">
      <header className="px-6 pt-5 pb-3 border-b border-border bg-canvas flex items-center justify-between">
        <div>
          <h1 className="font-heading text-[18px] font-semibold flex items-center gap-2">
            <Inbox className="size-4" /> My Tasks
          </h1>
          <p className="text-[12px] text-foreground-muted mt-0.5">
            {isLoading
              ? 'Loading…'
              : `${data?.items.length ?? 0} open ${data?.items.length === 1 ? 'task' : 'tasks'} across all cases`}
          </p>
        </div>
      </header>

      {data?.truncated && (
        <div className="mx-6 my-3 rounded-md border border-[var(--warning)] bg-[var(--warning-soft)] px-3 py-2 text-[12px] text-[var(--warning)]">
          Showing the first 500 tasks. Narrow your filters in a future release.
        </div>
      )}

      <div className="bg-canvas">
        {isLoading ? (
          <div className="grid place-items-center py-20">
            <Spinner className="size-6" />
          </div>
        ) : isError ? (
          <div className="px-6 py-12 text-center text-[var(--destructive)]">Failed to load tasks.</div>
        ) : !data || data.items.length === 0 ? (
          <div className="px-6 py-20 text-center">
            <Inbox className="size-8 mx-auto text-foreground-subtle" />
            <p className="mt-2 text-[14px] text-foreground-muted">You have no open tasks.</p>
          </div>
        ) : (
          <table className="w-full text-[13px] border-separate border-spacing-0">
            <thead>
              <tr className="text-foreground-subtle text-[11px] uppercase tracking-wider">
                <th className="text-left font-medium px-6 py-2 border-b border-border bg-background">Task</th>
                <th className="text-left font-medium px-3 py-2 border-b border-border bg-background">Case</th>
                <th className="text-left font-medium px-3 py-2 border-b border-border bg-background">
                  Assignee
                </th>
                <th className="text-left font-medium px-3 py-2 border-b border-border bg-background">
                  Opened
                </th>
                <th className="w-8 border-b border-border bg-background" />
              </tr>
            </thead>
            <tbody>
              {data.items.map((t) => (
                <tr key={t.id} className="hover:bg-surface-hover">
                  <td className="px-6 py-2 border-b border-divider">
                    <div className="flex items-center gap-2">
                      <span className="font-medium">{t.actionLabel ?? t.name}</span>
                      {t.archetype && <Badge tone="brand">{t.archetype.replace(/_/g, ' ')}</Badge>}
                    </div>
                  </td>
                  <td className="px-3 py-2 border-b border-divider">
                    <Link
                      to={`/cases/${t.caseId}`}
                      className="font-mono text-[12px] text-[var(--primary)] hover:underline"
                    >
                      {t.caseId.slice(0, 8)}
                    </Link>
                  </td>
                  <td className="px-3 py-2 border-b border-divider">
                    {t.assignee ? (
                      <span className="inline-flex items-center gap-1.5">
                        <Avatar name={t.assignee} size="sm" /> {t.assignee}
                      </span>
                    ) : (
                      <span className="text-foreground-subtle">Unassigned</span>
                    )}
                  </td>
                  <td className="px-3 py-2 border-b border-divider text-foreground-muted">
                    {formatRelativeTime(t.createdAt)}
                  </td>
                  <td className="px-3 py-2 border-b border-divider">
                    {t.formId ? (
                      <Link
                        to={`/cases/${t.caseId}/forms/${t.formId}`}
                        className="inline-flex items-center gap-1 text-[12px] text-[var(--primary)] hover:underline"
                      >
                        Open <ExternalLink className="size-3" />
                      </Link>
                    ) : null}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
