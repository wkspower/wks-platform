import { Slot } from '@radix-ui/react-slot';
import { cva, type VariantProps } from 'class-variance-authority';
import { forwardRef, type ButtonHTMLAttributes } from 'react';

import { cn } from '@/lib/cn';

export const buttonVariants = cva(
  // Base: legible disabled state via --disabled-* tokens (replaces opacity-50,
  // which collapses on light backgrounds to ~AAA-violating grey).
  'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-[var(--radius-md)] font-[var(--font-body)] text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:bg-[var(--disabled-bg)] disabled:text-[var(--disabled-fg)] disabled:border-transparent',
  {
    variants: {
      variant: {
        default:
          'bg-[var(--primary)] text-[var(--primary-foreground)] hover:bg-[var(--primary)]/90 focus-visible:ring-[var(--ring)]',
        secondary:
          'bg-[var(--secondary)] text-[var(--secondary-foreground)] hover:bg-[var(--secondary)]/90 focus-visible:ring-[var(--ring)]',
        destructive:
          'bg-[var(--destructive)] text-[var(--destructive-foreground)] hover:bg-[var(--destructive)]/90 focus-visible:ring-[var(--ring-destructive)]',
        outline:
          'border border-[var(--border)] bg-transparent hover:bg-[var(--muted)] focus-visible:ring-[var(--ring)]',
        ghost: 'hover:bg-[var(--muted)] text-[var(--foreground)] focus-visible:ring-[var(--ring)]',
        link: 'text-[var(--primary)] underline-offset-4 hover:underline focus-visible:ring-[var(--ring)]',
      },
      size: {
        sm: 'h-8 px-3 text-xs',
        md: 'h-11 px-4',
        lg: 'h-12 px-6 text-base',
        icon: 'size-11',
      },
    },
    defaultVariants: { variant: 'default', size: 'md' },
  },
);

export interface ButtonProps
  extends ButtonHTMLAttributes<HTMLButtonElement>, VariantProps<typeof buttonVariants> {
  asChild?: boolean;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button(
  { className, variant, size, asChild = false, type, ...props },
  ref,
) {
  const Comp = asChild ? Slot : 'button';
  // Default real <button> elements to type="button" so a Button placed inside
  // a <form> without an explicit type doesn't accidentally submit the form.
  // Slot/asChild callers own their child element's type (or its absence).
  const resolvedType = asChild ? type : (type ?? 'button');
  return (
    <Comp
      ref={ref}
      type={resolvedType}
      className={cn(buttonVariants({ variant, size, className }))}
      {...props}
    />
  );
});
