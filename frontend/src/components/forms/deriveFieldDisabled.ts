import type { CaseTypeView, FieldDefinition } from '@/types/caseType';

/**
 * Story 5.6 AC3 / AC4 — Derive whether a field should render as disabled given the field's
 * {@code editableBy} declaration and the current user's role set, optionally consulting the
 * case-type's {@code defaultFieldEditability} setting (AC4) when {@code editableBy} is omitted.
 *
 * <p>The server is authoritative for the actual permission check (AC2 — {@code WKS-AUTHZ-001} on
 * submit-time bypass). This helper is purely a UX hint to surface the restriction before the user
 * attempts a write.
 *
 * <p>Returns {@code disabled: false, tooltip: null} when the field is editable; otherwise
 * {@code disabled: true} with a human-readable tooltip describing the restriction. The tooltip
 * looks up role display names via {@code caseType.roles} when supplied, falling back to the role
 * id when the case-type does not surface its role declarations on the wire.
 */
export interface DerivedDisabled {
  disabled: boolean;
  tooltip: string | null;
}

export function deriveFieldDisabled(
  field: Pick<FieldDefinition, 'editableBy'>,
  caseType: Pick<CaseTypeView, 'defaultFieldEditability' | 'roles'>,
  userRoles: ReadonlySet<string>,
): DerivedDisabled {
  const editableBy = field.editableBy ?? [];
  if (editableBy.length === 0) {
    if (caseType.defaultFieldEditability === 'locked-by-default') {
      return {
        disabled: true,
        tooltip: 'Field is locked — no editableBy declaration',
      };
    }
    return { disabled: false, tooltip: null };
  }

  // editableBy entries follow `role:<id>` per AC1. Strip prefix; ignore unknown formats.
  const requiredRoles = editableBy
    .filter((s) => s.startsWith('role:'))
    .map((s) => s.substring('role:'.length))
    .filter((s) => s.length > 0);

  const hasRequired = requiredRoles.some((r) => userRoles.has(r));
  if (hasRequired) return { disabled: false, tooltip: null };

  // Build display-name tooltip from caseType.roles, falling back to role id.
  const roleNames = requiredRoles.map((rid) => {
    const r = caseType.roles?.find((x) => x.id === rid);
    return r?.displayName ?? rid;
  });
  const tooltip =
    roleNames.length > 0
      ? `Editable only by: ${roleNames.join(', ')}`
      : 'Editable only by a restricted role set';
  return { disabled: true, tooltip };
}
