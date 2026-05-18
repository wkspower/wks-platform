import { Slot } from '@radix-ui/react-slot';
import { cva, type VariantProps } from 'class-variance-authority';
import { forwardRef, type ButtonHTMLAttributes } from 'react';

import { cn } from '@/lib/cn';

const button = cva(
  'inline-flex items-center justify-center gap-1.5 whitespace-nowrap rounded-md font-medium transition-colors disabled:pointer-events-none disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ring)] focus-visible:ring-offset-1 focus-visible:ring-offset-[var(--ring-offset)]',
  {
    variants: {
      variant: {
        primary: 'bg-[var(--primary)] text-white hover:bg-[var(--primary-hover)] shadow-xs',
        secondary: 'bg-surface text-foreground border border-border hover:bg-surface-hover shadow-xs',
        ghost: 'text-foreground hover:bg-surface-hover',
        subtle: 'bg-surface-hover text-foreground hover:bg-surface-active',
        danger: 'bg-[var(--destructive)] text-white hover:opacity-90 shadow-xs',
        link: 'text-[var(--primary)] underline-offset-2 hover:underline',
      },
      size: {
        xs: 'h-6 px-2 text-xs',
        sm: 'h-7 px-2.5 text-[13px]',
        md: 'h-8 px-3 text-[13px]',
        lg: 'h-10 px-4 text-sm',
        icon: 'h-7 w-7',
      },
    },
    defaultVariants: { variant: 'secondary', size: 'md' },
  },
);

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement>, VariantProps<typeof button> {
  asChild?: boolean;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild, ...rest }, ref) => {
    const Comp = asChild ? Slot : 'button';
    return <Comp ref={ref} className={cn(button({ variant, size }), className)} {...rest} />;
  },
);
Button.displayName = 'Button';
