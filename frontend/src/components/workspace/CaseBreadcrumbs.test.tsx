import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { CaseBreadcrumbs } from './CaseBreadcrumbs';

function setup(short: string) {
  return render(
    <MemoryRouter>
      <CaseBreadcrumbs caseIdShort={short} />
    </MemoryRouter>,
  );
}

describe('CaseBreadcrumbs', () => {
  it('renders Cases link to /cases and current page span', () => {
    setup('abcd1234');
    const link = screen.getByRole('link', { name: 'Cases' });
    expect(link).toHaveAttribute('href', '/cases');
    expect(screen.getByText('abcd1234')).toHaveAttribute('aria-current', 'page');
  });

  it('hides separator from screen readers', () => {
    const { container } = setup('abcd1234');
    const sep = container.querySelector('[aria-hidden="true"]');
    expect(sep).toBeTruthy();
    expect(sep?.textContent).toBe('›');
  });

  it('exposes the breadcrumbs landmark', () => {
    setup('abcd1234');
    expect(screen.getByLabelText('Breadcrumbs')).toBeInTheDocument();
  });
});
