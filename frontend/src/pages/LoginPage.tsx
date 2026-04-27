import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { FormProvider, useForm } from 'react-hook-form';
import { useNavigate, useSearchParams } from 'react-router-dom';

import { ApiError } from '@/api/client';
import { Alert } from '@/components/ui/Alert';
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/Card';
import { FormField } from '@/components/ui/FormField';
import { Input } from '@/components/ui/Input';
import { MutationButton, type MutationButtonState } from '@/components/ui/MutationButton';
import { t } from '@/i18n';
import { loginSchema, type LoginValues } from '@/pages/login/loginSchema';
import { useAuthStore } from '@/stores/authStore';

export function safeReturnTo(value: string | null): string {
  if (!value) return '/cases';
  // Parse against the current origin; this normalizes every encoding and
  // backslash/whitespace edge case that ad-hoc `startsWith` checks miss.
  // Reject anything that resolves to a different origin, and bounce the
  // `/login` self-reference so we can't recursively redirect users back
  // to the login screen.
  let url: URL;
  try {
    url = new URL(value, window.location.origin);
  } catch {
    return '/cases';
  }
  if (url.origin !== window.location.origin) return '/cases';
  if (url.pathname === '/login') return '/cases';
  return url.pathname + url.search + url.hash;
}

/**
 * Story 2.7 AC11 — LoginPage retrofitted to RHF + Zod. The reference pattern the case-creation
 * dialog inherits. {@code MutationButton} drives the 4-state visual; the existing 1-3 auth
 * envelope wiring (401 → invalid copy, anything else → generic copy) is unchanged.
 */
export function LoginPage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const login = useAuthStore((s) => s.login);
  const [serverError, setServerError] = useState<{ message: string } | null>(null);

  const form = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    mode: 'onBlur',
    reValidateMode: 'onChange',
    shouldFocusError: true,
    defaultValues: { email: '', password: '' },
  });

  const state: MutationButtonState = form.formState.isSubmitting
    ? 'confirming'
    : form.formState.isSubmitSuccessful
      ? 'confirmed'
      : serverError
        ? 'failed'
        : 'idle';

  async function onSubmit(values: LoginValues): Promise<void> {
    setServerError(null);
    try {
      await login(values.email, values.password);
      navigate(safeReturnTo(params.get('returnTo')), { replace: true });
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setServerError({ message: t('login.error.invalid') });
      } else {
        setServerError({ message: t('login.error.generic') });
      }
    }
  }

  return (
    <Card className="w-full max-w-md bg-[var(--card)]">
      <CardHeader>
        <p className="font-heading text-2xl font-bold text-[var(--brand-navy)]">
          {t('app.brandName')}
        </p>
        <h1 className="mt-[var(--space-2)] text-lg font-medium text-[var(--foreground)]">
          {t('login.title')}
        </h1>
      </CardHeader>
      <FormProvider {...form}>
        <form
          onSubmit={form.handleSubmit(onSubmit)}
          autoComplete="on"
          noValidate
          aria-describedby={serverError ? 'login-error' : undefined}
        >
          <CardContent className="flex flex-col gap-[var(--space-4)]">
            <FormField<LoginValues> name="email" label={t('login.email')} required>
              {(field) => (
                <Input
                  type="email"
                  autoComplete="username"
                  disabled={state === 'confirming'}
                  {...field}
                />
              )}
            </FormField>
            <FormField<LoginValues> name="password" label={t('login.password')} required>
              {(field) => (
                <Input
                  type="password"
                  autoComplete="current-password"
                  disabled={state === 'confirming'}
                  {...field}
                />
              )}
            </FormField>
            {serverError ? (
              <Alert id="login-error" variant="destructive" className="py-[var(--space-2)] text-xs">
                {serverError.message}
              </Alert>
            ) : null}
          </CardContent>
          <CardFooter>
            <MutationButton
              state={state}
              className="w-full"
              confirmingLabel={t('login.submitting')}
              confirmedLabel={t('login.submit')}
              failedLabel={t('common.lifecycle.failed')}
            >
              {t('login.submit')}
            </MutationButton>
          </CardFooter>
        </form>
      </FormProvider>
    </Card>
  );
}
