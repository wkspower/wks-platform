import { useState } from 'react';

import { Button } from '@/components/ui/Button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/Select';
import { Input } from '@/components/ui/Input';
import type { CaseTypeView } from '@/types/caseType';

export interface BulkActionBarProps {
  count: number;
  onClear: () => void;
  onAssign: (assignee: string) => void;
  onSetStatus: (status: string) => void;
  /** Shared statuses across selected case types — empty disables the status dropdown. */
  commonStatuses: { id: string; displayName: string }[];
  caseTypes?: CaseTypeView[];
}

export function BulkActionBar({
  count,
  onClear,
  onAssign,
  onSetStatus,
  commonStatuses,
}: BulkActionBarProps) {
  const [mode, setMode] = useState<null | 'assign' | 'status'>(null);
  const [assignee, setAssignee] = useState('');

  return (
    <div
      role="region"
      aria-label="Bulk actions"
      className="sticky bottom-0 z-10 flex items-center gap-3 border-t border-border bg-surface px-4 py-2 shadow-[0_-4px_12px_rgba(0,0,0,0.04)]"
    >
      <span className="text-[13px] font-medium">
        {count} {count === 1 ? 'case' : 'cases'} selected
      </span>

      <div className="ml-auto flex items-center gap-2">
        {mode === null && (
          <>
            <Button variant="ghost" size="xs" onClick={() => setMode('assign')}>
              Assign…
            </Button>
            <Button
              variant="ghost"
              size="xs"
              onClick={() => setMode('status')}
              disabled={commonStatuses.length === 0}
              title={commonStatuses.length === 0 ? 'Selected case types share no statuses' : undefined}
            >
              Set status…
            </Button>
            <Button variant="ghost" size="xs" onClick={onClear}>
              Clear selection
            </Button>
          </>
        )}

        {mode === 'assign' && (
          <>
            <Input
              autoFocus
              placeholder="username"
              value={assignee}
              onChange={(e) => setAssignee(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && assignee.trim()) {
                  onAssign(assignee.trim());
                  setAssignee('');
                  setMode(null);
                } else if (e.key === 'Escape') {
                  setMode(null);
                }
              }}
              className="h-7 w-48"
            />
            <Button
              variant="primary"
              size="xs"
              disabled={!assignee.trim()}
              onClick={() => {
                onAssign(assignee.trim());
                setAssignee('');
                setMode(null);
              }}
            >
              Assign
            </Button>
            <Button variant="ghost" size="xs" onClick={() => setMode(null)}>
              Cancel
            </Button>
          </>
        )}

        {mode === 'status' && (
          <>
            <Select
              onValueChange={(v) => {
                onSetStatus(v);
                setMode(null);
              }}
            >
              <SelectTrigger className="h-7 w-48">
                <SelectValue placeholder="Status…" />
              </SelectTrigger>
              <SelectContent>
                {commonStatuses.map((s) => (
                  <SelectItem key={s.id} value={s.id}>
                    {s.displayName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Button variant="ghost" size="xs" onClick={() => setMode(null)}>
              Cancel
            </Button>
          </>
        )}
      </div>
    </div>
  );
}
