import * as DialogPrimitive from '@radix-ui/react-dialog';
import { X } from 'lucide-react';
import { forwardRef, type ComponentPropsWithoutRef, type ElementRef, type ReactNode } from 'react';
import { cn } from '@/lib/cn';

export const Dialog = DialogPrimitive.Root;
export const DialogTrigger = DialogPrimitive.Trigger;
export const DialogClose = DialogPrimitive.Close;

export const DialogContent = forwardRef<
  ElementRef<typeof DialogPrimitive.Content>,
  ComponentPropsWithoutRef<typeof DialogPrimitive.Content> & {
    title?: string;
    description?: string;
    children: ReactNode;
  }
>(({ className, title, description, children, ...rest }, ref) => (
  <DialogPrimitive.Portal>
    <DialogPrimitive.Overlay className="fixed inset-0 z-50 bg-black/30 anim-fade" />
    <DialogPrimitive.Content
      ref={ref}
      className={cn(
        'fixed left-1/2 top-1/2 z-50 w-full max-w-lg -translate-x-1/2 -translate-y-1/2 rounded-lg bg-surface shadow-xl border border-border p-5 anim-slide-up',
        className,
      )}
      {...rest}
    >
      {title && (
        <DialogPrimitive.Title className="text-base font-semibold mb-1">{title}</DialogPrimitive.Title>
      )}
      {description && (
        <DialogPrimitive.Description className="text-[13px] text-foreground-muted mb-3">
          {description}
        </DialogPrimitive.Description>
      )}
      {children}
      <DialogPrimitive.Close
        className="absolute right-3 top-3 inline-flex size-7 items-center justify-center rounded-md text-foreground-muted hover:bg-surface-hover"
        aria-label="Close"
      >
        <X className="size-4" />
      </DialogPrimitive.Close>
    </DialogPrimitive.Content>
  </DialogPrimitive.Portal>
));
DialogContent.displayName = 'DialogContent';
