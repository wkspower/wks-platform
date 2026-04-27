import * as CheckboxPrimitive from '@radix-ui/react-checkbox';
import { Check } from 'lucide-react';
import { forwardRef, type ComponentPropsWithoutRef, type ElementRef } from 'react';

import { cn } from '@/lib/cn';

export const Checkbox = forwardRef<
  ElementRef<typeof CheckboxPrimitive.Root>,
  ComponentPropsWithoutRef<typeof CheckboxPrimitive.Root> & { hasError?: boolean }
>(function Checkbox({ className, hasError, ...props }, ref) {
  return (
    <CheckboxPrimitive.Root
      ref={ref}
      aria-invalid={hasError ? true : undefined}
      className={cn(
        'peer size-4 shrink-0 rounded-[var(--radius-sm)] border border-[var(--border)] bg-[var(--card)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ring)] focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 data-[state=checked]:bg-[var(--primary)] data-[state=checked]:text-[var(--primary-foreground)]',
        hasError && 'border-[var(--destructive)] focus-visible:ring-[var(--destructive)]',
        className,
      )}
      {...props}
    >
      <CheckboxPrimitive.Indicator className="flex items-center justify-center text-current">
        <Check className="size-3.5" aria-hidden />
      </CheckboxPrimitive.Indicator>
    </CheckboxPrimitive.Root>
  );
});
