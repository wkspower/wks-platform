import { QueryClientProvider } from '@tanstack/react-query';
import { Component, StrictMode, type ErrorInfo, type ReactNode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';

import { AppErrorFallback } from '@/components/errors/AppErrorFallback';
import { TooltipProvider } from '@/components/ui/Tooltip';
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

const STARTUP_FALLBACK_HTML =
  '<main style="min-height:100vh;display:grid;place-items:center;font-family:system-ui,sans-serif;color:#0B1437"><div style="text-align:center"><h1>WKS Platform failed to start</h1><p>Please reload the page or contact support.</p></div></main>';

function renderStartupFallback(): void {
  if (typeof document !== 'undefined' && document.body) {
    document.body.innerHTML = STARTUP_FALLBACK_HTML;
  }
}

const rootElement = document.getElementById('root');
if (!rootElement) {
  renderStartupFallback();
  throw new Error('Missing #root element in index.html');
}

try {
  const queryClient = createQueryClient();
  createRoot(rootElement).render(
    <StrictMode>
      <AppErrorBoundary>
        <AuthProvider>
          <QueryClientProvider client={queryClient}>
            <TooltipProvider delayDuration={300}>
              <RouterProvider router={router} />
            </TooltipProvider>
          </QueryClientProvider>
        </AuthProvider>
      </AppErrorBoundary>
    </StrictMode>,
  );
} catch (err) {
  // Synchronous failure during createRoot/render means AppErrorBoundary
  // never mounted — surface the same inline HTML fallback so users see
  // a visible message instead of a blank page. The error is re-thrown
  // so bundled telemetry (when it lands) can still capture it.
  renderStartupFallback();
  // eslint-disable-next-line no-console
  console.error('[main] startup failed', err);
  throw err;
}
