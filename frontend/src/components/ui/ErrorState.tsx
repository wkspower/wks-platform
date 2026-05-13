import { AlertTriangle } from 'lucide-react';
import type { ComponentType, ReactNode, SVGProps } from 'react';

import { cn } from '@/lib/cn';

export interface ErrorStateProps {
  /** Defaults to AlertTriangle. */
  icon?: ComponentType<SVGProps<SVGSVGElement>>;
  headline: string;
  body?: ReactNode;
  /** Retry / recover slot — typically a `<Button>`. */
  action?: ReactNode;
  className?: string;
  'data-testid'?: string;
}

/**
 * Error counterpart to `<EmptyState>`. Same shape, destructive tone, retry
 * slot. Use whenever a load fails recoverably — never render `String(err)`
 * to users.
 */
export function ErrorState({
  icon: Icon = AlertTriangle,
  headline,
  body,
  action,
  className,
  ...rest
}: ErrorStateProps) {
  return (
    <div
      role="alert"
      className={cn('flex flex-col items-center justify-center py-8 text-center', className)}
      {...rest}
    >
      <Icon aria-hidden className="size-10 text-[var(--destructive)]" />
      <h3 className="mt-3 text-base font-semibold">{headline}</h3>
      {body ? <p className="mt-1 text-sm text-[var(--muted-foreground)]">{body}</p> : null}
      {action ? <div className="mt-4">{action}</div> : null}
    </div>
  );
}
