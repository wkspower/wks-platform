/**
 * Story 6.1 — Closed archetype catalog and data-driven affordance registry.
 *
 * The catalog is a closed set at Phase-0. Adding a fourth archetype in Phase-1 is a
 * one-line change in the REGISTRY literal (add the id to ArchetypeId union + one entry
 * in REGISTRY). No renderer component needs to change — the data-driven contract ensures
 * zero archetype literals escape into `components/`.
 *
 * AC2 enforcement: `grep -rn "draft_section\|submit_for_processing\|business_final"
 * frontend/src/components` must return matches ONLY in test files.
 */

/** Phase-0 archetype identifiers — matches the backend closed catalog. */
export type ArchetypeId = 'draft_section' | 'submit_for_processing' | 'business_final';

/** Ordered list for iteration / type guard use. */
export const ARCHETYPE_IDS: readonly ArchetypeId[] = Object.freeze([
  'draft_section',
  'submit_for_processing',
  'business_final',
] as const);

/**
 * Per-archetype affordance contract. Renderers read ONLY this record — they never branch
 * on archetype string literals themselves.
 */
export interface ArchetypeAffordance {
  /** i18n key for the primary CTA label. */
  ctaLabelKey: string;
  /** Visual tone of the primary CTA button. */
  ctaTone: 'primary' | 'secondary';
  /**
   * Confirmation interaction pattern before the actual mutation fires.
   * - `inline`: existing confirming → confirmed lifecycle (no extra UI).
   * - `processing-modal`: confirming → processing → confirmed (spinner visible).
   * - `confirmation-dialog`: AlertDialog interposes between click and mutation.
   */
  confirmationFlow: 'inline' | 'processing-modal' | 'confirmation-dialog';
  /**
   * Post-completion state of the surrounding container.
   * - `idle`: button resets to idle (re-editable, e.g. draft_section).
   * - `locked`: button stays disabled + terminal-accent badge applied.
   * - `terminal-accent`: same as locked (alias kept for CSS-hook clarity).
   */
  postActionState: 'idle' | 'locked' | 'terminal-accent';
}

/**
 * Default affordance — preserves pre-6.1 behavior for tasks/forms/stages with no archetype
 * declared. Matches the existing `MutationButton` idle → confirming → confirmed flow.
 */
const DEFAULT_AFFORDANCE: ArchetypeAffordance = Object.freeze({
  ctaLabelKey: 'task.cta.complete',
  ctaTone: 'primary',
  confirmationFlow: 'inline',
  postActionState: 'idle',
});

/** Frozen registry — reading by id is O(1); adding a new archetype is one line here. */
const REGISTRY: Readonly<Record<ArchetypeId, ArchetypeAffordance>> = Object.freeze({
  draft_section: Object.freeze({
    ctaLabelKey: 'task.cta.draft_section.complete',
    ctaTone: 'secondary',
    confirmationFlow: 'inline',
    postActionState: 'idle',
  } satisfies ArchetypeAffordance),

  submit_for_processing: Object.freeze({
    ctaLabelKey: 'task.cta.submit_for_processing.complete',
    ctaTone: 'primary',
    confirmationFlow: 'processing-modal',
    postActionState: 'idle',
  } satisfies ArchetypeAffordance),

  business_final: Object.freeze({
    ctaLabelKey: 'task.cta.business_final.complete',
    ctaTone: 'primary',
    confirmationFlow: 'confirmation-dialog',
    postActionState: 'locked',
  } satisfies ArchetypeAffordance),
});

/**
 * Look up the affordance for an archetype id.
 *
 * @param id - archetype string from the backend wire shape (nullable — omitted means default).
 * @returns The matching {@link ArchetypeAffordance}, or the default when `id` is null, undefined,
 *   or unrecognised (defensive: unknown values fall back rather than throw, matching the backend's
 *   optional posture).
 */
export function getArchetypeAffordance(id: string | null | undefined): ArchetypeAffordance {
  if (!id) return DEFAULT_AFFORDANCE;
  if ((ARCHETYPE_IDS as readonly string[]).includes(id)) {
    return REGISTRY[id as ArchetypeId];
  }
  return DEFAULT_AFFORDANCE;
}

/** Expose the default for test assertions. */
export { DEFAULT_AFFORDANCE };
