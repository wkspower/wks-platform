import { forwardRef, type ButtonHTMLAttributes } from 'react';
import { cn } from '@/lib/cn';

export const IconButton = forwardRef<HTMLButtonElement, ButtonHTMLAttributes<HTMLButtonElement>>(
  ({ className, ...rest }, ref) => (
    <button
      ref={ref}
      type="button"
      className={cn(
        'inline-flex size-7 items-center justify-center rounded-md text-foreground-muted hover:bg-surface-hover hover:text-foreground transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ring)] focus-visible:ring-offset-1 focus-visible:ring-offset-[var(--ring-offset)] disabled:opacity-50 disabled:cursor-not-allowed',
        className,
      )}
      {...rest}
    />
  ),
);
IconButton.displayName = 'IconButton';
