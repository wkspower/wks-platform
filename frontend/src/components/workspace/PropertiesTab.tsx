import { ListChecks, Pencil } from 'lucide-react';
import { Fragment, useState, type KeyboardEvent } from 'react';

import { ApiError } from '@/api/client';
import { Button } from '@/components/ui/Button';
import { Checkbox } from '@/components/ui/Checkbox';
import { EmptyState } from '@/components/ui/EmptyState';
import { Input } from '@/components/ui/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/Select';
import { Textarea } from '@/components/ui/Textarea';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/Tooltip';
import { useUpdateCase } from '@/hooks/useCases';
import { t } from '@/i18n';
import { buildZodFromFieldDefs } from '@/lib/buildZodFromFieldDefs';
import { renderFieldValue } from '@/lib/renderFieldValue';
import type { CaseDto } from '@/types/case';
import type { CaseTypeView, FieldDefinition } from '@/types/caseType';

export interface PropertiesTabProps {
  caseDto: CaseDto;
  caseTypeView: CaseTypeView;
}

export function PropertiesTab({ caseDto, caseTypeView }: PropertiesTabProps) {
  // Defensive: types declare `caseTypeView.fields` and `caseDto.data` as required, but a
  // legacy/malformed backend response (or an orphaned case-type version) could leave either
  // missing. Render the empty-state copy instead of letting a TypeError reach the boundary.
  const fields = caseTypeView?.fields ?? [];
  const data = caseDto?.data ?? {};
  const [editingId, setEditingId] = useState<string | null>(null);

  if (fields.length === 0) {
    return <EmptyState icon={ListChecks} headline={t('properties.empty')} />;
  }

  return (
    <dl className="grid grid-cols-[120px_1fr] gap-x-3 gap-y-2 px-1 py-2">
      {fields.map((field) => (
        <Fragment key={field.id}>
          {/*
            Tooltip wraps the label so the full displayName is reachable when the 120px column
            ellipses. `tabIndex` intentionally omitted: adding a tab-stop per field would create
            an N-step tab chain through the panel for keyboard users — Radix Tooltip exposes the
            content on hover/focus of any focusable descendant, but the `<dt>` itself has no
            interactive purpose, so we keep it out of the tab order.
          */}
          <Tooltip>
            <TooltipTrigger asChild>
              <dt className="truncate text-sm text-[var(--muted-foreground)]">
                {field.displayName}
              </dt>
            </TooltipTrigger>
            <TooltipContent>{field.displayName}</TooltipContent>
          </Tooltip>
          <dd className="text-sm">
            {editingId === field.id ? (
              <InlineEditor
                field={field}
                initial={data[field.id]}
                caseId={caseDto.id}
                caseVersion={caseDto.version}
                caseData={data}
                onDone={() => setEditingId(null)}
              />
            ) : (
              <ReadRow field={field} value={data[field.id]} onEdit={() => setEditingId(field.id)} />
            )}
          </dd>
        </Fragment>
      ))}
    </dl>
  );
}

function ReadRow({
  field,
  value,
  onEdit,
}: {
  field: FieldDefinition;
  value: unknown;
  onEdit: () => void;
}) {
  // File fields stay read-only — they round-trip through the Documents tab, not inline edit.
  const editable = field.type !== 'file';
  // Whole row is clickable when editable — that's the largest hit-target and removes the need
  // to find a tiny icon. The pencil stays as a visual affordance (always visible at low
  // contrast, full contrast on hover/focus) so users learn the row is editable.
  if (!editable) {
    return (
      <div className="flex items-center gap-2">
        <span className="flex-1">{renderFieldValue(field, value)}</span>
      </div>
    );
  }
  return (
    <button
      type="button"
      onClick={onEdit}
      aria-label={t('properties.edit.aria', { field: field.displayName })}
      className="group flex w-full items-center gap-2 rounded-[var(--radius-sm)] px-1 py-0.5 text-left hover:bg-[var(--muted)] focus-visible:bg-[var(--muted)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ring)]"
    >
      <span className="flex-1">{renderFieldValue(field, value)}</span>
      <Pencil
        aria-hidden
        className="size-3.5 text-[var(--muted-foreground)] opacity-40 transition-opacity group-hover:opacity-100 group-focus-visible:opacity-100"
      />
    </button>
  );
}

