import { QueryClient } from '@tanstack/react-query';

import { ApiError } from '@/api/client';

export function createQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 30_000,
        // Skip retry for auth failures — the session-expired bus has
        // already fired on the first attempt; a retry would double-emit
        // and the user is being redirected to /login anyway.
        retry: (failureCount, error) => {
          if (error instanceof ApiError && error.status === 401) return false;
          return failureCount < 1;
        },
        refetchOnWindowFocus: false,
      },
    },
  });
}
