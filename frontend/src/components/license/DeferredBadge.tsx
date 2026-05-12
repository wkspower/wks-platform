import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/ui/Tooltip';
import { t } from '@/i18n';

interface DeferredBadgeProps {
  /** The tracking story key (e.g. "15-7-audit-export-polish"). Shown in tooltip. */
  trackingStory: string;
}

/**
 * Small inline badge indicating that a feature is gated but its implementation is deferred
 * to a future story. Sets honest expectations for operators and EE customers.
 *
 * Story 7-6 AC-5: used in the LicenseStatusPage feature table for `audit.export` and
 * `audit.checksums` rows (whose underlying implementations are tracked in Epic 15).
 *
 * The badge is intentionally neutral — it signals "impl-deferred", not "gating-deferred".
 * An EE customer with the feature enabled will still see the Deferred badge because the
 * capability doesn't exist yet, not because the license is wrong.
 */
export function DeferredBadge({ trackingStory }: DeferredBadgeProps) {
  const tooltipText = t('license.features.deferred.tooltip', { story: trackingStory });
  const badgeLabel = t('license.features.deferred.badge');

  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <span
            aria-label={tooltipText}
            className="ml-1 inline-flex cursor-default items-center rounded-[var(--radius-sm)] bg-[var(--muted)] px-1.5 py-0.5 text-[10px] font-medium text-[var(--muted-foreground)]"
          >
            {badgeLabel}
          </span>
        </TooltipTrigger>
        <TooltipContent>{tooltipText}</TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}
