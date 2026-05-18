import { useEffect, useRef, useState } from 'react';

import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import type { FieldDefinition } from '@/types/caseType';

export type InlineEditableType = 'text' | 'number' | 'date' | 'select' | 'email';

export function isInlineEditableField(field: FieldDefinition): boolean {
  return (
    field.type === 'text' ||
    field.type === 'number' ||
    field.type === 'date' ||
    field.type === 'select' ||
    field.type === 'email'
  );
}

interface BaseProps {
  initialValue: unknown;
  onCommit: (next: unknown) => void;
  onCancel: () => void;
}

export function InlineFieldEditor({
  field,
  ...rest
}: BaseProps & { field: FieldDefinition }) {
  if (field.type === 'select') {
    return <InlineSelectEditor options={field.options ?? []} {...rest} />;
  }
  const inputType =
    field.type === 'number' ? 'number' : field.type === 'date' ? 'date' : field.type === 'email' ? 'email' : 'text';
  return <InlineTextEditor inputType={inputType} {...rest} />;
}

export function InlineTextEditor({
  initialValue,
  onCommit,
  onCancel,
  inputType = 'text',
  placeholder,
}: BaseProps & { inputType?: 'text' | 'number' | 'date' | 'email'; placeholder?: string }) {
  const [value, setValue] = useState<string>(initialValue == null ? '' : String(initialValue));
  const ref = useRef<HTMLInputElement>(null);

  useEffect(() => {
    ref.current?.focus();
    ref.current?.select();
  }, []);

  return (
    <input
      ref={ref}
      type={inputType}
      value={value}
      placeholder={placeholder}
      onChange={(e) => setValue(e.target.value)}
      onClick={(e) => e.stopPropagation()}
      onKeyDown={(e) => {
        e.stopPropagation();
        if (e.key === 'Enter') {
          e.preventDefault();
          commit();
        } else if (e.key === 'Escape') {
          e.preventDefault();
          onCancel();
        }
      }}
      onBlur={commit}
      className="h-7 w-full rounded border border-[var(--primary)] bg-surface px-1.5 text-[13px] focus:outline-none"
    />
  );

  function commit() {
    if (inputType === 'number') {
      if (value === '') return onCommit(null);
      const n = Number(value);
      if (Number.isNaN(n)) return onCancel();
      onCommit(n);
    } else {
      onCommit(value === '' ? null : value);
    }
  }
}

export function InlineSelectEditor({
  initialValue,
  onCommit,
  onCancel,
  options,
}: BaseProps & { options: { value: string; label: string }[] }) {
  const [open, setOpen] = useState(true);
  const initial = initialValue == null ? '' : String(initialValue);

  return (
    <div onClick={(e) => e.stopPropagation()} onKeyDown={(e) => e.stopPropagation()}>
      <Select
        defaultValue={initial}
        open={open}
        onOpenChange={(o) => {
          setOpen(o);
          if (!o) onCancel();
        }}
        onValueChange={(v) => {
          setOpen(false);
          onCommit(v);
        }}
      >
        <SelectTrigger className="h-7 w-full border-[var(--primary)]">
          <SelectValue placeholder="Select…" />
        </SelectTrigger>
        <SelectContent>
          {options.map((o) => (
            <SelectItem key={o.value} value={o.value}>
              {o.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}
