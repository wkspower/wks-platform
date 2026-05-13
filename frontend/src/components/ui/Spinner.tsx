import { Loader2 } from 'lucide-react';

import { cn } from '@/lib/cn';

export interface SpinnerProps {
  /** Tailwind size utility, eg `size-4`, `size-5`. Defaults to `size-5`. */
  className?: string;
  /** Optional accessible label. Defaults to a visually hidden "Loading". */
  label?: string;
}

/**
 * Single spinner primitive. The hand-rolled `rounded-full border-2 border-...
 * border-t-transparent` ring used to live in five places — they drifted on
 * thickness and animation timing. Consumers now compose this with their own
 * sizing utility.
 */
export function Spinner({ className, label = 'Loading' }: SpinnerProps) {
  return (
    <>
      <Loader2
        aria-hidden
        className={cn('animate-spin text-[var(--primary)]', className ?? 'size-5')}
      />
      <span className="sr-only">{label}</span>
    </>
  );
}
