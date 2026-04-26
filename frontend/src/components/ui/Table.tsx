import {
  forwardRef,
  type HTMLAttributes,
  type TdHTMLAttributes,
  type ThHTMLAttributes,
} from 'react';

import { cn } from '@/lib/cn';

export const Table = forwardRef<HTMLTableElement, HTMLAttributes<HTMLTableElement>>(function Table(
  { className, ...props },
  ref,
) {
  return (
    <div className="w-full overflow-auto">
      <table ref={ref} className={cn('w-full caption-bottom text-sm', className)} {...props} />
    </div>
  );
});

export const THead = forwardRef<HTMLTableSectionElement, HTMLAttributes<HTMLTableSectionElement>>(
  function THead({ className, ...props }, ref) {
    return (
      <thead
        ref={ref}
        className={cn('border-b border-[var(--border)] bg-[var(--muted)]', className)}
        {...props}
      />
    );
  },
);

export const TBody = forwardRef<HTMLTableSectionElement, HTMLAttributes<HTMLTableSectionElement>>(
  function TBody({ className, ...props }, ref) {
    return <tbody ref={ref} className={cn('[&_tr:last-child]:border-0', className)} {...props} />;
  },
);

export const Tr = forwardRef<HTMLTableRowElement, HTMLAttributes<HTMLTableRowElement>>(function Tr(
  { className, ...props },
  ref,
) {
  return (
    <tr
      ref={ref}
      className={cn(
        'border-b border-[var(--border)] transition-colors hover:bg-[var(--muted)]/50 data-[state=selected]:bg-[var(--muted)]',
        className,
      )}
      {...props}
    />
  );
});

export const Th = forwardRef<HTMLTableCellElement, ThHTMLAttributes<HTMLTableCellElement>>(
  function Th({ className, scope, ...props }, ref) {
    // `scope` must be destructured out of `props` before the spread, otherwise `{...props}`
    // would overwrite the defaulted attribute below with `undefined` from the original props bag.
    return (
      <th
        ref={ref}
        scope={scope ?? 'col'}
        className={cn(
          'h-10 px-2 text-left align-middle font-medium text-[var(--muted-foreground)]',
          className,
        )}
        {...props}
      />
    );
  },
);

export const Td = forwardRef<HTMLTableCellElement, TdHTMLAttributes<HTMLTableCellElement>>(
  function Td({ className, ...props }, ref) {
    return <td ref={ref} className={cn('p-2 align-middle', className)} {...props} />;
  },
);
