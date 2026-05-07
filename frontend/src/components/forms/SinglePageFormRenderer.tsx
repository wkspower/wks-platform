import { zodResolver } from '@hookform/resolvers/zod';
import { useMemo, useRef, useState } from 'react';
import { FormProvider, useForm, type UseFormReturn } from 'react-hook-form';

import { ApiError } from '@/api/client';
import { submitForm } from '@/api/forms';
import { Alert } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { Checkbox } from '@/components/ui/Checkbox';
import { FormErrorsBanner, type FormErrorEntry } from '@/components/ui/FormErrorsBanner';
import { FormField, type FormFieldRenderProps } from '@/components/ui/FormField';
import { Input } from '@/components/ui/Input';
import { MutationButton, type MutationButtonState } from '@/components/ui/MutationButton';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import { t } from '@/i18n';
import { buildZodFromFieldDefs } from '@/lib/buildZodFromFieldDefs';
import type { FieldDefinition, FormDefinitionView } from '@/types/caseType';

export interface SinglePageFormRendererProps {
  /** The form definition (id, topology, dataModel, rendering, fields). */
  formDefinition: FormDefinitionView;
  /** The case to submit the form for. */
  caseId: string;
  /** Called after a successful submit so the parent can react (e.g. show a toast, navigate). */
  onSuccess?: () => void;
}

interface ServerErrorBanner {
  title: string;
  message: string;
}

/**
 * Story 5.2 AC1 — Renders a single-page form from a {@link FormDefinitionView}. All fields appear
 * on one page in order-sorted sequence with a single "Submit" button. No pagination, wizard steps,
 * or tabs.
 *
 * AC2 — Field-level validation with inline errors + submit-blocked {@link FormErrorsBanner} at top.
 *
 * AC4 — WCAG: focus order follows DOM order (top-to-bottom, which mirrors visual order for a
 * single-page form). Error messages are announced via the existing {@link FormField} wrapper
 * (role="alert", aria-invalid, aria-describedby) and {@link FormErrorsBanner} (role="alert"
 * aria-live="polite") — no additional WCAG work needed.
 *
 * Template: mirrors {@link NewCaseDialog} exactly, rendered inline as a {@code <div>} instead of
 * inside a Dialog wrapper.
 */
export function SinglePageFormRenderer({
  formDefinition,
  caseId,
  onSuccess,
}: SinglePageFormRendererProps) {
  const [serverError, setServerError] = useState<ServerErrorBanner | null>(null);
  const [isPending, setIsPending] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isError, setIsError] = useState(false);
  const bannerRef = useRef<HTMLDivElement | null>(null);

  // AC1 — all fields in order-sorted sequence.
  const sortedFields = useMemo(
    () => [...formDefinition.fields].sort((a, b) => a.order - b.order),
    [formDefinition.fields],
  );

  // AC2 — 'submit' mode: include ALL fields where required === true (not filtered by
  // requiredOnCreate — that is the create-form gate, not the submit-form gate).
  const schema = useMemo(() => buildZodFromFieldDefs(sortedFields, 'submit'), [sortedFields]);

  // Default values keyed by field id so controlled inputs are always controlled.
  const defaultValues = useMemo<Record<string, unknown>>(() => {
    const dv: Record<string, unknown> = {};
    for (const f of sortedFields) dv[f.id] = f.type === 'checkbox' ? false : '';
    return dv;
  }, [sortedFields]);

  const form = useForm<Record<string, unknown>>({
    resolver: zodResolver(schema),
    mode: 'onBlur',
    reValidateMode: 'onChange',
    shouldFocusError: true,
    defaultValues,
  });

  const state: MutationButtonState = isPending
    ? 'confirming'
    : isSuccess
      ? 'confirmed'
      : isError || serverError
        ? 'failed'
        : 'idle';

  async function onSubmit(values: Record<string, unknown>): Promise<void> {
    setServerError(null);
    setIsError(false);
    setIsSuccess(false);
    setIsPending(true);
    try {
      await submitForm(caseId, formDefinition.id, values);
      setIsSuccess(true);
      onSuccess?.();
    } catch (err) {
      handleSubmitError(err);
    } finally {
      setIsPending(false);
    }
  }

  function handleSubmitError(err: unknown): void {
    setIsError(true);
    if (err instanceof ApiError && err.status === 422 && err.envelopeErrors) {
      // Map field-level errors (WKS-FORM-002) to RHF setError so they appear inline.
      const knownIds = new Set(sortedFields.map((f) => f.id));
      const unmapped: string[] = [];
      let firstFieldName: string | null = null;
      for (const e of err.envelopeErrors) {
        if (e.field && knownIds.has(e.field)) {
          form.setError(e.field, { type: 'server', message: e.message });
          if (firstFieldName === null) firstFieldName = e.field;
        } else if (e.message) {
          unmapped.push(e.message);
        }
      }
      if (firstFieldName !== null) {
        form.setFocus(firstFieldName);
      }
      if (unmapped.length > 0 || firstFieldName === null) {
        setServerError({
          title: t('cases.create.errorBanner.title'),
          message: unmapped.length > 0 ? unmapped.join(' · ') : t('cases.create.errorBanner.body'),
        });
        bannerRef.current?.focus();
      }
      return;
    }
    if (err instanceof ApiError) {
      setServerError({
        title: t('cases.create.errorBanner.title'),
        message: t('cases.create.errorBanner.body'),
      });
    } else {
      setServerError({
        title: t('cases.create.errorBanner.title'),
        message: t('cases.create.errorBanner.networkBody'),
      });
    }
    bannerRef.current?.focus();
  }

  const failedReason = serverError?.message ?? null;
  const failedLabel = failedReason
    ? t('common.lifecycle.failed', { reason: failedReason })
    : t('common.lifecycle.failedNoReason');

  const retryAction = (
    <Button
      type="button"
      variant="ghost"
      onClick={() => {
        setServerError(null);
        setIsError(false);
        void form.handleSubmit(onSubmit)();
      }}
    >
      {t('common.retry')}
    </Button>
  );

  return (
    <div>
      <FormProvider {...form}>
        <form
          noValidate
          onSubmit={form.handleSubmit(onSubmit)}
          aria-busy={isPending ? true : undefined}
          className="flex flex-col gap-[var(--form-field-gap,1rem)]"
        >
          {sortedFields.length === 0 ? (
            <p className="text-sm text-[var(--muted-foreground)]">
              {t('cases.create.noRequired', { caseTypeName: formDefinition.id })}
            </p>
          ) : (
            sortedFields.map((f) => renderField(f, isPending, form))
          )}
          {form.formState.isSubmitted ? (
            <FormErrorsBanner
              errors={collectFormErrors(form, sortedFields)}
              onAnchorClick={(field) => form.setFocus(field)}
            />
          ) : null}
          {serverError ? (
            <Alert
              ref={bannerRef}
              tabIndex={-1}
              role="alert"
              variant="destructive"
              className="py-[var(--space-2)] text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ring)]"
            >
              <strong className="block">{serverError.title}</strong>
              <span>{serverError.message}</span>
            </Alert>
          ) : null}
          <div className="flex justify-end">
            <MutationButton
              state={state}
              confirmingLabel={t('common.lifecycle.confirming')}
              confirmedLabel={t('common.lifecycle.confirmed')}
              failedLabel={failedLabel}
              retryAction={retryAction}
            >
              {t('form.submit')}
            </MutationButton>
          </div>
        </form>
      </FormProvider>
    </div>
  );
}

