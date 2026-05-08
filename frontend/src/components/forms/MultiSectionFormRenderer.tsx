import { zodResolver } from '@hookform/resolvers/zod';
import { ChevronDown } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
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
import { t } from '@/i18n';
import { getArchetypeAffordance } from '@/lib/archetypes';
import { buildZodFromFieldDefs } from '@/lib/buildZodFromFieldDefs';
import type { FieldDefinition, FormDefinitionView, FormSectionView } from '@/types/caseType';

export interface MultiSectionFormRendererProps {
  /** The form definition (must have sections[]). */
  formDefinition: FormDefinitionView;
  /** The case to submit the form for. */
  caseId: string;
  /**
   * Existing case data to prefill the form with. When provided, field values from the case are used
   * as initial values so users see current data when reopening a form.
   */
  defaultValues?: Record<string, unknown>;
  /** Called after a successful submit so the parent can react (e.g. navigate). */
  onSuccess?: () => void;
  /**
   * Story 5.4 AC1 — invoked whenever any field value changes; the parent wires this to the
   * draft auto-save scheduler. Optional for backward compatibility.
   */
  onValuesChange?: (values: Record<string, unknown>) => void;
  /** Story 5.4 AC1 — explicit "Save Draft" action; renders an extra ghost button next to Submit. */
  saveDraftAction?: {
    label: string;
    onClick: (values: Record<string, unknown>) => void;
  };
}

type SectionStatus = 'incomplete' | 'complete' | 'error';

interface ServerErrorBanner {
  title: string;
  message: string;
}

/**
 * Story 5.3 AC1–4 — Renders a multi-section form from a {@link FormDefinitionView} with {@code
 * dataModel: sectioned}. Each section declared in {@code sections[]} appears as an expandable panel
 * with a section label heading and a validation indicator.
 *
 * AC1 — Sections render as expandable panels with section-level validation indicators.
 * AC2 — Submit is blocked and focuses the first incomplete section when required fields are empty.
 * AC3 — Independent section validation: completing one section does not re-validate others.
 * AC4 — Progress indicator shows "X of Y sections complete".
 *
 * One RHF {@code useForm} for ALL fields across sections (flat namespace — field IDs must be unique
 * across sections). Section panels use React state for expand/collapse, NOT HTML {@code <details>}
 * (needed for programmatic focus to open the correct section on submit error).
 */
