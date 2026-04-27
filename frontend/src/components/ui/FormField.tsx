import { useId, type ReactNode } from 'react';
import {
  useController,
  useFormContext,
  type ControllerRenderProps,
  type FieldPath,
  type FieldValues,
} from 'react-hook-form';

import { cn } from '@/lib/cn';

export type FormFieldRenderProps<
  TFieldValues extends FieldValues = FieldValues,
  TName extends FieldPath<TFieldValues> = FieldPath<TFieldValues>,
> = ControllerRenderProps<TFieldValues, TName> & {
  id: string;
  'aria-invalid': boolean | undefined;
  'aria-describedby': string | undefined;
  'aria-required': boolean | undefined;
  disabled?: boolean;
};

export type FormFieldProps<
  TFieldValues extends FieldValues = FieldValues,
  TName extends FieldPath<TFieldValues> = FieldPath<TFieldValues>,
> = {
  name: TName;
  label: string;
  description?: string;
  required?: boolean;
  /** When true, forwards `disabled` to the rendered input via the render-prop's `disabled`. */
  disabled?: boolean;
  className?: string;
  children: (field: FormFieldRenderProps<TFieldValues, TName>) => ReactNode;
};

/**
 * Story 2.7 — RHF + Zod field wrapper. Composes a Label + the consumer's input element + an
 * ErrorMessage. Wires `id` / `htmlFor` / `aria-invalid` / `aria-describedby` / `aria-required`
 * automatically from RHF state so individual input components don't need per-field plumbing.
 *
 * Closes 1.3 chunk-3 deferred-work entry: "Input lacks aria-describedby wiring to external error
 * messages — formalize in the FormField wrapper that ships with RHF + Zod in 2.7."
 *
 * Uses {@code useController} (per AC12) so controlled Radix primitives (Select, Checkbox) slot in
 * via the same wrapper as plain Input/Textarea — `field.value` + `field.onChange` work uniformly.
 * Also exposes `field.ref` so RHF's `shouldFocusError` can focus controlled controls.
 */
export function FormField<
  TFieldValues extends FieldValues = FieldValues,
  TName extends FieldPath<TFieldValues> = FieldPath<TFieldValues>,
>({
  name,
  label,
  description,
  required,
  disabled,
  className,
  children,
}: FormFieldProps<TFieldValues, TName>) {
  // Touch the form context so we throw a clear error when used outside <FormProvider> rather
  // than the cryptic useController error.
  useFormContext<TFieldValues>();
  const { field, fieldState } = useController<TFieldValues, TName>({ name });
  const reactId = useId();
  const fieldId = `${reactId}-${String(name)}`;
  const errorId = `${fieldId}-error`;
  const descId = description ? `${fieldId}-desc` : undefined;

  const errorMessage =
    typeof fieldState.error?.message === 'string' ? fieldState.error.message : undefined;

  const describedBy =
    [errorMessage ? errorId : null, descId].filter(Boolean).join(' ') || undefined;

  const renderProps: FormFieldRenderProps<TFieldValues, TName> = {
    ...field,
    // Default value to '' so plain Inputs are always controlled (RHF can hand back undefined).
    value: (field.value ?? '') as ControllerRenderProps<TFieldValues, TName>['value'],
    id: fieldId,
    'aria-invalid': errorMessage ? true : undefined,
    'aria-describedby': describedBy,
    'aria-required': required ? true : undefined,
    disabled,
  };

  return (
    <div className={cn('flex flex-col gap-1.5', className)}>
      <label htmlFor={fieldId} className="text-sm font-medium text-[var(--foreground)]">
        {label}
        {required && (
          <span className="ml-0.5 text-[var(--destructive)]" aria-hidden>
            {'*'}
          </span>
        )}
      </label>
      {children(renderProps)}
      {description && (
        <p id={descId} className="text-sm text-[var(--muted-foreground)]">
          {description}
        </p>
      )}
      {errorMessage && (
        <p id={errorId} role="alert" className="text-sm text-[var(--destructive)]">
          {errorMessage}
        </p>
      )}
    </div>
  );
}
