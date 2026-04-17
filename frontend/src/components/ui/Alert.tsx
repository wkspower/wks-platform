import { cva, type VariantProps } from 'class-variance-authority';
import { forwardRef, type HTMLAttributes } from 'react';

import { cn } from '@/lib/cn';

export const alertVariants = cva(
  'relative w-full rounded-[var(--radius-md)] border p-[var(--space-4)] text-sm',
  {
    variants: {
      variant: {
        info: 'border-[var(--info)]/30 bg-[var(--info)]/10 text-[var(--info)]',
        success: 'border-[var(--success)]/30 bg-[var(--success)]/10 text-[var(--success)]',
        warning: 'border-[var(--warning)]/30 bg-[var(--warning)]/10 text-[var(--warning)]',
        destructive:
          'border-[var(--destructive)]/30 bg-[var(--destructive)]/10 text-[var(--destructive)]',
      },
    },
    defaultVariants: { variant: 'info' },
  },
);

export interface AlertProps
  extends HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof alertVariants> {}

export const Alert = forwardRef<HTMLDivElement, AlertProps>(function Alert(
  { className, variant = 'info', role, ...props },
  ref,
) {
  const inferredRole = role ?? (variant === 'destructive' || variant === 'warning' ? 'alert' : 'status');
  return (
    <div
      ref={ref}
      role={inferredRole}
      aria-live={inferredRole === 'alert' ? 'assertive' : 'polite'}
      className={cn(alertVariants({ variant, className }))}
      {...props}
    />
  );
});
