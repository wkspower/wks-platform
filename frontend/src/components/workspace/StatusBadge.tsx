import { Badge } from '@/components/ui/Badge';
import { t } from '@/i18n';
import { statusOnFgVar, statusSoftBgVar } from '@/lib/statusColor';
import type { CaseTypeView } from '@/types/caseType';

export interface StatusBadgeProps {
  status: string;
  caseType: Pick<CaseTypeView, 'statuses'>;
}

/**
 * Soft-bg + on-fg badge. The previous solid-fill version slammed white text on
 * mid-saturation amber, sitting at borderline AA — see contrast.test.ts pairs.
 * Badges still show colour + text (a11y: never colour alone). Unknown status →
 * neutral closed-soft pair + raw id in title, with a `console.warn`.
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
          backgroundColor: 'var(--status-closed-soft)',
          color: 'var(--status-closed-on)',
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
        backgroundColor: statusSoftBgVar(definition.color),
        color: statusOnFgVar(definition.color),
      }}
    >
      {definition.displayName}
    </Badge>
  );
}
