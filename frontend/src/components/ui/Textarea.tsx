import { forwardRef, type TextareaHTMLAttributes } from 'react';
import { cn } from '@/lib/cn';

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaHTMLAttributes<HTMLTextAreaElement>>(
  ({ className, ...props }, ref) => (
    <textarea
      ref={ref}
      className={cn(
        'w-full min-h-[72px] rounded-md border border-border bg-surface px-2.5 py-2 text-[13px] text-foreground placeholder:text-foreground-subtle',
        'hover:border-border-strong focus:border-[var(--primary)] focus:outline-none focus:ring-2 focus:ring-[var(--primary-soft)]',
        'aria-invalid:border-[var(--destructive)]',
        className,
      )}
      {...props}
    />
  ),
);
Textarea.displayName = 'Textarea';
