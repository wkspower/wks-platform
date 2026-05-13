import type { HTMLAttributes } from 'react';

import { cn } from '@/lib/cn';

/**
 * Loading placeholder — replaces the three local pulse-block implementations
 * that drifted apart (rounded radius, opacity, height conventions). Use one
 * `<Skeleton>` per logical row/field; group with a `flex flex-col gap-*`.
 *
 * Respects `prefers-reduced-motion` via the underlying `animate-pulse`
 * utility (Tailwind animation tokens already gated by tokens.css).
 */
export function Skeleton({ className, ...rest }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      aria-hidden
      className={cn('animate-pulse rounded-[var(--radius-md)] bg-[var(--muted)]/60', className)}
      {...rest}
    />
  );
}