function InlineEditor({
  field,
  initial,
  caseId,
  caseVersion,
  caseData,
  onDone,
}: {
  field: FieldDefinition;
  initial: unknown;
  caseId: string;
  caseVersion: number;
  caseData: Record<string, unknown>;
  onDone: () => void;
}) {
  const [draft, setDraft] = useState<unknown>(initial);
  const [clientError, setClientError] = useState<string | null>(null);
  const mutation = useUpdateCase(caseId);

  // Server errors take precedence — they reflect what the wire actually saw. Client errors only
  // fill in when the user has not yet attempted a save (or fixed it and retried) but the new
  // draft still fails Zod.
  const errorMessage = mutation.isError ? toErrorMessage(mutation.error) : clientError;

  function save() {
    // Client-side Zod check on the single touched field — mirrors NewCaseDialog's pre-POST gate
    // so EMAIL (and future-typed) bad input is rejected without a server round-trip. Server-side
    // enforcement (CaseDataValidatorAdapter EMAIL post-check) is the source of truth; this is UX
    // symmetry with create.
    const schema = buildZodFromFieldDefs([field], 'edit');
    const parsed = schema.safeParse({ [field.id]: draft });
    if (!parsed.success) {
      const issue =
        parsed.error.issues.find((i) => i.path[0] === field.id) ?? parsed.error.issues[0];
      setClientError(issue?.message ?? t('properties.error.generic'));
      return;
    }
    setClientError(null);
    const payload = { ...caseData, [field.id]: draft };
    mutation.mutate({ data: payload, version: caseVersion }, { onSuccess: onDone });
  }

  function onKeyDown(e: KeyboardEvent<HTMLElement>) {
    if (e.key === 'Escape') {
      e.preventDefault();
      onDone();
      return;
    }
    // Enter saves on single-line inputs; in textarea Enter inserts a newline (Save button only).
    if (e.key === 'Enter' && field.type !== 'textarea') {
      e.preventDefault();
      save();
    }
  }

  const disabled = mutation.isPending;

  return (
    <div className="flex flex-col gap-1" onKeyDown={onKeyDown}>
      <div className="flex items-center gap-2">
        <FieldInput
          field={field}
          value={draft}
          onChange={setDraft}
          disabled={disabled}
          hasError={Boolean(errorMessage)}
        />
        <Button type="button" size="sm" onClick={save} disabled={disabled}>
          {t('properties.save')}
        </Button>
        <Button type="button" size="sm" variant="outline" onClick={onDone} disabled={disabled}>
          {t('properties.cancel')}
        </Button>
      </div>
      {errorMessage && (
        <p className="text-xs text-[var(--destructive)]" role="alert">
          {errorMessage}
        </p>
      )}
    </div>
  );
}

function toErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.status === 409) return t('properties.error.conflict');
    if (error.status === 403) return t('properties.error.forbidden');
    // 422 validation: backend message is human-readable and field-scoped — surface verbatim.
    if (error.status === 422 && error.message) return error.message;
  }
  return t('properties.error.generic');
}

function FieldInput({
  field,
  value,
  onChange,
  disabled,
  hasError,
}: {
  field: FieldDefinition;
  value: unknown;
  onChange: (v: unknown) => void;
  disabled: boolean;
  hasError: boolean;
}) {
  const ariaLabel = field.displayName;
  switch (field.type) {
    case 'text':
      return (
        <Input
          type="text"
          aria-label={ariaLabel}
          autoFocus
          disabled={disabled}
          hasError={hasError}
          maxLength={field.maxLength ?? undefined}
          value={value == null ? '' : String(value)}
          onChange={(e) => onChange(e.target.value)}
        />
      );
    case 'email':
      return (
        <Input
          type="email"
          aria-label={ariaLabel}
          autoFocus
          disabled={disabled}
          hasError={hasError}
          inputMode="email"
          autoComplete="email"
          maxLength={field.maxLength ?? undefined}
          value={value == null ? '' : String(value)}
          onChange={(e) => onChange(e.target.value)}
        />
      );
    case 'textarea':
      return (
        <Textarea
          aria-label={ariaLabel}
          autoFocus
          disabled={disabled}
          hasError={hasError}
          maxLength={field.maxLength ?? undefined}
          value={value == null ? '' : String(value)}
          onChange={(e) => onChange(e.target.value)}
        />
      );
    case 'number':
      return (
        <Input
          type="number"
          aria-label={ariaLabel}
          autoFocus
          disabled={disabled}
          hasError={hasError}
          inputMode="numeric"
          min={field.min ?? undefined}
          max={field.max ?? undefined}
          step={field.step ?? 1}
          value={value == null || value === '' ? '' : String(value)}
          onChange={(e) => {
            const raw = e.target.value;
            // Empty input clears the field (NULL on the wire); otherwise send a number so the
            // backend's number-type validator accepts it (string→number rejected by validator).
            onChange(raw === '' ? null : Number(raw));
          }}
        />
      );
    case 'date':
      return (
        <Input
          type="date"
          aria-label={ariaLabel}
          autoFocus
          disabled={disabled}
          hasError={hasError}
          min={field.dateMin ?? undefined}
          max={field.dateMax ?? undefined}
          value={value == null ? '' : String(value)}
          onChange={(e) => onChange(e.target.value || null)}
        />
      );
    case 'select': {
      const stringValue = typeof value === 'string' && value !== '' ? value : undefined;
      return (
        <Select value={stringValue} onValueChange={(v) => onChange(v)} disabled={disabled}>
          <SelectTrigger aria-label={ariaLabel} hasError={hasError}>
            <SelectValue placeholder={t('properties.select.placeholder')} />
          </SelectTrigger>
          <SelectContent>
            {field.options.map((opt) => (
              <SelectItem key={opt.value} value={opt.value}>
                {opt.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      );
    }
    case 'checkbox':
      return (
        <Checkbox
          aria-label={ariaLabel}
          autoFocus
          disabled={disabled}
          hasError={hasError}
          checked={Boolean(value)}
          onCheckedChange={(v) => onChange(v === true)}
        />
      );
    case 'file':
    default:
      // File round-trips through the Documents tab; should never reach the editor (ReadRow hides
      // the pencil), but render a no-op span so an unexpected type-widening doesn't crash.
      return (
        <span className="text-[var(--muted-foreground)]">{t('properties.file.placeholder')}</span>
      );
  }
}
