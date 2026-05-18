import * as SelectPrimitive from '@radix-ui/react-select';
import { Check, ChevronDown } from 'lucide-react';
import { forwardRef, type ComponentPropsWithoutRef, type ElementRef } from 'react';
import { cn } from '@/lib/cn';

export const Select = SelectPrimitive.Root;
export const SelectValue = SelectPrimitive.Value;

export const SelectTrigger = forwardRef<
  ElementRef<typeof SelectPrimitive.Trigger>,
  ComponentPropsWithoutRef<typeof SelectPrimitive.Trigger>
>(({ className, children, ...rest }, ref) => (
  <SelectPrimitive.Trigger
    ref={ref}
    className={cn(
      'inline-flex h-8 w-full items-center justify-between rounded-md border border-border bg-surface px-2.5 text-[13px] hover:border-border-strong focus:border-[var(--primary)] focus:outline-none focus:ring-2 focus:ring-[var(--primary-soft)]',
      className,
    )}
    {...rest}
  >
    {children}
    <SelectPrimitive.Icon asChild>
      <ChevronDown className="size-3.5 text-foreground-subtle" />
    </SelectPrimitive.Icon>
  </SelectPrimitive.Trigger>
));
SelectTrigger.displayName = 'SelectTrigger';

export const SelectContent = forwardRef<
  ElementRef<typeof SelectPrimitive.Content>,
  ComponentPropsWithoutRef<typeof SelectPrimitive.Content>
>(({ className, children, position = 'popper', ...rest }, ref) => (
  <SelectPrimitive.Portal>
    <SelectPrimitive.Content
      ref={ref}
      position={position}
      className={cn(
        'z-50 max-h-72 overflow-hidden rounded-md border border-border bg-surface shadow-lg anim-slide-up',
        position === 'popper' && 'data-[side=bottom]:translate-y-1',
        className,
      )}
      {...rest}
    >
      <SelectPrimitive.Viewport className="p-1 min-w-[var(--radix-select-trigger-width)]">
        {children}
      </SelectPrimitive.Viewport>
    </SelectPrimitive.Content>
  </SelectPrimitive.Portal>
));
SelectContent.displayName = 'SelectContent';

export const SelectItem = forwardRef<
  ElementRef<typeof SelectPrimitive.Item>,
  ComponentPropsWithoutRef<typeof SelectPrimitive.Item>
>(({ className, children, ...rest }, ref) => (
  <SelectPrimitive.Item
    ref={ref}
    className={cn(
      'relative flex h-7 cursor-pointer select-none items-center rounded px-2 pr-7 text-[13px] hover:bg-surface-hover data-[highlighted]:bg-surface-hover data-[highlighted]:outline-none',
      className,
    )}
    {...rest}
  >
    <SelectPrimitive.ItemText>{children}</SelectPrimitive.ItemText>
    <span className="absolute right-2 flex h-4 w-4 items-center justify-center">
      <SelectPrimitive.ItemIndicator>
        <Check className="size-3.5" />
      </SelectPrimitive.ItemIndicator>
    </span>
  </SelectPrimitive.Item>
));
SelectItem.displayName = 'SelectItem';
