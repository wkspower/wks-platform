import { forwardRef, type InputHTMLAttributes } from 'react';
import { cn } from '@/lib/cn';

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  ({ className, ...props }, ref) => (
    <input
      ref={ref}
      className={cn(
        'h-8 w-full rounded-md border border-border bg-surface px-2.5 text-[13px] text-foreground placeholder:text-foreground-subtle transition-colors',
        'hover:border-border-strong focus:border-[var(--primary)] focus:outline-none focus:ring-2 focus:ring-[var(--primary-soft)]',
        'disabled:bg-surface-hover disabled:text-foreground-muted disabled:cursor-not-allowed',
        'aria-invalid:border-[var(--destructive)] aria-invalid:focus:ring-[var(--destructive-soft)]',
        className,
      )}
      {...props}
    />
  ),
);
Input.displayName = 'Input';
