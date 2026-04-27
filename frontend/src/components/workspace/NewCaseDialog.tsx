import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useMemo, useRef, useState } from 'react';
import { FormProvider, useForm, type UseFormReturn } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';

import { ApiError } from '@/api/client';
import { Alert } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { Checkbox } from '@/components/ui/Checkbox';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/Dialog';
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
import { useCreateCase } from '@/hooks/useCases';
import { t } from '@/i18n';
import { buildZodFromFieldDefs } from '@/lib/buildZodFromFieldDefs';
import { useUiStore } from '@/stores/uiStore';
import type { CaseTypeView, FieldDefinition } from '@/types/caseType';

interface ServerErrorBanner {
  title: string;
  message: string;
}

export interface NewCaseDialogProps {
  open: boolean;
  caseType: CaseTypeView | null;
  onOpenChange: (open: boolean) => void;
}

/**
 * Story 2.7 — Case-creation dialog. Renders one FormField per `requiredOnCreate: true` field in
 * declared order, validates via runtime-built Zod, and submits via `useCreateCase`. On success
 * primes the detail-cache, invalidates the list, pushes the id into recentlyCreated, and
 * navigates to /cases/{id}. On 422 with field-level errors, maps them onto RHF via setError;
 * envelope errors render a banner with the confidence-not-safety copy.
 */
