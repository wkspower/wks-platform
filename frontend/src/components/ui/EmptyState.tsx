import type { ComponentType, ReactNode, SVGProps } from 'react';

import { cn } from '@/lib/cn';

export interface EmptyStateProps {
  /** Lucide-style icon component. Rendered at size-10 in the muted-fg tone. */
  icon?: ComponentType<SVGProps<SVGSVGElement>>;
  headline: string;
  body?: ReactNode;
  /** Optional primary action — typically a `<Button>` or `<Link>`. */
  action?: ReactNode;
  className?: string;
  /** Stable id for test selectors. */
  'data-testid'?: string;
}

/**
 * Canonical empty-state shape: muted icon → headline → body → optional CTA.
 * Modelled after the DocumentsTab empty block that was the only one of the
 * three pre-existing implementations doing it right. Use for: no rows, no
 * config, no permissions, no results.
 *
 * For an error variant (red tone, retry slot), use `<ErrorState>`.
 */
export function EmptyState({
  icon: Icon,
  headline,
  body,
  action,
  className,
  ...rest
}: EmptyStateProps) {
  return (
    <div
      className={cn('flex flex-col items-center justify-center py-8 text-center', className)}
      {...rest}
    >
      {Icon ? <Icon aria-hidden className="size-10 text-[var(--muted-foreground)]/60" /> : null}
      <h3 className={cn('text-base font-semibold', Icon ? 'mt-3' : undefined)}>{headline}</h3>
      {body ? <p className="mt-1 text-sm text-[var(--muted-foreground)]">{body}</p> : null}
      {action ? <div className="mt-4">{action}</div> : null}
    </div>
  );
}
