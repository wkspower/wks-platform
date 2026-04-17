import { describe, expect, it } from 'vitest';

import { renderWithProviders } from '@/test/renderWithProviders';

import { Button } from './Button';

describe('Button', () => {
  it('renders with an accessible name', () => {
    const { getByRole } = renderWithProviders(<Button>Save</Button>);
    expect(getByRole('button')).toHaveAccessibleName('Save');
  });

  it('honors size and variant classes', () => {
    const { getByRole } = renderWithProviders(
      <Button variant="destructive" size="lg">
        Delete
      </Button>,
    );
    const btn = getByRole('button');
    expect(btn.className).toMatch(/destructive/);
    expect(btn.className).toMatch(/h-12/);
  });

  it('asChild composes onto the child element', () => {
    const { getByRole } = renderWithProviders(
      <Button asChild>
        <a href="/somewhere">Go</a>
      </Button>,
    );
    expect(getByRole('link')).toHaveAccessibleName('Go');
  });
});
