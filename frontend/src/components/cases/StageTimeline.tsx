import { Check, Circle, Minus } from 'lucide-react';
import type { StageView } from '@/types/case';
import { cn } from '@/lib/cn';

export function StageTimeline({ stages }: { stages: StageView[] }) {
  if (!stages || stages.length === 0) return null;
  return (
    <ol className="flex items-center gap-1 overflow-x-auto py-2">
      {stages.map((s, i) => (
        <li key={s.stageId} className="flex items-center gap-1 shrink-0">
          <div
            className={cn(
              'inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[12px] border',
              s.state === 'COMPLETED' && 'bg-[var(--success-soft)] text-[var(--success)] border-transparent',
              s.state === 'ACTIVE' && 'bg-[var(--primary-soft)] text-[var(--primary-soft-on)] border-[var(--primary)] font-medium',
              s.state === 'PENDING' && 'bg-surface text-foreground-muted border-border',
              s.state === 'SKIPPED' && 'bg-surface-hover text-foreground-subtle border-border line-through',
            )}
          >
            {s.state === 'COMPLETED' && <Check className="size-3" />}
            {s.state === 'ACTIVE' && <Circle className="size-3 fill-current" />}
            {s.state === 'SKIPPED' && <Minus className="size-3" />}
            {s.state === 'PENDING' && <Circle className="size-3" />}
            <span>{s.displayName}</span>
          </div>
          {i < stages.length - 1 && <span className="w-3 h-px bg-border" />}
        </li>
      ))}
    </ol>
  );
}
