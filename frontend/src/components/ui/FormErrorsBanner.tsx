import { t } from '@/i18n';

/**
 * Story 2.8 AC9 — generic multi-field errors-count banner with anchor links. Shipped as a reusable
 * component (owned-source) so future task forms / document upload metadata / config deploy errors
 * can wire it without re-implementing the count copy + focus management.
 *
 * Renders nothing when {@code errors.length === 0}. Single ARIA-live region with {@code
 * aria-live="polite"}; per-field inline errors continue to render alongside this banner — the
 * banner is the orientation device for screen readers + keyboard users, the per-field error is
 * the spatial cue for sighted users (Story 2.8 §AC9).
 */
export interface FormErrorEntry {
  field: string;
  displayName: string;
  order: number;
}

export interface FormErrorsBannerProps {
  errors: FormErrorEntry[];
  /**
   * Click handler for the per-field anchor link — the parent typically routes this through
   * RHF's {@code form.setFocus(name)} so controlled Radix primitives (Select, Checkbox) focus
   * correctly via {@code field.ref}.
   */
  onAnchorClick: (field: string) => void;
}

export function FormErrorsBanner({ errors, onAnchorClick }: FormErrorsBannerProps) {
  if (errors.length === 0) return null;
  const sorted = [...errors].sort((a, b) => a.order - b.order);
  const count = sorted.length;
  // Manual ICU plural — the i18n engine does simple `{name}` substitution; keep the singular and
  // plural copies as separate keys (single key would either drop the count or read awkwardly).
  const heading =
    count === 1
      ? t('cases.create.errorsCount.one')
      : t('cases.create.errorsCount', { count: String(count) });

  return (
    <div
      role="alert"
      aria-live="polite"
      className="rounded-[var(--radius-md)] border border-[var(--destructive)] bg-[var(--destructive)]/10 px-3 py-2 text-sm"
    >
      <p className="font-medium text-[var(--destructive)]">{heading}</p>
      <ul className="mt-1 flex flex-wrap gap-x-3 gap-y-1">
        {sorted.map((e) => (
          <li key={e.field}>
            <button
              type="button"
              className="text-[var(--primary)] underline-offset-2 hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ring)]"
              onClick={() => onAnchorClick(e.field)}
            >
              {e.displayName}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
