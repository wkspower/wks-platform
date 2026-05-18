import { QueryClientProvider } from '@tanstack/react-query';
import { Component, StrictMode, type ErrorInfo, type ReactNode } from 'react';
import { createRoot } from 'react-dom/client';
import { RouterProvider } from 'react-router-dom';

import { TooltipProvider } from '@/components/ui/Tooltip';
import { createQueryClient } from '@/lib/queryClient';
import { AuthProvider } from '@/providers/AuthProvider';
import { router } from '@/routes';

import '@/index.css';

class AppErrorBoundary extends Component<{ children: ReactNode }, { error: Error | null }> {
  state = { error: null as Error | null };
  static getDerivedStateFromError(error: Error) {
    return { error };
  }
  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('[AppErrorBoundary]', error, info.componentStack);
  }
  render() {
    if (this.state.error) {
      return (
        <main className="grid min-h-screen place-items-center bg-background p-6 text-center">
          <div>
            <h1 className="text-xl font-semibold mb-1">Something went wrong</h1>
            <p className="text-foreground-muted text-sm">Reload the page or contact your admin.</p>
          </div>
        </main>
      );
    }
    return this.props.children;
  }
}

const root = document.getElementById('root');
if (!root) throw new Error('Missing #root');

const queryClient = createQueryClient();
createRoot(root).render(
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
