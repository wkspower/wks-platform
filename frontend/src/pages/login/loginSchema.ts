import { z } from 'zod';

import { t } from '@/i18n';

/**
 * Story 2.7 AC11 — RHF + Zod schema for the LoginPage. This is the **reference pattern** the
 * case-creation surface inherits. The retrofit closes the 1-3 deferred-work entry: "LoginPage
 * uses native HTML validation; no React Hook Form / Zod in 1.3".
 *
 * Locale is module-frozen at build time (1-3 chunk-2 deferred-work) — `t()` is called once at
 * schema construction. When dynamic locale switching lands, this schema becomes a function.
 */
export const loginSchema = z.object({
  email: z.string().email(t('login.errors.notEmail')),
  password: z.string().min(1, t('login.errors.passwordRequired')),
});

export type LoginValues = z.infer<typeof loginSchema>;
