import { zodResolver } from '@hookform/resolvers/zod';
import { useMemo, useState } from 'react';
import { FormProvider, useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { z } from 'zod';

import { ApiError } from '@/api/client';
import { Button } from '@/components/ui/Button';
import { Dialog, DialogContent } from '@/components/ui/Dialog';
import { Input } from '@/components/ui/Input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import { useCaseType, useCaseTypes } from '@/hooks/useCaseTypes';
import { useCreateCase } from '@/hooks/useCases';
import { buildZodFromFieldDefs } from '@/lib/buildZodFromFieldDefs';
import { useUiStore } from '@/stores/uiStore';

export function NewCaseDialog({ open, onOpenChange }: { open: boolean; onOpenChange: (v: boolean) => void }) {
  const { data: types } = useCaseTypes();
  const allowed = (types ?? []).filter((t) => !t.permissions || t.permissions.includes('create'));
  const [caseTypeId, setCaseTypeId] = useState<string>('');
  const effectiveId = caseTypeId || allowed[0]?.id || '';
  const { data: view } = useCaseType(effectiveId || undefined);

  const createFields = useMemo(() => (view ? view.fields.filter((f) => f.requiredOnCreate) : []), [view]);
  const schema = useMemo(
    () => (view ? buildZodFromFieldDefs(createFields, 'create') : z.object({})),
    [view, createFields],
  );

  const methods = useForm<Record<string, unknown>>({
    resolver: zodResolver(schema),
    mode: 'onBlur',
    defaultValues: {},
  });
  const { handleSubmit, register, formState, reset, setError } = methods;

  const create = useCreateCase();
  const push = useUiStore((s) => s.pushRecentlyCreated);
  const navigate = useNavigate();
  const [envelopeError, setEnvelopeError] = useState<string | null>(null);

  const onSubmit = async (values: Record<string, unknown>) => {
    if (!effectiveId) return;
    setEnvelopeError(null);
    try {
      const dto = await create.mutateAsync({ caseTypeId: effectiveId, data: values });
      push(dto.id);
      reset();
      onOpenChange(false);
      navigate(`/cases/${dto.id}`);
    } catch (err) {
      if (err instanceof ApiError && err.envelopeErrors?.length) {
        for (const e of err.envelopeErrors) {
          if (e.field) setError(e.field, { type: 'server', message: e.message });
        }
      }
      setEnvelopeError("Couldn't create case. Check highlighted fields.");
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent title="Create case" description="Pick a case type and fill in the required fields.">
        <FormProvider {...methods}>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-3" noValidate>
            <div>
              <label className="block text-[12px] font-medium mb-1.5">Case type</label>
              <Select
                value={effectiveId}
                onValueChange={(v) => setCaseTypeId(v)}
                disabled={allowed.length === 0}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Pick a case type" />
                </SelectTrigger>
                <SelectContent>
                  {allowed.map((t) => (
                    <SelectItem key={t.id} value={t.id}>
                      {t.displayName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {createFields.map((f) => {
              const err = formState.errors[f.id]?.message as string | undefined;
              return (
                <div key={f.id}>
                  <label htmlFor={`new-${f.id}`} className="block text-[12px] font-medium mb-1.5">
                    {f.displayName} {f.required && <span className="text-[var(--destructive)]">*</span>}
                  </label>
                  {f.type === 'textarea' ? (
                    <Textarea id={`new-${f.id}`} aria-invalid={!!err} {...register(f.id)} />
                  ) : f.type === 'select' ? (
                    <select
                      id={`new-${f.id}`}
                      aria-invalid={!!err}
                      className="h-8 w-full rounded-md border border-border bg-surface px-2.5 text-[13px]"
                      {...register(f.id)}
                    >
                      <option value="">Select…</option>
                      {f.options.map((o) => (
                        <option key={o.value} value={o.value}>
                          {o.label}
                        </option>
                      ))}
                    </select>
                  ) : (
                    <Input
                      id={`new-${f.id}`}
                      type={
                        f.type === 'number'
                          ? 'number'
                          : f.type === 'date'
                            ? 'date'
                            : f.type === 'email'
                              ? 'email'
                              : 'text'
                      }
                      aria-invalid={!!err}
                      {...register(f.id)}
                    />
                  )}
                  {err && <p className="mt-1 text-[12px] text-[var(--destructive)]">{err}</p>}
                </div>
              );
            })}

            {envelopeError && (
              <div
                role="alert"
                className="rounded-md border border-[var(--destructive)] bg-[var(--destructive-soft)] px-3 py-2 text-[12px] text-[var(--destructive)]"
              >
                {envelopeError}
              </div>
            )}

            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="ghost" onClick={() => onOpenChange(false)}>
                Cancel
              </Button>
              <Button type="submit" variant="primary" disabled={!effectiveId || create.isPending}>
                {create.isPending ? 'Creating…' : 'Create case'}
              </Button>
            </div>
          </form>
        </FormProvider>
      </DialogContent>
    </Dialog>
  );
}
