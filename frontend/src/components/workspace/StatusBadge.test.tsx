import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { loanApplicationCaseTypeView } from '@/test/fixtures/buildCaseListFixture';

import { StatusBadge } from './StatusBadge';

describe('StatusBadge', () => {
  const caseType = loanApplicationCaseTypeView();

  it('renders the status displayName paired with a colour token', () => {
    render(<StatusBadge status="open" caseType={caseType} />);
    const badge = screen.getByText('Open');
    expect(badge).toBeInTheDocument();
    expect(badge.getAttribute('style')).toContain('var(--status-open)');
  });

  it('falls back to neutral closed colour and the raw id on unknown status, with a warn', () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    render(<StatusBadge status="unknown-status" caseType={caseType} />);
    const badge = screen.getByText('unknown-status');
    expect(badge.getAttribute('style')).toContain('var(--status-closed)');
    expect(warn).toHaveBeenCalled();
    warn.mockRestore();
  });

  it('always emits a text label (a11y: never colour alone)', () => {
    render(<StatusBadge status="resolved" caseType={caseType} />);
    expect(screen.getByText('Resolved')).toBeInTheDocument();
  });
});
