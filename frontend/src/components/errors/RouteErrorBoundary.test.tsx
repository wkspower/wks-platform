import { describe, expect, it, vi } from 'vitest';

import { renderWithProviders } from '@/test/renderWithProviders';

import { RouteErrorBoundary } from './RouteErrorBoundary';

function Boom(): never {
  throw new Error('kaboom');
}

describe('RouteErrorBoundary', () => {
  it('renders the fallback card when a child throws', () => {
    const errSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    const { getByRole, getByText } = renderWithProviders(
      <RouteErrorBoundary>
        <Boom />
      </RouteErrorBoundary>,
    );
    expect(getByRole('heading', { level: 2 })).toHaveTextContent(/something unexpected/i);
    expect(getByText(/work is saved on the server/i)).toBeInTheDocument();
    errSpy.mockRestore();
  });

  it('renders children verbatim when no error is thrown', () => {
    const { getByText } = renderWithProviders(
      <RouteErrorBoundary>
        <p>OK</p>
      </RouteErrorBoundary>,
    );
    expect(getByText('OK')).toBeInTheDocument();
  });
});
