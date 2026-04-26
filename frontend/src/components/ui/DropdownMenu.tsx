import * as DropdownMenuPrimitive from '@radix-ui/react-dropdown-menu';
import { Check } from 'lucide-react';
import {
  forwardRef,
  type ComponentPropsWithoutRef,
  type ElementRef,
  type HTMLAttributes,
} from 'react';

import { cn } from '@/lib/cn';

export const DropdownMenu = DropdownMenuPrimitive.Root;
export const DropdownMenuTrigger = DropdownMenuPrimitive.Trigger;
export const DropdownMenuPortal = DropdownMenuPrimitive.Portal;

export const DropdownMenuContent = forwardRef<
  ElementRef<typeof DropdownMenuPrimitive.Content>,
  ComponentPropsWithoutRef<typeof DropdownMenuPrimitive.Content>
>(function DropdownMenuContent({ className, sideOffset = 4, ...props }, ref) {
  return (
    <DropdownMenuPrimitive.Portal>
      <DropdownMenuPrimitive.Content
        ref={ref}
        sideOffset={sideOffset}
        className={cn(
          'z-50 min-w-[12rem] overflow-hidden rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--card)] p-1 text-[var(--foreground)] shadow-md',
          className,
        )}
        {...props}
      />
    </DropdownMenuPrimitive.Portal>
  );
});

export const DropdownMenuLabel = forwardRef<
  ElementRef<typeof DropdownMenuPrimitive.Label>,
  ComponentPropsWithoutRef<typeof DropdownMenuPrimitive.Label>
>(function DropdownMenuLabel({ className, ...props }, ref) {
  return (
    <DropdownMenuPrimitive.Label
      ref={ref}
      className={cn('px-2 py-1.5 text-xs font-semibold text-[var(--muted-foreground)]', className)}
      {...props}
    />
  );
});

export const DropdownMenuSeparator = forwardRef<
  ElementRef<typeof DropdownMenuPrimitive.Separator>,
  ComponentPropsWithoutRef<typeof DropdownMenuPrimitive.Separator>
>(function DropdownMenuSeparator({ className, ...props }, ref) {
  return (
    <DropdownMenuPrimitive.Separator
      ref={ref}
      className={cn('-mx-1 my-1 h-px bg-[var(--border)]', className)}
      {...props}
    />
  );
});

export const DropdownMenuCheckboxItem = forwardRef<
  ElementRef<typeof DropdownMenuPrimitive.CheckboxItem>,
  ComponentPropsWithoutRef<typeof DropdownMenuPrimitive.CheckboxItem>
>(function DropdownMenuCheckboxItem({ className, children, ...props }, ref) {
  return (
    <DropdownMenuPrimitive.CheckboxItem
      ref={ref}
      className={cn(
        'relative flex cursor-default select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none transition-colors focus:bg-[var(--muted)] data-[disabled]:pointer-events-none data-[disabled]:opacity-50',
        className,
      )}
      {...props}
    >
      <span className="absolute left-2 flex size-4 items-center justify-center">
        <DropdownMenuPrimitive.ItemIndicator>
          <Check className="size-4" />
        </DropdownMenuPrimitive.ItemIndicator>
      </span>
      {children}
    </DropdownMenuPrimitive.CheckboxItem>
  );
});

export function DropdownMenuItemGroup({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return <div className={cn('flex flex-col gap-px', className)} {...props} />;
}
