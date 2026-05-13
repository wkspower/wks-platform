import { Link } from 'react-router-dom';

import { Table, TBody, THead, Th, Td, Tr } from '@/components/ui/Table';
import { useAllTasks } from '@/hooks/useTasks';
import { t } from '@/i18n';
import type { TaskDto } from '@/types/task';

/**
 * Story 13-1 — Tasks Screen: cross-case task list.
 *
 * Renders every pending BPMN user task across every case-type the caller can view (server-side
 * RBAC via `CaseTypePermissionEvaluator`). Sprint 12 scope is read-only:
 * - Row click navigates to `/cases/{caseId}` (existing case route).
 * - Orchestration dialogs (claim/complete from this surface), filters, and deep-link to a
 *   specific task inside the case panel are Sprint 13+ (stories 13-2 / 13-3 / 13-4).
 * - Server caps the response at 500 oldest-first entries; banner copy explains the truncation
 *   confidently rather than blocking interaction.
 */
export function TasksPage() {
  const tasksQuery = useAllTasks();

  return (
    <section>
      <h1 className="font-heading text-2xl font-semibold">{t('page.tasks.title')}</h1>

      {tasksQuery.isLoading ? (
        <TaskListSkeleton />
      ) : tasksQuery.isError ? (
        <p className="mt-[var(--space-4)] text-[var(--muted-foreground)]" role="alert">
          {t('page.tasks.error.label')}
        </p>
      ) : tasksQuery.data && tasksQuery.data.items.length === 0 ? (
        <p className="mt-[var(--space-4)] text-[var(--muted-foreground)]" data-testid="tasks-empty">
          {t('page.tasks.empty.label')}
        </p>
      ) : tasksQuery.data ? (
        <>
          {tasksQuery.data.truncated ? (
            <div
              role="status"
              aria-live="polite"
              data-testid="tasks-truncated-banner"
              className="mt-[var(--space-4)] rounded border border-[var(--border)] bg-[var(--muted)] px-[var(--space-3)] py-[var(--space-2)] text-sm text-[var(--muted-foreground)]"
            >
              {t('page.tasks.truncated.label')}
            </div>
          ) : null}
          <TaskListTable items={tasksQuery.data.items} />
        </>
      ) : null}
    </section>
  );
}

interface TaskListTableProps {
  items: TaskDto[];
}

function TaskListTable({ items }: TaskListTableProps) {
  return (
    <div className="mt-[var(--space-4)]">
      <Table aria-label={t('page.tasks.title')}>
        <THead>
          <Tr>
            <Th>{t('page.tasks.column.case')}</Th>
            <Th>{t('page.tasks.column.task')}</Th>
            <Th>{t('page.tasks.column.assignee')}</Th>
            <Th>{t('page.tasks.column.created')}</Th>
            <Th>{t('page.tasks.column.action')}</Th>
          </Tr>
        </THead>
        <TBody>
          {items.map((task) => (
            <TaskRow key={task.id} task={task} />
          ))}
        </TBody>
      </Table>
    </div>
  );
}

function TaskRow({ task }: { task: TaskDto }) {
  // AC3 — row click navigates to /cases/{caseId}. The case identifier cell carries the actual
  // <Link>, so screen readers reach the navigation control without an extra interactive layer
  // and keyboard users get Enter/Space activation for free via the anchor. Sprint 13+ adds
  // ?taskId= deep-link via Story 13-2 polish.
  const caseHref = `/cases/${encodeURIComponent(task.caseId)}`;
  // Case identifier presentation — short prefix of the UUID is enough for visual scanning at
  // demo scale. Story 13-4 will surface a richer case label once a stable name field exists.
  const caseLabel = task.caseId.length > 8 ? `${task.caseId.slice(0, 8)}…` : task.caseId;
  return (
    <Tr className="hover:bg-[var(--muted)]/40">
      <Td>
        <Link
          to={caseHref}
          className="text-[var(--primary)] underline-offset-2 hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ring)]"
        >
          {caseLabel}
        </Link>
      </Td>
      <Td>{task.name}</Td>
      <Td>{task.assignee ?? t('page.tasks.assignee.unassigned')}</Td>
      <Td>
        <time dateTime={task.createdAt}>{formatRelativeTime(task.createdAt)}</time>
      </Td>
      <Td>{task.actionLabel ?? task.name}</Td>
    </Tr>
  );
}

function TaskListSkeleton() {
  return (
    <div className="mt-[var(--space-4)]" aria-hidden>
      {Array.from({ length: 4 }).map((_, i) => (
        <div
          key={i}
          className="my-[var(--space-2)] h-6 rounded bg-[var(--muted)]/60 animate-pulse"
        />
      ))}
    </div>
  );
}

/**
 * Lightweight relative-time formatter — "5m ago", "3h ago", "2d ago". The Tasks screen is the
 * first surface in the app to need it (case list uses absolute updatedAt); a shared utility
 * arrives with Sprint 13's filter/sort work (Story 13-4) when more surfaces adopt the format.
 */
function formatRelativeTime(iso: string): string {
  const created = Date.parse(iso);
  if (Number.isNaN(created)) return iso;
  const deltaSeconds = Math.max(0, Math.floor((Date.now() - created) / 1000));
  if (deltaSeconds < 60) return `${deltaSeconds}s ago`;
  const minutes = Math.floor(deltaSeconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
}
