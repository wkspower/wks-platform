import { Filter, X } from 'lucide-react';
import { useCaseTypes } from '@/hooks/useCaseTypes';
import { useUiStore } from '@/stores/uiStore';
import { cn } from '@/lib/cn';

export function FilterBar() {
  const { data: caseTypes } = useCaseTypes();
  const filters = useUiStore((s) => s.caseListFilters);
  const setFilters = useUiStore((s) => s.setCaseListFilters);
  const clear = useUiStore((s) => s.clearCaseListFilters);

  const toggle = (id: string) => {
    const next = filters.caseTypeIds.includes(id)
      ? filters.caseTypeIds.filter((x) => x !== id)
      : [...filters.caseTypeIds, id];
    setFilters({ ...filters, caseTypeIds: next });
  };

  const hasActive = filters.caseTypeIds.length > 0 || filters.statusIds.length > 0;

  return (
    <div className="flex items-center gap-2 flex-wrap">
      <div className="inline-flex items-center gap-1.5 text-foreground-subtle text-[12px]">
        <Filter className="size-3.5" />
        Case type
      </div>
      <div className="flex flex-wrap gap-1.5">
        {(caseTypes ?? []).map((ct) => {
          const active = filters.caseTypeIds.includes(ct.id);
          return (
            <button
              key={ct.id}
              type="button"
              onClick={() => toggle(ct.id)}
              className={cn(
                'h-7 rounded-full border px-2.5 text-[12px] transition-colors',
                active
                  ? 'bg-[var(--primary-soft)] border-[var(--primary)] text-[var(--primary-soft-on)]'
                  : 'bg-surface border-border text-foreground-muted hover:border-border-strong hover:text-foreground',
              )}
            >
              {ct.displayName}
            </button>
          );
        })}
      </div>
      {hasActive && (
        <button
          type="button"
          onClick={clear}
          className="inline-flex items-center gap-1 text-[12px] text-foreground-subtle hover:text-foreground ml-2"
        >
          <X className="size-3" /> Clear
        </button>
      )}
    </div>
  );
}
