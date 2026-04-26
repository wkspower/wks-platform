import { Badge } from '@/components/ui/Badge';
import { t } from '@/i18n';
import { statusColorVar } from '@/lib/statusColor';
import type { CaseTypeView } from '@/types/caseType';

export interface StatusBadgeProps {
  status: string;
  caseType: Pick<CaseTypeView, 'statuses'>;
}

/**
 * AC6 — solid-fill badge whose background reads from the configurable status palette tokens.
 * The badge always shows colour + text label (a11y: never colour alone). When the supplied
 * `status` does not resolve in `caseType.statuses[]` the badge falls back to the neutral
 * `--status-closed` token + the raw status id, with a `console.warn` so test runs surface
 * the misconfiguration.
 *
 * Foreground is hardcoded to `--primary-foreground` (white) for every palette colour — the
 * contrast ratios are verified by `styles/contrast.test.ts` (1.3 guardrail).
 */
export function StatusBadge({ status, caseType }: StatusBadgeProps) {
  const definition = caseType.statuses.find((s) => s.id === status);

  if (!definition) {
    // eslint-disable-next-line no-console
    console.warn(`StatusBadge: status '${status}' not found in caseType.statuses`);
    return (
      <Badge
        variant="solid"
        title={status}
        style={{
          backgroundColor: 'var(--status-closed)',
          color: 'var(--primary-foreground)',
        }}
      >
        {t('cases.status.unknown')}
      </Badge>
    );
  }

  return (
    <Badge
      variant="solid"
      style={{
        backgroundColor: statusColorVar(definition.color),
        color: 'var(--primary-foreground)',
      }}
    >
      {definition.displayName}
    </Badge>
  );
}