export function NewCaseDialog({ open, caseType, onOpenChange }: NewCaseDialogProps) {
  const navigate = useNavigate();
  const createCase = useCreateCase();
  const pushRecentlyCreated = useUiStore((s) => s.pushRecentlyCreated);
  const [serverError, setServerError] = useState<ServerErrorBanner | null>(null);
  const bannerRef = useRef<HTMLDivElement | null>(null);

  const requiredFields = useMemo<FieldDefinition[]>(() => {
    if (!caseType) return [];
    return caseType.fields.filter((f) => f.requiredOnCreate).sort((a, b) => a.order - b.order);
  }, [caseType]);

  const schema = useMemo(
    () => (caseType ? buildZodFromFieldDefs(requiredFields, 'create') : null),
    [caseType, requiredFields],
  );

  // Default values keyed by field id so controlled inputs are always controlled. Checkboxes
  // default to false, everything else to ''. P20 — derived from the current required-fields list
  // so a same-id YAML refresh that swaps the schema also re-keys defaults.
  const defaultValues = useMemo<Record<string, unknown>>(() => {
    const dv: Record<string, unknown> = {};
    for (const f of requiredFields) dv[f.id] = f.type === 'checkbox' ? false : '';
    return dv;
  }, [requiredFields]);

  const form = useForm<Record<string, unknown>>({
    resolver: schema ? zodResolver(schema) : undefined,
    mode: 'onBlur',
    reValidateMode: 'onChange',
    shouldFocusError: true,
    defaultValues,
  });

  // Reset on dialog open OR when the field set changes (case-type switch / YAML refresh). The
  // dep on `defaultValues` (memoised on requiredFields) handles the same-id-different-schema
  // case the previous `[open, caseType?.id]` dep missed.
  useEffect(() => {
    if (!open) return;
    form.reset(defaultValues);
    setServerError(null);
    createCase.reset();
    // form/createCase are stable refs from RHF/TanStack — disabling exhaustive-deps to avoid
    // pinning over-eagerly.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, defaultValues]);

  // P22 — focus the form-level error banner when one appears so screen readers announce it
  // immediately and keyboard users land on the recovery copy.
  useEffect(() => {
    if (!serverError) return;
    bannerRef.current?.focus();
  }, [serverError]);

  if (!caseType) return null;

  const isPending = createCase.isPending;
  const state: MutationButtonState = isPending
    ? 'confirming'
    : createCase.isSuccess
      ? 'confirmed'
      : createCase.isError || serverError
        ? 'failed'
        : 'idle';

  async function onSubmit(values: Record<string, unknown>): Promise<void> {
    setServerError(null);
    try {
      // We've already exited the !caseType guard above — narrow safely.
      const ct = caseType!;
      const dto = await createCase.mutateAsync({
        caseTypeId: ct.id,
        data: values,
        assignee: null,
      });
      pushRecentlyCreated(dto.id);
      onOpenChange(false);
      navigate(`/cases/${dto.id}`);
    } catch (err) {
      handleSubmitError(err);
    }
  }

  function handleSubmitError(err: unknown): void {
    if (err instanceof ApiError && err.status === 403) {
      // P4 — 403 means permission was revoked between dropdown render and submit. Surface
      // auth-specific copy so the user knows retry is futile until they refresh / talk to admin.
      setServerError({
        title: t('cases.create.errorBanner.forbiddenTitle'),
        message: t('cases.create.errorBanner.forbiddenBody'),
      });
      return;
    }
    if (err instanceof ApiError && err.status === 422 && err.envelopeErrors) {
      let firstFieldName: string | null = null;
      const unmapped: string[] = [];
      const knownIds = new Set(caseType!.fields.map((f) => f.id));
      for (const e of err.envelopeErrors) {
        // P2 — route every field error through setError, not just `requiredOnCreate` ones.
        // Backend may legitimately reject a non-required field (e.g. defaulted by mapper).
        if (e.field && knownIds.has(e.field)) {
          form.setError(e.field, { type: 'server', message: e.message });
          if (firstFieldName === null) firstFieldName = e.field;
        } else if (e.message) {
          unmapped.push(e.message);
        }
      }
      if (firstFieldName === null) {
        setServerError({
          title: t('cases.create.errorBanner.title'),
          message: unmapped.length > 0 ? unmapped.join(' · ') : t('cases.create.errorBanner.body'),
        });
      } else {
        form.setFocus(firstFieldName);
        if (unmapped.length > 0) {
          // Field errors are surfaced inline; surface any non-field errors in the banner so they
          // aren't silently dropped.
          setServerError({
            title: t('cases.create.errorBanner.title'),
            message: unmapped.join(' · '),
          });
        }
      }
      return;
    }
    if (err instanceof ApiError) {
      setServerError({
        title: t('cases.create.errorBanner.title'),
        message: t('cases.create.errorBanner.body'),
      });
      return;
    }
    // Network / unknown — distinguish from server-side rejection per the error-content pattern.
    setServerError({
      title: t('cases.create.errorBanner.title'),
      message: t('cases.create.errorBanner.networkBody'),
    });
  }

  // P10 — block ESC / outside-click while the mutation is in flight. Without this the dialog
  // unmounts mid-request: success fires `pushRecentlyCreated` + navigate from a closed surface,
  // failure setStates an unmounted component and the error vanishes.
  const blockCloseWhilePending = (e: Event) => {
    if (isPending) e.preventDefault();
  };

  // P8 — wire a Retry slot into the failed-state lifecycle button so the user doesn't have to
  // close the dialog to clear the error.
  const retryAction = (
    <Button
      type="button"
      variant="ghost"
      onClick={() => {
        setServerError(null);
        createCase.reset();
        void form.handleSubmit(onSubmit)();
      }}
    >
      {t('common.retry')}
    </Button>
  );

  // P9 — interpolate {reason} into the failed lifecycle label so the SR announcement and
  // visible chip carry the actual server message (or a generic fallback).
  const failedReason =
    serverError?.message ?? (createCase.error instanceof Error ? createCase.error.message : null);
  const failedLabel = failedReason
    ? t('common.lifecycle.failed', { reason: failedReason })
    : t('common.lifecycle.failedNoReason');

  return (
    <Dialog open={open} onOpenChange={(next) => (isPending ? null : onOpenChange(next))}>
      <DialogContent
        onEscapeKeyDown={blockCloseWhilePending}
        onPointerDownOutside={blockCloseWhilePending}
        onInteractOutside={blockCloseWhilePending}
        // P22 — opt out of Radix's default focus-restore so the next page (case detail) can
        // claim focus on its own <h1>.
        onCloseAutoFocus={(e) => e.preventDefault()}
      >
        <DialogHeader>
          <DialogTitle>
            {t('cases.create.dialogTitle', { caseTypeName: caseType.displayName })}
          </DialogTitle>
        </DialogHeader>
        <FormProvider {...form}>
          <form
            noValidate
            onSubmit={form.handleSubmit(onSubmit)}
            aria-busy={isPending ? true : undefined}
            className="flex flex-col gap-[var(--form-field-gap,1rem)]"
          >
            {requiredFields.length === 0 ? (
              <p className="text-sm text-[var(--muted-foreground)]">
                {t('cases.create.noRequired', { caseTypeName: caseType.displayName })}
              </p>
            ) : (
              requiredFields.map((f) => renderField(f, isPending, form))
            )}
            {form.formState.isSubmitted ? (
              <FormErrorsBanner
                errors={collectFormErrors(form, requiredFields)}
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
            <DialogFooter>
              <Button
                variant="ghost"
                type="button"
                onClick={() => onOpenChange(false)}
                disabled={isPending}
              >
                {t('common.cancel')}
              </Button>
              <MutationButton
                state={state}
                confirmingLabel={t('common.lifecycle.confirming')}
                confirmedLabel={t('common.lifecycle.confirmed')}
                failedLabel={failedLabel}
                retryAction={retryAction}
              >
                {t('cases.create.submit')}
              </MutationButton>
            </DialogFooter>
          </form>
        </FormProvider>
      </DialogContent>
    </Dialog>
  );
}

function collectFormErrors(
  form: UseFormReturn<Record<string, unknown>>,
  requiredFields: FieldDefinition[],
): FormErrorEntry[] {
  const errs = form.formState.errors;
  const out: FormErrorEntry[] = [];
  for (const f of requiredFields) {
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
      required={f.requiredOnCreate}
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
      // P14 — `value=""` is a Radix anti-pattern; pass undefined so SelectValue renders the
      // placeholder. P25 — go through field.onChange (RHF onBlur mode) so validation timing
      // matches the rest of the form instead of forcing-validate on every change.
      const stringValue =
        typeof field.value === 'string' && field.value !== '' ? field.value : undefined;
      return (
        <Select
          value={stringValue}
          onValueChange={(v) => {
            field.onChange(v);
            // Trigger field-level blur so onBlur validation kicks in once the user picks.
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
