import { forwardRef, type InputHTMLAttributes } from 'react';

import { cn } from '@/lib/cn';

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  hasError?: boolean;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(function Input(
  { className, hasError, ...props },
  ref,
) {
  return (
    <input
      ref={ref}
      aria-invalid={hasError ? true : undefined}
      className={cn(
        'flex h-11 w-full rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--card)] px-3 py-2 text-sm text-[var(--foreground)] placeholder:text-[var(--muted-foreground)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ring)] focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:bg-[var(--disabled-bg)] disabled:text-[var(--disabled-fg)]',
        hasError && 'border-[var(--destructive)] focus-visible:ring-[var(--destructive)]',
        className,
      )}
      {...props}
    />
  );
});
