import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';

import { ApiError } from '@/api/client';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { useAuthStore } from '@/stores/authStore';

const schema = z.object({
  email: z.string().min(1, 'Email is required').email('Enter a valid email'),
  password: z.string().min(1, 'Password is required'),
});

type FormValues = z.infer<typeof schema>;

export function LoginPage() {
  const status = useAuthStore((s) => s.status);
  const login = useAuthStore((s) => s.login);
  const error = useAuthStore((s) => s.error);
  const navigate = useNavigate();
  const location = useLocation();
  const [submitError, setSubmitError] = useState<string | null>(null);

  const params = new URLSearchParams(location.search);
  const returnTo = params.get('returnTo') ?? '/cases';

  useEffect(() => {
    setSubmitError(null);
  }, [location.search]);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema), mode: 'onBlur' });

  if (status === 'authenticated') {
    return <Navigate to={returnTo} replace />;
  }

  const onSubmit = async (values: FormValues) => {
    setSubmitError(null);
    try {
      await login(values.email, values.password);
      navigate(returnTo, { replace: true });
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setSubmitError('Email or password is incorrect.');
      } else {
        setSubmitError('Sign-in failed. Please try again.');
      }
    }
  };

  return (
    <main className="min-h-screen grid lg:grid-cols-2 bg-background">
      {/* Left: visual */}
      <aside
        className="hidden lg:flex flex-col justify-between p-10 text-white"
        style={{
          background: 'linear-gradient(135deg, #0B1437 0%, #1e3a8a 50%, #3b5bdb 100%)',
        }}
      >
        <div className="flex items-center gap-2 font-heading text-lg font-semibold">
          <span
            className="grid size-7 place-items-center rounded-md text-white font-bold"
            style={{ background: 'rgba(255,255,255,0.15)' }}
          >
            W
          </span>
          WKS Platform
        </div>
        <div className="max-w-md">
          <h1 className="text-4xl font-heading font-semibold leading-tight">
            Case management,<br />engineered for SIs.
          </h1>
          <p className="mt-4 text-white/70 text-[14px] leading-relaxed">
            Configure case types in YAML, model workflows in BPMN, and ship branded portals to your customers in days, not months.
          </p>
        </div>
        <div className="text-[12px] text-white/50">v0.1 · OSS · Apache 2.0</div>
      </aside>

      {/* Right: form */}
      <section className="flex items-center justify-center p-6">
        <div className="w-full max-w-sm">
          <div className="mb-7 lg:hidden flex items-center gap-2 font-heading text-lg font-semibold">
            <span
              className="grid size-7 place-items-center rounded-md text-white font-bold"
              style={{ background: 'linear-gradient(135deg, var(--primary), var(--secondary))' }}
            >
              W
            </span>
            WKS Platform
          </div>
          <h2 className="font-heading text-2xl font-semibold">Welcome back</h2>
          <p className="text-foreground-muted text-[13px] mt-1">Sign in to your workspace</p>

          <form onSubmit={handleSubmit(onSubmit)} className="mt-7 space-y-3" noValidate>
            <div>
              <label htmlFor="email" className="block text-[12px] font-medium mb-1.5">
                Email
              </label>
              <Input
                id="email"
                type="email"
                autoComplete="email"
                autoFocus
                aria-invalid={!!errors.email}
                {...register('email')}
              />
              {errors.email && (
                <p className="mt-1 text-[12px] text-[var(--destructive)]">{errors.email.message}</p>
              )}
            </div>
            <div>
              <label htmlFor="password" className="block text-[12px] font-medium mb-1.5">
                Password
              </label>
              <Input
                id="password"
                type="password"
                autoComplete="current-password"
                aria-invalid={!!errors.password}
                {...register('password')}
              />
              {errors.password && (
                <p className="mt-1 text-[12px] text-[var(--destructive)]">{errors.password.message}</p>
              )}
            </div>

            {submitError && (
              <div
                role="alert"
                className="rounded-md border border-[var(--destructive)] bg-[var(--destructive-soft)] px-3 py-2 text-[12px] text-[var(--destructive)]"
              >
                {submitError}
              </div>
            )}
            {!submitError && error === 'unexpected' && (
              <div role="alert" className="text-[12px] text-[var(--destructive)]">
                Sign-in failed. Please try again.
              </div>
            )}

            <Button
              type="submit"
              variant="primary"
              size="lg"
              disabled={isSubmitting}
              className="w-full mt-1"
            >
              {isSubmitting ? 'Signing in…' : 'Sign in'}
            </Button>
          </form>

          <p className="mt-8 text-[11px] text-foreground-subtle text-center">
            By signing in you agree to the Acceptable Use Policy.
          </p>
        </div>
      </section>
    </main>
  );
}
