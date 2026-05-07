import { z } from 'zod';

import { t } from '@/i18n';
import type { FieldDefinition } from '@/types/caseType';

/**
 * Story 2.7 AC4 — runtime Zod schema builder for the create-case form. Pure function: takes the
 * case-type's `fields[]` (post-filter to `requiredOnCreate: true` for the create form) and returns
 * a `z.ZodObject` whose property schemas mirror per-type slots.
 *
 * Architecture note: the runtime build (NOT a build-time codegen) is the contract from
 * architecture.md §Decision 12 — schemas reflect the most recently deployed YAML, with no
 * recompile.
 *
 * Specific error messages per slot (epic AC4) — no generic "Invalid input". Strings come from
 * `i18n/en.json` keyed by error variant.
 */
export type BuildMode = 'create' | 'edit' | 'submit';

export function buildZodFromFieldDefs(
  fields: FieldDefinition[],
  mode: BuildMode = 'create',
): z.ZodObject<z.ZodRawShape> {
  const shape: z.ZodRawShape = {};
  // 'create' — only fields with requiredOnCreate (create-case dialog gate).
  // 'submit' — only fields with required: true (all required fields, not filtered by
  //            requiredOnCreate). This is the key distinction for SinglePageFormRenderer.
  // 'edit'   — all fields (no filter); existing edit-case behaviour is unchanged.
  const filtered =
    mode === 'create'
      ? fields.filter((f) => f.requiredOnCreate)
      : mode === 'submit'
        ? fields.filter((f) => f.required)
        : fields;
  for (const f of filtered) {
    shape[f.id] = schemaForField(f, mode);
  }
  return z.object(shape);
}

function schemaForField(f: FieldDefinition, mode: BuildMode): z.ZodTypeAny {
  switch (f.type) {
    case 'text':
    case 'textarea':
      return textSchema(f);
    case 'number':
      return numberSchema(f);
    case 'date':
      return dateSchema(f);
    case 'select':
      return selectSchema(f);
    case 'checkbox':
      return checkboxSchema(f, mode);
    case 'file':
      // AC3 — `file` is treated as optional on the frontend until Story 3.1 ships upload.
      return z.unknown().optional();
    default:
      return z.unknown();
  }
}

function textSchema(f: FieldDefinition): z.ZodTypeAny {
  const minLen = typeof f.minLength === 'number' && f.minLength > 0 ? f.minLength : 1;
  let s = z.string({ required_error: t('cases.create.errors.required') }).min(minLen, {
    message:
      minLen > 1
        ? t('cases.create.errors.tooShort', { min: String(minLen) })
        : t('cases.create.errors.required'),
  });
  if (typeof f.maxLength === 'number') {
    s = s.max(f.maxLength, {
      message: t('cases.create.errors.tooLong', { max: String(f.maxLength) }),
    });
  }
  return s;
}

function numberSchema(f: FieldDefinition): z.ZodTypeAny {
  // P3 — `z.coerce.number()` traps: empty-string coerces to 0 (silent data corruption); "abc"
  // coerces to NaN that JSON.stringify converts to null. Preprocess empty-ish inputs to undefined
  // (so `required_error` fires), then guard against NaN explicitly.
  const base = z.preprocess(
    (v) => {
      if (v === '' || v === null || v === undefined) return undefined;
      if (typeof v === 'string') {
        const trimmed = v.trim();
        if (trimmed === '') return undefined;
        const n = Number(trimmed);
        return Number.isFinite(n) ? n : NaN;
      }
      return v;
    },
    z
      .number({
        invalid_type_error: t('cases.create.errors.notNumber'),
        required_error: t('cases.create.errors.required'),
      })
      .refine((n) => Number.isFinite(n), { message: t('cases.create.errors.notNumber') }),
  );

  let s: z.ZodTypeAny = base;
  if (typeof f.min === 'number') {
    s = s.refine((n) => typeof n !== 'number' || n >= f.min!, {
      message: t('cases.create.errors.lt', { min: String(f.min) }),
    });
  }
  if (typeof f.max === 'number') {
    s = s.refine((n) => typeof n !== 'number' || n <= f.max!, {
      message: t('cases.create.errors.gt', { max: String(f.max) }),
    });
  }
  if (typeof f.step === 'number' && f.step > 0) {
    // P17 — multipleOf: tolerate floating-point dust by checking against a small epsilon scaled
    // to the step (covers e.g. 0.1+0.2 ≈ 0.3 drift).
    const step = f.step;
    s = s.refine(
      (n) => {
        if (typeof n !== 'number' || !Number.isFinite(n)) return true;
        const ratio = n / step;
        return Math.abs(ratio - Math.round(ratio)) < 1e-9;
      },
      { message: t('cases.create.errors.notMultipleOf', { step: String(step) }) },
    );
  }
  return s;
}

function dateSchema(f: FieldDefinition): z.ZodTypeAny {
  // P18 — `dateMin`/`dateMax` from YAML may be non-zero-padded (e.g. "2024-2-3"). Lexicographic
  // compare is unsafe in that case — normalise to YYYY-MM-DD before compare.
  const normalize = (raw: string | null | undefined): string | null => {
    if (!raw) return null;
    const m = /^(\d{4})-(\d{1,2})-(\d{1,2})$/.exec(raw.trim());
    if (!m) return null;
    return `${m[1]}-${m[2]!.padStart(2, '0')}-${m[3]!.padStart(2, '0')}`;
  };
  const min = normalize(f.dateMin ?? null);
  const max = normalize(f.dateMax ?? null);
  const iso = z
    .string({ required_error: t('cases.create.errors.required') })
    .regex(/^\d{4}-\d{2}-\d{2}$/, { message: t('cases.create.errors.notDate') });
  if (min || max) {
    return iso.refine((v) => (!min || v >= min) && (!max || v <= max), {
      message: t('cases.create.errors.dateRange', {
        min: min ?? '',
        max: max ?? '',
      }),
    });
  }
  return iso;
}

function selectSchema(f: FieldDefinition): z.ZodTypeAny {
  const values = f.options.map((o) => o.value);
  if (values.length === 0) {
    // ConfigValidator already blocks required+select with 0 options at YAML load — defensive
    // fallback only.
    return z
      .string({ required_error: t('cases.create.errors.required') })
      .min(1, { message: t('cases.create.errors.required') });
  }
  // P13 — disambiguate "user hasn't picked anything" (required) from "user picked an off-list
  // value" (notInList) so the inline message matches what the user actually did wrong.
  return z.enum(values as [string, ...string[]], {
    errorMap: (issue, ctx) => {
      if (issue.code === 'invalid_type' || ctx.data === undefined || ctx.data === '') {
        return { message: t('cases.create.errors.required') };
      }
      return { message: t('cases.create.errors.notInList') };
    },
  });
}

function checkboxSchema(f: FieldDefinition, mode: BuildMode): z.ZodTypeAny {
  // P12 — in 'edit' mode the `requiredOnCreate` flag is the wrong axis: it's a create-time
  // gate, not an edit-time one. Use `f.required` for non-create modes so editing still enforces
  // any always-required boolean (e.g. an "active" flag).
  const isRequired = mode === 'create' ? f.requiredOnCreate : f.required;
  if (isRequired) {
    return z
      .boolean({ required_error: t('cases.create.errors.mustCheck') })
      .refine((v) => v === true, { message: t('cases.create.errors.mustCheck') });
  }
  return z.boolean().optional();
}
