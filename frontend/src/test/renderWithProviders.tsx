import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, type RenderOptions, type RenderResult } from '@testing-library/react';
import type { ReactElement, ReactNode } from 'react';
import { MemoryRouter } from 'react-router-dom';

import { TooltipProvider } from '@/components/ui/Tooltip';
import { useAuthStore, type AuthState, type AuthStatus } from '@/stores/authStore';
import type { AuthUser } from '@/types/auth';

export function createTestQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false, gcTime: 0, staleTime: 0 },
      mutations: { retry: false },
    },
  });
}

export interface RenderWithProvidersOptions extends Omit<RenderOptions, 'wrapper'> {
  initialPath?: string;
  initialAuth?: Partial<Pick<AuthState, 'user' | 'status' | 'error'>> & {
    user?: AuthUser | null;
    status?: AuthStatus;
  };
  queryClient?: QueryClient;
}

/**
 * Render helper used by every component test in this project. Wraps the
 * tree in MemoryRouter + QueryClientProvider, and seeds the Zustand
 * auth store with the requested initial state. Does NOT own MSW
 * lifecycle — that is owned by src/test/setup.ts.
 */
export function renderWithProviders(
  ui: ReactElement,
  { initialPath = '/', initialAuth, queryClient, ...rest }: RenderWithProvidersOptions = {},
): RenderResult {
  if (initialAuth) {
    useAuthStore.setState({
      user: initialAuth.user ?? null,
      status: initialAuth.status ?? (initialAuth.user ? 'authenticated' : 'unauthenticated'),
      error: initialAuth.error ?? null,
    });
  } else {
    useAuthStore.setState({ user: null, status: 'unauthenticated', error: null });
  }

  const client = queryClient ?? createTestQueryClient();

  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={client}>
        <TooltipProvider delayDuration={0}>
          <MemoryRouter initialEntries={[initialPath]}>{children}</MemoryRouter>
        </TooltipProvider>
      </QueryClientProvider>
    );
  }

  return render(ui, { wrapper: Wrapper, ...rest });
}
