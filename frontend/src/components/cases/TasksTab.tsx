import { CheckCircle2, ExternalLink } from 'lucide-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';

import { completeTask } from '@/api/tasks';
import { Avatar } from '@/components/ui/Avatar';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Spinner } from '@/components/ui/Spinner';
import { useCaseTasks } from '@/hooks/useTasks';
import { caseQueryKeys, taskQueryKeys } from '@/lib/queryKeys';
import { formatRelativeTime } from '@/lib/formatDate';

export function TasksTab({ caseId }: { caseId: string }) {
  const { data: tasks, isLoading } = useCaseTasks(caseId);
  const qc = useQueryClient();

  const complete = useMutation({
    mutationFn: (taskId: string) => completeTask(taskId, {}),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: taskQueryKeys.byCase(caseId) });
      qc.invalidateQueries({ queryKey: caseQueryKeys.detail(caseId) });
    },
  });

  if (isLoading) {
    return (
      <div className="grid place-items-center py-16">
        <Spinner />
      </div>
    );
  }
  if (!tasks || tasks.length === 0) {
    return (
      <div className="px-6 py-12 text-center text-foreground-muted text-[13px]">
        No open tasks for this case.
      </div>
    );
  }

  return (
    <ul className="divide-y divide-divider">
      {tasks.map((t) => (
        <li key={t.id} className="px-6 py-3 flex items-start gap-3 hover:bg-surface-hover">
          <div className="mt-0.5">
            <CheckCircle2 className="size-4 text-foreground-subtle" />
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2">
              <div className="font-medium text-[13px]">{t.actionLabel ?? t.name}</div>
              {t.archetype && <Badge tone="brand">{t.archetype.replace(/_/g, ' ')}</Badge>}
            </div>
            <div className="mt-0.5 text-[12px] text-foreground-muted flex items-center gap-2">
              {t.assignee ? (
                <span className="inline-flex items-center gap-1">
                  <Avatar name={t.assignee} size="xs" /> {t.assignee}
                </span>
              ) : (
                <span>Unassigned</span>
              )}
              <span>·</span>
              <span>Opened {formatRelativeTime(t.createdAt)}</span>
              {t.dueAt && (
                <>
                  <span>·</span>
                  <span>Due {formatRelativeTime(t.dueAt)}</span>
                </>
              )}
            </div>
          </div>
          <div className="flex items-center gap-2">
            {t.formId && (
              <Button asChild variant="secondary" size="sm">
                <Link to={`/cases/${caseId}/forms/${t.formId}`}>
                  Open form <ExternalLink className="size-3" />
                </Link>
              </Button>
            )}
            <Button
              variant="primary"
              size="sm"
              disabled={complete.isPending}
              onClick={() => complete.mutate(t.id)}
            >
              {complete.isPending ? 'Completing…' : 'Complete'}
            </Button>
          </div>
        </li>
      ))}
    </ul>
  );
}
