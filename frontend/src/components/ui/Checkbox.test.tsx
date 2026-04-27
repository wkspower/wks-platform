import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useState } from 'react';
import { describe, expect, it } from 'vitest';

import { Checkbox } from './Checkbox';

function Harness({ initial = false }: { initial?: boolean }) {
  const [checked, setChecked] = useState<boolean>(initial);
  return (
    <Checkbox
      aria-label="Agree"
      checked={checked}
      onCheckedChange={(v) => setChecked(Boolean(v))}
    />
  );
}

describe('Checkbox', () => {
  it('toggles checked state on click', async () => {
    const user = userEvent.setup();
    render(<Harness />);
    const cb = screen.getByRole('checkbox', { name: 'Agree' });
    expect(cb).toHaveAttribute('data-state', 'unchecked');
    await user.click(cb);
    expect(cb).toHaveAttribute('data-state', 'checked');
  });

  it('hasError prop sets aria-invalid', () => {
    render(<Checkbox aria-label="x" hasError />);
    expect(screen.getByRole('checkbox', { name: 'x' })).toHaveAttribute('aria-invalid', 'true');
  });

  it('respects keyboard activation (Space)', async () => {
    const user = userEvent.setup();
    render(<Harness />);
    const cb = screen.getByRole('checkbox', { name: 'Agree' });
    cb.focus();
    await user.keyboard(' ');
    expect(cb).toHaveAttribute('data-state', 'checked');
  });
});
