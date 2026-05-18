import { Check, Pencil, X } from 'lucide-react';
import { useState } from 'react';

import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Textarea } from '@/components/ui/Textarea';
import { useUpdateCase } from '@/hooks/useCases';
import { renderFieldValue } from '@/lib/renderFieldValue';
import type { CaseDto } from '@/types/case';
import type { FieldDefinition } from '@/types/caseType';

export function PropertiesTab({ dto }: { dto: CaseDto }) {
  const fields = [...dto.caseType.fields].sort((a, b) => a.order - b.order);

  return (
    <div className="px-6 py-5">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-1.5 max-w-3xl">
        {fields.map((f) => (
          <FieldRow key={f.id} dto={dto} field={f} />
        ))}
      </div>
    </div>
  );
}

function FieldRow({ dto, field }: { dto: CaseDto; field: FieldDefinition }) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState<string>(String(dto.data[field.id] ?? ''));
  const update = useUpdateCase(dto.id);

  const cancel = () => {
    setDraft(String(dto.data[field.id] ?? ''));
    setEditing(false);
  };

  const save = async () => {
    try {
      await update.mutateAsync({
        data: { ...dto.data, [field.id]: coerce(field, draft) },
        version: dto.version,
      });
      setEditing(false);
    } catch {
      /* surfaced via mutation state — silent toast */
    }
  };

  return (
    <div className="group grid grid-cols-[140px_1fr_auto] items-start gap-3 py-2 border-b border-divider">
      <div className="text-[12px] text-foreground-muted pt-1">{field.displayName}</div>
      <div className="text-[13px]">
        {editing ? (
          field.type === 'textarea' ? (
            <Textarea value={draft} onChange={(e) => setDraft(e.target.value)} />
          ) : field.type === 'select' ? (
            <select
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              className="h-8 w-full rounded-md border border-border bg-surface px-2.5 text-[13px]"
            >
              <option value="">—</option>
              {field.options.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
          ) : (
            <Input
              type={field.type === 'number' ? 'number' : field.type === 'date' ? 'date' : 'text'}
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
            />
          )
        ) : (
          <span className="text-foreground">{renderFieldValue(field, dto.data[field.id])}</span>
        )}
      </div>
      <div className="flex items-center gap-1 pt-0.5">
        {editing ? (
          <>
            <Button size="icon" variant="ghost" onClick={cancel} aria-label="Cancel">
              <X className="size-3.5" />
            </Button>
            <Button size="icon" variant="primary" onClick={save} disabled={update.isPending} aria-label="Save">
              <Check className="size-3.5" />
            </Button>
          </>
        ) : (
          <button
            type="button"
            onClick={() => setEditing(true)}
            aria-label={`Edit ${field.displayName}`}
            className="opacity-0 group-hover:opacity-100 inline-flex size-7 items-center justify-center rounded text-foreground-muted hover:bg-surface-hover"
          >
            <Pencil className="size-3.5" />
          </button>
        )}
      </div>
    </div>
  );
}

function coerce(field: FieldDefinition, raw: string): unknown {
  if (raw === '') return null;
  if (field.type === 'number') return Number(raw);
  if (field.type === 'checkbox') return raw === 'true';
  return raw;
}
