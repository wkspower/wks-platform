import { type FormEvent, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

import { ApiError } from '@/api/client';
import { Alert } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/Card';
import { Input } from '@/components/ui/Input';
import { t } from '@/i18n';
import { useAuthStore } from '@/stores/authStore';

function safeReturnTo(value: string | null): string {
  if (!value) return '/cases';
  if (!value.startsWith('/') || value.startsWith('//')) return '/cases';
  return value;
}

export function LoginPage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const login = useAuthStore((s) => s.login);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  async function handleSubmit(e: FormEvent<HTMLFormElement>): Promise<void> {
    e.preventDefault();
    if (submitting) return;
    setSubmitting(true);
    setErrorMessage(null);
    try {
      await login(email, password);
      navigate(safeReturnTo(params.get('returnTo')), { replace: true });
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setErrorMessage(t('login.error.invalid'));
      } else {
        setErrorMessage(t('login.error.generic'));
      }
    } finally {
      setSubmitting(false);
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
      <form onSubmit={handleSubmit} aria-describedby={errorMessage ? 'login-error' : undefined}>
        <CardContent className="flex flex-col gap-[var(--space-4)]">
          <label className="flex flex-col gap-[var(--space-2)] text-sm">
            <span>{t('login.email')}</span>
            <Input
              type="email"
              autoComplete="username"
              required
              disabled={submitting}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </label>
          <label className="flex flex-col gap-[var(--space-2)] text-sm">
            <span>{t('login.password')}</span>
            <Input
              type="password"
              autoComplete="current-password"
              required
              disabled={submitting}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </label>
          {errorMessage ? (
            <Alert
              id="login-error"
              variant="destructive"
              className="py-[var(--space-2)] text-xs"
            >
              {errorMessage}
            </Alert>
          ) : null}
        </CardContent>
        <CardFooter>
          <Button type="submit" disabled={submitting} className="w-full">
            {submitting ? t('login.submitting') : t('login.submit')}
          </Button>
        </CardFooter>
      </form>
    </Card>
  );
}
