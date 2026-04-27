import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { Textarea } from './Textarea';

describe('Textarea', () => {
  it('renders a textarea with the given placeholder + default 4 rows', () => {
    render(<Textarea placeholder="Notes" />);
    const ta = screen.getByPlaceholderText('Notes') as HTMLTextAreaElement;
    expect(ta.tagName).toBe('TEXTAREA');
    expect(ta.rows).toBe(4);
  });

  it('forwards maxLength', async () => {
    const user = userEvent.setup();
    render(<Textarea placeholder="x" maxLength={5} />);
    const ta = screen.getByPlaceholderText('x') as HTMLTextAreaElement;
    await user.type(ta, 'abcdefg');
    expect(ta.value).toBe('abcde');
  });

  it('hasError sets aria-invalid', () => {
    render(<Textarea aria-label="x" hasError />);
    expect(screen.getByRole('textbox', { name: 'x' })).toHaveAttribute('aria-invalid', 'true');
  });
});
