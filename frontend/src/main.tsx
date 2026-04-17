import { QueryClientProvider } from '@tanstack/react-query';
import { Component, StrictMode, type ErrorInfo, type ReactNode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';

import { AppErrorFallback } from '@/components/errors/AppErrorFallback';
import { createQueryClient } from '@/lib/queryClient';
import { AuthProvider } from '@/providers/AuthProvider';
import { router } from '@/routes';

import '@/index.css';

interface BoundaryState {
  error: Error | null;
}

class AppErrorBoundary extends Component<{ children: ReactNode }, BoundaryState> {
  state: BoundaryState = { error: null };
  static getDerivedStateFromError(error: Error): BoundaryState {
    return { error };
  }
  componentDidCatch(error: Error, info: ErrorInfo): void {
    // eslint-disable-next-line no-console
    console.error('[AppErrorBoundary]', error, info.componentStack);
  }
  render() {
    return this.state.error ? <AppErrorFallback /> : this.props.children;
  }
}

const queryClient = createQueryClient();

const rootElement = document.getElementById('root');
if (!rootElement) {
  // Story 1.1 deferred fix: surface a visible fallback rather than a
  // blank screen if index.html is misconfigured.
  document.body.innerHTML =
    '<main style="min-height:100vh;display:grid;place-items:center;font-family:system-ui,sans-serif;color:#0B1437"><div style="text-align:center"><h1>WKS Platform failed to start</h1><p>Missing #root element in index.html. Please contact support.</p></div></main>';
  throw new Error('Missing #root element in index.html');
}

createRoot(rootElement).render(
  <StrictMode>
    <AppErrorBoundary>
      <AuthProvider>
        <QueryClientProvider client={queryClient}>
          <RouterProvider router={router} />
        </QueryClientProvider>
      </AuthProvider>
    </AppErrorBoundary>
  </StrictMode>,
);
