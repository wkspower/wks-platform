import { useMemo } from 'react';

import { useAuthStore } from '@/stores/authStore';

/**
 * Story 5.6 AC3 — Returns the current user's role set as a {@link ReadonlySet}. Convenience
 * wrapper over {@link useAuthStore} that memoises the {@code Set} so consumers can use referential
 * equality in {@code useMemo} dependency arrays.
 *
 * <p>Returns an empty set when the user is not authenticated (the renderers should not render at
 * all in that state, but the hook stays defensive).
 */
export function useUserRoles(): ReadonlySet<string> {
  const roles = useAuthStore((s) => s.user?.roles);
  return useMemo(() => new Set(roles ?? []), [roles]);
}
