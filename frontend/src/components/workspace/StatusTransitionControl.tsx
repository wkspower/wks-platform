import { Button } from '@/components/ui/Button';
import { useTransitionCase } from '@/hooks/useCases';
import { t } from '@/i18n';
import type { CaseDto } from '@/types/case';

export interface StatusTransitionControlProps {
  caseDto: CaseDto;
}

/**
 * Renders inline status-transition affordances next to the StatusBadge. Reads the case-type's
 * declared statuses, hides itself when the current status is terminal or no transition targets
 * exist, renders a single button for one target and a list of buttons for multiple targets.
 *
 * On zero-process case types (no BPMN, no stages) `POST /api/cases/{id}/transition` bypasses the
 * engine entirely — see CaseService.transition zero-process path. The hook owns cache invalidation
 * so the badge re-renders with the new status on success.
 */
export function StatusTransitionControl({ caseDto }: StatusTransitionControlProps) {
  const statuses = caseDto.caseType.statuses ?? [];
  const current = statuses.find((s) => s.id === caseDto.status);
  const mutation = useTransitionCase(caseDto.id);

  if (current?.terminal) return null;

  const targets = statuses.filter((s) => s.id !== caseDto.status);
  if (targets.length === 0) return null;

  return (
    <div className="flex items-center gap-2" data-testid="status-transition-control">
      {targets.map((target) => (
        <Button
          key={target.id}
          type="button"
          size="sm"
          variant="outline"
          disabled={mutation.isPending}
          onClick={() => mutation.mutate({ action: target.id })}
        >
          {mutation.isPending
            ? t('case.status.transitionPending')
            : t('case.status.transitionTo', { label: target.displayName })}
        </Button>
      ))}
      {mutation.isError ? (
        <span className="text-xs text-[var(--destructive)]" role="alert">
          {t('case.status.transitionError')}
        </span>
      ) : null}
    </div>
  );
}
