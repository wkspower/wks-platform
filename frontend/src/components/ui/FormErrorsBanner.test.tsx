import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { FormErrorsBanner } from './FormErrorsBanner';

describe('FormErrorsBanner (Story 2.8 AC9)', () => {
  it('renders nothing when there are no errors', () => {
    const { container } = render(<FormErrorsBanner errors={[]} onAnchorClick={() => {}} />);
    expect(container.firstChild).toBeNull();
  });

  it('renders singular copy when count is 1', () => {
    render(
      <FormErrorsBanner
        errors={[{ field: 'name', displayName: 'Name', order: 0 }]}
        onAnchorClick={() => {}}
      />,
    );
    expect(screen.getByText('1 field needs attention')).toBeInTheDocument();
  });

  it('renders plural copy with the count interpolated', () => {
    render(
      <FormErrorsBanner
        errors={[
          { field: 'name', displayName: 'Name', order: 0 },
          { field: 'email', displayName: 'Email', order: 1 },
        ]}
        onAnchorClick={() => {}}
      />,
    );
    expect(screen.getByText('2 fields need attention')).toBeInTheDocument();
  });

  it('renders anchor links sorted by field.order ascending', () => {
    render(
      <FormErrorsBanner
        errors={[
          { field: 'b', displayName: 'Beta', order: 2 },
          { field: 'a', displayName: 'Alpha', order: 1 },
        ]}
        onAnchorClick={() => {}}
      />,
    );
    const buttons = screen.getAllByRole('button');
    expect(buttons.map((b) => b.textContent)).toEqual(['Alpha', 'Beta']);
  });

  it('uses role="alert" + aria-live="polite" (single live region per AC9)', () => {
    render(
      <FormErrorsBanner
        errors={[{ field: 'name', displayName: 'Name', order: 0 }]}
        onAnchorClick={() => {}}
      />,
    );
    const region = screen.getByRole('alert');
    expect(region).toHaveAttribute('aria-live', 'polite');
  });

  it('invokes onAnchorClick with the field id', () => {
    const onAnchorClick = vi.fn();
    render(
      <FormErrorsBanner
        errors={[{ field: 'name', displayName: 'Name', order: 0 }]}
        onAnchorClick={onAnchorClick}
      />,
    );
    fireEvent.click(screen.getByRole('button', { name: 'Name' }));
    expect(onAnchorClick).toHaveBeenCalledWith('name');
  });
});
