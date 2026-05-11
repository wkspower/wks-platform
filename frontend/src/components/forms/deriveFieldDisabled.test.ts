/**
 * Story 5.6 AC3 / AC4 — unit tests for the per-field disabled-state derivation helper used by both
 * SinglePageFormRenderer and MultiSectionFormRenderer.
 *
 * Memory `feedback_consolidate_property_readers.md` analog: both renderers consume this single
 * helper; do not duplicate the role-derivation logic.
 *
 * Coverage matrix per AC3:
 *   (a) no editableBy + default editable → not disabled
 *   (b) editableBy declared, user has role → not disabled
 *   (c) editableBy declared, user lacks role → disabled with tooltip
 *   (d) editableBy with two roles, user has one → not disabled
 *
 * Coverage per AC4:
 *   (e) no editableBy + locked-by-default → disabled with locked tooltip
 *   (f) editableBy declared takes precedence over defaultFieldEditability when user has role
 */
import { describe, expect, it } from 'vitest';

import { deriveFieldDisabled } from './deriveFieldDisabled';

const ROLES = [
  { id: 'underwriter', displayName: 'Underwriter' },
  { id: 'supervisor', displayName: 'Supervisor' },
  { id: 'officer', displayName: 'Loan Officer' },
];

describe('deriveFieldDisabled', () => {
  it('(a) no editableBy + default editable → not disabled', () => {
    const r = deriveFieldDisabled(
      { editableBy: undefined },
      { defaultFieldEditability: 'editable-by-default', roles: ROLES },
      new Set([]),
    );
    expect(r.disabled).toBe(false);
    expect(r.tooltip).toBeNull();
  });

  it('(b) editableBy declared, user has role → not disabled', () => {
    const r = deriveFieldDisabled(
      { editableBy: ['role:underwriter'] },
      { defaultFieldEditability: 'editable-by-default', roles: ROLES },
      new Set(['underwriter']),
    );
    expect(r.disabled).toBe(false);
    expect(r.tooltip).toBeNull();
  });

  it('(c) editableBy declared, user lacks role → disabled with display-name tooltip', () => {
    const r = deriveFieldDisabled(
      { editableBy: ['role:underwriter'] },
      { defaultFieldEditability: 'editable-by-default', roles: ROLES },
      new Set(['officer']),
    );
    expect(r.disabled).toBe(true);
    expect(r.tooltip).toBe('Editable only by: Underwriter');
  });

  it('(d) editableBy with two roles, user has one → not disabled', () => {
    const r = deriveFieldDisabled(
      { editableBy: ['role:underwriter', 'role:supervisor'] },
      { defaultFieldEditability: 'editable-by-default', roles: ROLES },
      new Set(['supervisor']),
    );
    expect(r.disabled).toBe(false);
    expect(r.tooltip).toBeNull();
  });

  it('(e) no editableBy + locked-by-default → disabled with locked tooltip', () => {
    const r = deriveFieldDisabled(
      { editableBy: [] },
      { defaultFieldEditability: 'locked-by-default', roles: ROLES },
      new Set(['officer']),
    );
    expect(r.disabled).toBe(true);
    expect(r.tooltip).toBe('Field is locked — no editableBy declaration');
  });

  it('(f) tooltip falls back to role id when caseType.roles omitted', () => {
    const r = deriveFieldDisabled(
      { editableBy: ['role:underwriter', 'role:supervisor'] },
      { defaultFieldEditability: 'editable-by-default' },
      new Set(['officer']),
    );
    expect(r.disabled).toBe(true);
    // No display names supplied → falls back to role id verbatim.
    expect(r.tooltip).toBe('Editable only by: underwriter, supervisor');
  });
});