function collectFormErrors(
  form: UseFormReturn<Record<string, unknown>>,
  fields: FieldDefinition[],
): FormErrorEntry[] {
  const errs = form.formState.errors;
  const out: FormErrorEntry[] = [];
  for (const f of fields) {
    if (errs[f.id]) {
      out.push({ field: f.id, displayName: f.displayName, order: f.order });
    }
  }
  return out;
}

function renderField(
  f: FieldDefinition,
  disabled: boolean,
  form: UseFormReturn<Record<string, unknown>>,
) {
  if (f.type === 'file') {
    return (
      <p key={f.id} className="text-sm text-[var(--muted-foreground)]">
        {t('cases.create.fileNotYet')}
      </p>
    );
  }
  return (
    <FormField
      key={f.id}
      name={f.id}
      label={f.displayName}
      required={f.required}
      disabled={disabled}
    >
      {(field) => renderInput(f, field, form)}
    </FormField>
  );
}

function renderInput(
  f: FieldDefinition,
  field: FormFieldRenderProps<Record<string, unknown>, string>,
  form: UseFormReturn<Record<string, unknown>>,
) {
  switch (f.type) {
    case 'text':
      return (
        <Input
          type="text"
          maxLength={f.maxLength ?? undefined}
          {...field}
          value={String(field.value ?? '')}
        />
      );
    case 'textarea':
      return (
        <Textarea
          maxLength={f.maxLength ?? undefined}
          {...field}
          value={String(field.value ?? '')}
        />
      );
    case 'number':
      return (
        <Input
          type="number"
          inputMode="numeric"
          min={f.min ?? undefined}
          max={f.max ?? undefined}
          step={f.step ?? 1}
          {...field}
          value={field.value === undefined || field.value === null ? '' : String(field.value)}
        />
      );
    case 'date':
      return (
        <Input
          type="date"
          min={f.dateMin ?? undefined}
          max={f.dateMax ?? undefined}
          {...field}
          value={String(field.value ?? '')}
        />
      );
    case 'select': {
      const stringValue =
        typeof field.value === 'string' && field.value !== '' ? field.value : undefined;
      return (
        <Select
          value={stringValue}
          onValueChange={(v) => {
            field.onChange(v);
            void form.trigger(f.id);
          }}
          disabled={field.disabled}
        >
          <SelectTrigger
            id={field.id}
            aria-invalid={field['aria-invalid']}
            aria-describedby={field['aria-describedby']}
            ref={field.ref as unknown as React.Ref<HTMLButtonElement>}
          >
            <SelectValue placeholder={t('cases.create.selectPlaceholder')} />
          </SelectTrigger>
          <SelectContent>
            {f.options.map((o) => (
              <SelectItem key={o.value} value={o.value}>
                {o.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      );
    }
    case 'checkbox':
      return (
        <Checkbox
          id={field.id}
          checked={Boolean(field.value)}
          onCheckedChange={(v) => {
            field.onChange(Boolean(v));
            void form.trigger(f.id);
          }}
          disabled={field.disabled}
          aria-invalid={field['aria-invalid']}
          aria-describedby={field['aria-describedby']}
          ref={field.ref as unknown as React.Ref<HTMLButtonElement>}
        />
      );
    default:
      return null;
  }
}
