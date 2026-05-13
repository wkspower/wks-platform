import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useMemo, useRef, useState } from 'react';
import { FormProvider, useForm, type UseFormReturn } from 'react-hook-form';

import { ApiError } from '@/api/client';
import { submitForm } from '@/api/forms';
import { Alert } from '@/components/ui/Alert';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/AlertDialog';
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
import { useUserRoles } from '@/hooks/useUserRoles';
import { t } from '@/i18n';
import { getArchetypeAffordance } from '@/lib/archetypes';
import { buildZodFromFieldDefs } from '@/lib/buildZodFromFieldDefs';
import type { CaseTypeView, FieldDefinition, FormDefinitionView } from '@/types/caseType';

import { deriveFieldDisabled } from './deriveFieldDisabled';

export interface SinglePageFormRendererProps {
  /** The form definition (id, topology, dataModel, rendering, fields). */
  formDefinition: FormDefinitionView;
  /** The case to submit the form for. */
  caseId: string;
  /**
   * Existing case data to prefill the form with (P8). When provided, field values from the case
   * are used as initial values so users see current data when reopening a form.
   */
  defaultValues?: Record<string, unknown>;
  /** Called after a successful submit so the parent can react (e.g. show a toast, navigate). */
  onSuccess?: () => void;
  /**
   * Story 5.4 AC1 — invoked whenever any field value changes (subscription via RHF watch). The
   * parent (typically {@code FormPage}) wires this to {@code useFormDraft.scheduleSave} so the
   * draft is debounce-PUT to the backend. Optional for backward compatibility with existing
   * callers; when omitted the renderer behaves exactly as before.
   */
  onValuesChange?: (values: Record<string, unknown>) => void;
  /**
   * Story 5.4 AC1 — explicit "Save Draft" button label and onClick. When provided, an extra
   * ghost-variant button is rendered next to Submit; clicking it calls back with the current
   * RHF values so the parent can issue an immediate save (bypassing the debounce). Optional.
   */
  saveDraftAction?: {
    label: string;
    onClick: (values: Record<string, unknown>) => void;
  };
  /**
   * Story 5.6 AC3 / AC4 — Optional case-type view for per-field edit-permission derivation.
   * Carries {@code defaultFieldEditability} (AC4) and {@code roles} (display-name lookup for the
   * disabled-field tooltip). When omitted, every field is rendered without the {@code editableBy}
   * disabled-state derivation — preserves pre-5.6 behavior for callers that do not yet pass the
   * case-type view.
   */
  caseType?: Pick<CaseTypeView, 'defaultFieldEditability' | 'roles'>;
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
  defaultValues: externalDefaultValues,
  onSuccess,
  onValuesChange,
  saveDraftAction,
  caseType,
}: SinglePageFormRendererProps) {
  const userRoles = useUserRoles();
  const [serverError, setServerError] = useState<ServerErrorBanner | null>(null);
  const [isPending, setIsPending] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isError, setIsError] = useState(false);
  // Story 6-3b AC2 — WKS-EDIT-001 is structurally non-retryable from the form; suppress
  // the Retry CTA when set. Reset on every new submit attempt.
  const [editBlocked, setEditBlocked] = useState(false);
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
  // P8: merge externalDefaultValues (case.data) over the blank-field defaults so users see
  // existing case data when reopening the form. The blank-field default ensures every field is
  // controlled even if the case has no stored value for it yet.
  const defaultValues = useMemo<Record<string, unknown>>(() => {
    const dv: Record<string, unknown> = {};
    for (const f of sortedFields) dv[f.id] = f.type === 'checkbox' ? false : '';
    if (externalDefaultValues) {
      Object.assign(dv, externalDefaultValues);
    }
    return dv;
  }, [sortedFields, externalDefaultValues]);

  const form = useForm<Record<string, unknown>>({
    resolver: zodResolver(schema),
    mode: 'onBlur',
    reValidateMode: 'onChange',
    shouldFocusError: true,
    defaultValues,
  });

  // Story 5.4 AC1 — subscribe to value changes and forward to the parent for debounced auto-save.
  useEffect(() => {
    if (!onValuesChange) return;
    const subscription = form.watch((values) => {
      onValuesChange(values as Record<string, unknown>);
    });
    return () => subscription.unsubscribe();
  }, [form, onValuesChange]);

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
    setEditBlocked(false);
    setIsPending(true);
    try {
      await submitForm(caseId, formDefinition.id, values);
      setIsSuccess(true);
      // CF1 fix — delay onSuccess by 1200ms so user sees the MutationButton "confirmed" state
      if (onSuccess) setTimeout(onSuccess, 1200);
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
      // Story 6-3b AC2 — WKS-EDIT-001 (edit-contract gate) is structurally non-retryable:
      // the only way to unblock is to complete the open task. Track presence so the submit
      // CTA's Retry affordance can be suppressed below. The localized copy replaces the
      // wire message so we never leak openTaskId / formId at render time even if a future
      // backend regression reintroduces them.
      let editBlockedSeen = false;
      for (const e of err.envelopeErrors) {
        const displayMessage = e.code === 'WKS-EDIT-001' ? t('form.error.editBlocked') : e.message;
        if (e.code === 'WKS-EDIT-001') {
          editBlockedSeen = true;
        }
        if (e.field && knownIds.has(e.field)) {
          form.setError(e.field, { type: 'server', message: displayMessage });
          if (firstFieldName === null) firstFieldName = e.field;
        } else if (displayMessage) {
          unmapped.push(displayMessage);
        }
      }
      setEditBlocked(editBlockedSeen);
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

  // Story 6.1 AC4 — archetype-driven submit CTA affordance.
  // When archetype is absent, falls back to the pre-6.1 'form.submit' label and default variant,
  // preserving existing behavior exactly (AC4: "for archetype = null, existing behavior preserved").
  const affordance = getArchetypeAffordance(formDefinition.archetype);
  const submitLabel = formDefinition.archetype ? t(affordance.ctaLabelKey) : t('form.submit');
  const submitVariant = formDefinition.archetype
    ? affordance.ctaTone === 'secondary'
      ? 'ghost'
      : 'default'
    : 'default';
  const needsDialog = affordance.confirmationFlow === 'confirmation-dialog';
  const isTerminal =
    affordance.postActionState === 'locked' || affordance.postActionState === 'terminal-accent';

  const submitButton = (
    <MutationButton
      state={state}
      variant={submitVariant}
      confirmingLabel={t('common.lifecycle.confirming')}
      confirmedLabel={t('common.lifecycle.confirmed')}
      failedLabel={failedLabel}
      // Story 6-3b AC2 — suppress Retry CTA for WKS-EDIT-001 (structurally futile until
      // the open task is completed via the bound form, which is the submit currently being
      // rejected by a sibling-form gate — retry has nothing to act on).
      retryAction={editBlocked ? undefined : retryAction}
      // Story 6.1 AC4 — when an AlertDialog interposes, the button must NOT be type="submit"
      // so clicking the trigger opens the dialog rather than running HTML5 form validation.
      // The dialog's AlertDialogAction fires form.handleSubmit(onSubmit) explicitly.
      type={needsDialog ? 'button' : 'submit'}
    >
      {submitLabel}
    </MutationButton>
  );

  return (
    <div data-archetype-terminal={isTerminal && isSuccess ? 'true' : undefined}>
      <FormProvider {...form}>
        <form
          noValidate
          onSubmit={form.handleSubmit(onSubmit)}
          aria-busy={isPending ? true : undefined}
          className="flex flex-col gap-[var(--form-field-gap,1rem)]"
        >
          {form.formState.isSubmitted ? (
            <FormErrorsBanner
              errors={collectFormErrors(form, sortedFields)}
              onAnchorClick={(field) => form.setFocus(field)}
            />
          ) : null}
          {sortedFields.length === 0 ? (
            <p className="text-sm text-[var(--muted-foreground)]">
              {t('cases.create.noRequired', { caseTypeName: formDefinition.id })}
            </p>
          ) : (
            sortedFields.map((f) => renderField(f, isPending, form, caseType, userRoles))
          )}
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
          <div className="flex justify-end gap-2">
            {saveDraftAction ? (
              <Button
                type="button"
                variant="ghost"
                onClick={() => saveDraftAction.onClick(form.getValues())}
              >
                {saveDraftAction.label}
              </Button>
            ) : null}
            {needsDialog ? (
              // Story 6.1 AC4 — business_final: AlertDialog interposes before submitForm.
              <AlertDialog>
                <AlertDialogTrigger asChild>{submitButton}</AlertDialogTrigger>
                <AlertDialogContent>
                  <AlertDialogTitle>{t('task.confirm.title')}</AlertDialogTitle>
                  <AlertDialogDescription>{t('task.confirm.description')}</AlertDialogDescription>
                  <div className="flex justify-end gap-2 pt-2">
                    <AlertDialogCancel>{t('common.cancel')}</AlertDialogCancel>
                    <AlertDialogAction
                      onClick={() => {
                        void form.handleSubmit(onSubmit)();
                      }}
                    >
                      {submitLabel}
                    </AlertDialogAction>
                  </div>
                </AlertDialogContent>
              </AlertDialog>
            ) : (
              submitButton
            )}
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
  pending: boolean,
  form: UseFormReturn<Record<string, unknown>>,
  caseType: Pick<CaseTypeView, 'defaultFieldEditability' | 'roles'> | undefined,
  userRoles: ReadonlySet<string>,
) {
  if (f.type === 'file') {
    return (
      <p key={f.id} className="text-sm text-[var(--muted-foreground)]">
        {t('cases.create.fileNotYet')}
      </p>
    );
  }
  // Story 5.6 AC3 / AC4 — per-field edit-permission disabled state. When caseType is omitted
  // (pre-5.6 callers), the helper falls through with editableBy:[] and default editable behavior.
  const perm = caseType
    ? deriveFieldDisabled(f, caseType, userRoles)
    : { disabled: false, tooltip: null };
  const disabled = pending || perm.disabled;
  return (
    <div key={f.id} title={perm.tooltip ?? undefined}>
      <FormField name={f.id} label={f.displayName} required={f.required} disabled={disabled}>
        {(field) => renderInput(f, field, form)}
      </FormField>
    </div>
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
