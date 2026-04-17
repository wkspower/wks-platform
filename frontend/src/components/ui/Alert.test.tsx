import { describe, expect, it } from 'vitest';

import { renderWithProviders } from '@/test/renderWithProviders';

import { Alert } from './Alert';

describe('Alert', () => {
  it('default info variant uses role=status', () => {
    const { getByRole } = renderWithProviders(<Alert>Info text</Alert>);
    expect(getByRole('status')).toHaveTextContent('Info text');
  });

  it('destructive variant uses role=alert', () => {
    const { getByRole } = renderWithProviders(<Alert variant="destructive">Bad</Alert>);
    expect(getByRole('alert')).toHaveTextContent('Bad');
  });
});