export function MultiSectionFormRenderer({
  formDefinition,
  caseId,
  defaultValues: externalDefaultValues,
  onSuccess,
  onValuesChange,
  saveDraftAction,
}: MultiSectionFormRendererProps) {
  const sections = useMemo(() => formDefinition.sections ?? [], [formDefinition.sections]);
  const allFields = useMemo(() => sections.flatMap((s) => s.fields), [sections]);

  const [serverError, setServerError] = useState<ServerErrorBanner | null>(null);
  const [isPending, setIsPending] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isError, setIsError] = useState(false);

  // Track which sections are expanded — all open initially (AC1)
  const [expanded, setExpanded] = useState<Record<string, boolean>>(() =>
    Object.fromEntries(sections.map((s) => [s.id, true])),
  );

  // AC2 — 'submit' mode: include ALL fields where required === true
  const schema = useMemo(() => buildZodFromFieldDefs(allFields, 'submit'), [allFields]);

  // Default values keyed by field id; merge externalDefaultValues (case.data) over blank defaults
  const defaultValues = useMemo<Record<string, unknown>>(() => {
    const dv: Record<string, unknown> = {};
    for (const f of allFields) dv[f.id] = f.type === 'checkbox' ? false : '';
    if (externalDefaultValues) Object.assign(dv, externalDefaultValues);
    return dv;
  }, [allFields, externalDefaultValues]);

  const form = useForm<Record<string, unknown>>({
    resolver: zodResolver(schema),
    mode: 'onBlur',
    reValidateMode: 'onChange',
    // shouldFocusError: false — we handle focus manually to open the correct section first (AC2)
    shouldFocusError: false,
    defaultValues,
  });

  // Story 5.4 AC1 — subscribe to value changes for debounced auto-save.
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

  /**
   * AC1 / AC3 — Section status derived from RHF errors and values.
   * - Before any submit attempt: always 'incomplete' (no pre-validation shown).
   * - After submit: 'error' if any field in section has an error; 'complete' if all required fields
   *   filled and no errors; 'incomplete' otherwise.
   * This is computed per section independently (AC3 — section A errors don't affect section B).
   */
  function getSectionStatus(section: FormSectionView): SectionStatus {
    if (!form.formState.isSubmitted) return 'incomplete';
    const fieldIds = new Set(section.fields.map((f) => f.id));
    const hasErrors = Object.keys(form.formState.errors).some((k) => fieldIds.has(k));
    if (hasErrors) return 'error';
    const allRequiredFilled = section.fields
      .filter((f) => f.required)
      .every((f) => {
        const v = form.getValues(f.id as string);
        return v !== '' && v !== false && v !== null && v !== undefined;
      });
    return allRequiredFilled ? 'complete' : 'incomplete';
  }

  /** AC4 — count sections where all required fields filled and no errors */
  const completeCount = sections.filter((s) => getSectionStatus(s) === 'complete').length;

  /** AC2 — on submit failure: open the first section that has errors, scroll and focus */
  function openFirstErrorSection(): void {
    for (const section of sections) {
      const fieldIds = new Set(section.fields.map((f) => f.id));
      const hasError = Object.keys(form.formState.errors).some((k) => fieldIds.has(k));
      if (hasError) {
        setExpanded((prev) => ({ ...prev, [section.id]: true }));
        document.getElementById(`section-${section.id}`)?.scrollIntoView({ behavior: 'smooth' });
        const firstErrorField = section.fields.find((f) => form.formState.errors[f.id]);
        if (firstErrorField) {
          setTimeout(() => form.setFocus(firstErrorField.id as string), 100);
        }
        break;
      }
    }
  }

  async function onSubmit(values: Record<string, unknown>): Promise<void> {
    setServerError(null);
    setIsError(false);
    setIsSuccess(false);
    setIsPending(true);
    try {
      await submitForm(caseId, formDefinition.id, values);
      setIsSuccess(true);
      // CF1: delay onSuccess by 1200ms so user sees the MutationButton "confirmed" state
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
      const knownIds = new Set(allFields.map((f) => f.id));
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
        openFirstErrorSection();
      }
      if (unmapped.length > 0 || firstFieldName === null) {
        setServerError({
          title: t('cases.create.errorBanner.title'),
          message: unmapped.length > 0 ? unmapped.join(' · ') : t('cases.create.errorBanner.body'),
        });
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
  }

  // Collect all errors across all sections for FormErrorsBanner (AC2)
  function collectAllErrors(): FormErrorEntry[] {
    const errs = form.formState.errors;
    const out: FormErrorEntry[] = [];
    for (const f of allFields) {
      if (errs[f.id]) {
        out.push({ field: f.id, displayName: f.displayName, order: f.order });
      }
    }
    return out;
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
      retryAction={retryAction}
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
          onSubmit={form.handleSubmit(onSubmit, () => {
            // RHF calls this on validation failure; open first error section
            openFirstErrorSection();
          })}
          aria-busy={isPending ? true : undefined}
          className="flex flex-col gap-[var(--form-field-gap,1rem)]"
        >
          {/* AC2 — FormErrorsBanner with all field errors (same as SinglePageFormRenderer) */}
          {form.formState.isSubmitted ? (
            <FormErrorsBanner
              errors={collectAllErrors()}
              onAnchorClick={(field) => {
                // Find the section containing this field, expand it, then focus
                const ownerSection = sections.find((s) => s.fields.some((f) => f.id === field));
                if (ownerSection) {
                  setExpanded((prev) => ({ ...prev, [ownerSection.id]: true }));
                  setTimeout(() => form.setFocus(field), 50);
                } else {
                  form.setFocus(field);
                }
              }}
            />
          ) : null}

          {/* AC4 — Progress indicator */}
          {sections.length > 0 ? (
            <p className="text-sm text-[var(--muted-foreground)]">
              {t('form.sections.progress', {
                complete: String(completeCount),
                total: String(sections.length),
              })}
            </p>
          ) : null}

          {/* AC1 — Section panels */}
          {sections.map((section) => {
            const status = getSectionStatus(section);
            const isOpen = expanded[section.id] ?? true;
            const sortedFields = [...section.fields].sort((a, b) => a.order - b.order);
            return (
              <div
                key={section.id}
                id={`section-${section.id}`}
                className="rounded-md border border-[var(--border)]"
              >
                <button
                  type="button"
                  className="flex w-full items-center justify-between p-4 font-medium"
                  aria-expanded={isOpen}
                  onClick={() =>
                    setExpanded((prev) => ({ ...prev, [section.id]: !prev[section.id] }))
                  }
                >
                  <span>{section.label}</span>
                  <span className="flex items-center gap-2">
                    {statusIcon(status)}
                    <ChevronDown
                      className={`h-4 w-4 transition-transform ${isOpen ? 'rotate-180' : ''}`}
                    />
                  </span>
                </button>
                {isOpen ? (
                  <div className="flex flex-col gap-[var(--form-field-gap,1rem)] p-4 pt-0">
                    {sortedFields.length === 0 ? (
                      <p className="text-sm text-[var(--muted-foreground)]">
                        {t('cases.create.noRequired', { caseTypeName: section.id })}
                      </p>
                    ) : (
                      sortedFields.map((f) => renderField(f, isPending, form))
                    )}
                  </div>
                ) : null}
              </div>
            );
          })}

          {serverError ? (
            <Alert role="alert" variant="destructive" className="py-[var(--space-2)] text-sm">
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

/** AC1 — Section status icon */
function statusIcon(status: SectionStatus) {
  if (status === 'complete')
    return (
      <span aria-label="complete" className="text-green-600">
        {t('form.sections.status.complete')}
      </span>
    );
  if (status === 'error')
    return (
      <span aria-label="error" className="text-[var(--destructive)]">
        {t('form.sections.status.error')}
      </span>
    );
  return (
    <span aria-label="incomplete" className="text-[var(--muted-foreground)]">
      {t('form.sections.status.incomplete')}
    </span>
  );
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
