import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft } from 'lucide-react';
import { useMemo, useState } from 'react';
import { FormProvider, useForm } from 'react-hook-form';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { z } from 'zod';

import { ApiError } from '@/api/client';
import { submitForm } from '@/api/forms';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Spinner } from '@/components/ui/Spinner';
import { Textarea } from '@/components/ui/Textarea';
import { useCase } from '@/hooks/useCases';
import { buildZodFromFieldDefs } from '@/lib/buildZodFromFieldDefs';
import { caseQueryKeys, taskQueryKeys } from '@/lib/queryKeys';

export function FormPage() {
  const { caseId, formId } = useParams<{ caseId: string; formId: string }>();
  const { data: dto, isLoading } = useCase(caseId ?? null);
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [submitError, setSubmitError] = useState<string | null>(null);

  const form = useMemo(() => dto?.caseType.forms?.find((f) => f.id === formId) ?? null, [dto, formId]);

  const schema = useMemo(() => (form ? buildZodFromFieldDefs(form.fields, 'submit') : z.object({})), [form]);

  const methods = useForm<Record<string, unknown>>({
    resolver: zodResolver(schema),
    mode: 'onBlur',
    defaultValues: useMemo(() => (dto ? { ...dto.data } : {}), [dto]),
  });
  const { register, handleSubmit, formState, setError } = methods;

  const submit = useMutation({
    mutationFn: (values: Record<string, unknown>) => submitForm(caseId!, formId!, values),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: caseQueryKeys.detail(caseId!) });
      qc.invalidateQueries({ queryKey: taskQueryKeys.byCase(caseId!) });
      navigate(`/cases/${caseId}`);
    },
  });

  if (isLoading || !dto) {
    return (
      <div className="grid place-items-center py-20">
        <Spinner className="size-6" />
      </div>
    );
  }
  if (!form) {
    return (
      <div className="px-6 py-12 text-center text-foreground-muted">
        Form not found on this case type.
        <div className="mt-3">
          <Link to={`/cases/${caseId}`} className="text-[var(--primary)] hover:underline">
            ← Back to case
          </Link>
        </div>
      </div>
    );
  }

  const onSubmit = async (values: Record<string, unknown>) => {
    setSubmitError(null);
    try {
      await submit.mutateAsync(values);
    } catch (err) {
      if (err instanceof ApiError && err.envelopeErrors?.length) {
        for (const e of err.envelopeErrors) {
          if (e.field) setError(e.field, { type: 'server', message: e.message });
        }
        setSubmitError('Check highlighted fields and try again.');
      } else {
        setSubmitError('Submission failed.');
      }
    }
  };

  return (
    <div className="min-h-full bg-canvas">
      <header className="border-b border-border px-6 pt-4 pb-3">
        <Button variant="ghost" size="xs" onClick={() => navigate(`/cases/${caseId}`)}>
          <ArrowLeft className="size-3.5" /> Back to case
        </Button>
        <h1 className="mt-2 font-heading text-[18px] font-semibold">{form.id}</h1>
        <p className="text-[12px] text-foreground-muted">Case {dto.id.slice(0, 8)}</p>
      </header>

      <FormProvider {...methods}>
        <form onSubmit={handleSubmit(onSubmit)} className="px-6 py-5 max-w-2xl space-y-4" noValidate>
          {form.fields.map((f) => {
            const err = formState.errors[f.id]?.message as string | undefined;
            return (
              <div key={f.id}>
                <label htmlFor={`f-${f.id}`} className="block text-[12px] font-medium mb-1.5">
                  {f.displayName} {f.required && <span className="text-[var(--destructive)]">*</span>}
                </label>
                {f.type === 'textarea' ? (
                  <Textarea id={`f-${f.id}`} aria-invalid={!!err} {...register(f.id)} />
                ) : f.type === 'select' ? (
                  <select
                    id={`f-${f.id}`}
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
                    id={`f-${f.id}`}
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

          {submitError && (
            <div
              role="alert"
              className="rounded-md border border-[var(--destructive)] bg-[var(--destructive-soft)] px-3 py-2 text-[12px] text-[var(--destructive)]"
            >
              {submitError}
            </div>
          )}

          <div className="pt-2 flex gap-2">
            <Button type="submit" variant="primary" disabled={submit.isPending}>
              {submit.isPending ? 'Submitting…' : 'Submit'}
            </Button>
            <Button type="button" variant="ghost" onClick={() => navigate(`/cases/${caseId}`)}>
              Cancel
            </Button>
          </div>
        </form>
      </FormProvider>
    </div>
  );
}
