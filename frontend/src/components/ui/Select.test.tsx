import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useState } from 'react';
import { describe, expect, it } from 'vitest';

import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './Select';

function Harness({ initial = undefined as string | undefined }) {
  const [value, setValue] = useState<string | undefined>(initial);
  return (
    <Select value={value} onValueChange={setValue}>
      <SelectTrigger aria-label="Color">
        <SelectValue placeholder="Pick…" />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="red">Red</SelectItem>
        <SelectItem value="blue">Blue</SelectItem>
      </SelectContent>
    </Select>
  );
}

describe('Select', () => {
  it('renders the placeholder when value is undefined', () => {
    render(<Harness />);
    expect(screen.getByText('Pick…')).toBeInTheDocument();
  });

  it('shows the selected option label after pick', async () => {
    const user = userEvent.setup();
    render(<Harness />);
    await user.click(screen.getByRole('combobox', { name: 'Color' }));
    await user.click(screen.getByRole('option', { name: 'Blue' }));
    expect(screen.getByRole('combobox', { name: 'Color' })).toHaveTextContent('Blue');
  });

  it('hasError prop sets aria-invalid on the trigger', () => {
    render(
      <Select>
        <SelectTrigger aria-label="X" hasError>
          <SelectValue placeholder="Pick…" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="x">X</SelectItem>
        </SelectContent>
      </Select>,
    );
    expect(screen.getByRole('combobox', { name: 'X' })).toHaveAttribute('aria-invalid', 'true');
  });
});
