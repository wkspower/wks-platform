import { cva, type VariantProps } from 'class-variance-authority';
import type { HTMLAttributes } from 'react';
import { cn } from '@/lib/cn';

const badge = cva(
  'inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[11px] font-medium border',
  {
    variants: {
      tone: {
        neutral: 'bg-surface-hover text-foreground-muted border-border',
        info: 'bg-[var(--info-soft)] text-[var(--info)] border-transparent',
        success: 'bg-[var(--success-soft)] text-[var(--success)] border-transparent',
        warning: 'bg-[var(--warning-soft)] text-[var(--warning)] border-transparent',
        danger: 'bg-[var(--destructive-soft)] text-[var(--destructive)] border-transparent',
        brand: 'bg-[var(--primary-soft)] text-[var(--primary-soft-on)] border-transparent',
      },
    },
    defaultVariants: { tone: 'neutral' },
  },
);

export interface BadgeProps
  extends HTMLAttributes<HTMLSpanElement>,
    VariantProps<typeof badge> {}

export function Badge({ className, tone, ...rest }: BadgeProps) {
  return <span className={cn(badge({ tone }), className)} {...rest} />;
}
