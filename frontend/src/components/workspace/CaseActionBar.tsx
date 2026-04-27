import { useEffect, useState } from 'react';

import { useCaseTasks } from '@/hooks/useTasks';
import { t } from '@/i18n';

import { TaskLifecycleButton } from './TaskLifecycleButton';

const NEXT_CASE_HINT_DELAY_MS = 4_000;

/**
 * Story 2.8 AC8 / AC10 — primary CTA placement. Lives between the case heading row and the tabs.
 * Renders a {@link TaskLifecycleButton} per pending task (topmost-by-createdAt is the primary CTA;
 * subsequent tasks render in a vertical stack below it). When the case has no pending tasks, a
 * subtle "Next case ({@code J})" hint appears after a 4s delay (the J/K shortcut already lives in
 * {@code CaseWorkspace}; 2.8 only ships the visible affordance).
 */
export interface CaseActionBarProps {
  caseId: string;
}

export function CaseActionBar({ caseId }: CaseActionBarProps) {
  const tasksQuery = useCaseTasks(caseId);
  const [showHint, setShowHint] = useState(false);

  const hasTasks = (tasksQuery.data?.length ?? 0) > 0;

  // Empty-state hint: show after 4s, hide the moment a task arrives. Reset the timer when caseId
  // changes (J/K stepping to a new case).
  useEffect(() => {
    setShowHint(false);
    if (tasksQuery.isLoading) return undefined;
    if (hasTasks) return undefined;
    const handle = window.setTimeout(() => setShowHint(true), NEXT_CASE_HINT_DELAY_MS);
    return () => clearTimeout(handle);
  }, [caseId, tasksQuery.isLoading, hasTasks]);

  if (tasksQuery.isError) {
    // Phase 0 — fail quietly. The case panel owns the case-level error UI; surfacing a second
    // error band here would double-announce. Re-fetch happens on focus (TanStack Query default).
    return null;
  }

  if (!tasksQuery.data || tasksQuery.data.length === 0) {
    if (!showHint) return null;
    return (
      <div
        data-testid="case-action-bar-empty-hint"
        className="px-4 py-2 text-xs text-[var(--muted-foreground)]"
      >
        {t('case.nextCaseHint', { shortcut: 'J' })}
      </div>
    );
  }

  const [primary, ...secondary] = tasksQuery.data;
  if (!primary) return null;

  return (
    <div className="flex flex-col gap-2 px-4 py-3" data-testid="case-action-bar">
      <TaskLifecycleButton task={primary} />
      {secondary.length > 0 ? (
        <div className="flex flex-col gap-2">
          {secondary.map((task) => (
            <TaskLifecycleButton key={task.id} task={task} />
          ))}
        </div>
      ) : null}
    </div>
  );
}
