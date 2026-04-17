import { Component, type ErrorInfo, type ReactNode } from 'react';

import { Button } from '@/components/ui/Button';
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/Card';
import { t } from '@/i18n';
import { useAuthStore } from '@/stores/authStore';

interface Props {
  children: ReactNode;
}

interface State {
  error: Error | null;
}

function shouldShowDetails(): boolean {
  if (import.meta.env.DEV) return true;
  if (typeof window !== 'undefined' && window.location.search.includes('debug=true')) return true;
  // Class component cannot use hooks; read Zustand state directly.
  const user = useAuthStore.getState().user;
  return Boolean(user?.roles.includes('developer'));
}

export class RouteErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo): void {
    // eslint-disable-next-line no-console
    console.error('[RouteErrorBoundary]', error, info.componentStack);
  }

  reload = (): void => {
    window.location.reload();
  };

  render() {
    const { error } = this.state;
    if (!error) return this.props.children;
    return (
      <div className="flex min-h-full items-start justify-center p-[var(--space-8)]">
        <Card className="w-full max-w-2xl">
          <CardHeader>
            <h2 className="font-heading text-xl font-semibold">{t('error.title')}</h2>
          </CardHeader>
          <CardContent>
            <p className="text-[var(--muted-foreground)]">{t('error.savedServerSide')}</p>
            {shouldShowDetails() ? (
              <details className="mt-[var(--space-4)] text-xs">
                <summary className="cursor-pointer text-[var(--muted-foreground)]">
                  {t('error.details')}
                </summary>
                <pre className="mt-[var(--space-2)] overflow-auto rounded-[var(--radius-md)] bg-[var(--muted)] p-[var(--space-3)] text-[var(--foreground)]">
                  {error.stack ?? error.message}
                </pre>
              </details>
            ) : null}
          </CardContent>
          <CardFooter>
            <Button onClick={this.reload}>{t('error.reload')}</Button>
          </CardFooter>
        </Card>
      </div>
    );
  }
}
