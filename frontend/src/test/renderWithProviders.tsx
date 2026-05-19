import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, type RenderOptions, type RenderResult } from '@testing-library/react';
import type { ReactElement, ReactNode } from 'react';
import { MemoryRouter } from 'react-router-dom';

import { useAuthStore } from '@/stores/authStore';
import type { AuthUser } from '@/types/auth';

export interface RenderOpts extends Omit<RenderOptions, 'wrapper'> {
  /** Initial entry for the in-memory router. Defaults to `/`. */
  route?: string;
  /** Seed the auth store before render. Pass `null` for unauthenticated. */
  authUser?: AuthUser | null;
}

/**
 * Single source of truth for component tests. Wraps with MemoryRouter +
 * QueryClientProvider and seeds the auth store. Use this for every component
 * test that hits hooks, routing, or auth — do NOT hand-wire providers in tests.
 */
export function renderWithProviders(ui: ReactElement, opts: RenderOpts = {}): RenderResult {
  const { route = '/', authUser = null, ...rest } = opts;

  // Fresh QueryClient per render; retries off so failing queries surface immediately.
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, staleTime: Infinity, gcTime: Infinity },
      mutations: { retry: false },
    },
  });

  useAuthStore.setState({
    user: authUser,
    status: authUser ? 'authenticated' : 'unauthenticated',
    error: null,
  });

  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[route]}>{children}</MemoryRouter>
      </QueryClientProvider>
    );
  }

  return render(ui, { wrapper: Wrapper, ...rest });
}
