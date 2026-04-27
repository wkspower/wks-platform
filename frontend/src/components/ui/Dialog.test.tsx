import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useState } from 'react';
import { describe, expect, it } from 'vitest';

import { Dialog, DialogContent, DialogDescription, DialogTitle, DialogTrigger } from './Dialog';

function Harness({ defaultOpen = false }: { defaultOpen?: boolean }) {
  const [open, setOpen] = useState(defaultOpen);
  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger>Open</DialogTrigger>
      <DialogContent>
        <DialogTitle>Hello</DialogTitle>
        <DialogDescription>World</DialogDescription>
      </DialogContent>
    </Dialog>
  );
}

describe('Dialog', () => {
  it('renders title + description with the right ARIA wiring', () => {
    render(<Harness defaultOpen />);
    const dialog = screen.getByRole('dialog');
    expect(dialog).toBeInTheDocument();
    expect(screen.getByText('Hello')).toBeInTheDocument();
    expect(screen.getByText('World')).toBeInTheDocument();
  });

  it('close button has an accessible name (P7 — fixes empty sr-only span)', () => {
    render(<Harness defaultOpen />);
    expect(screen.getByRole('button', { name: 'Close' })).toBeInTheDocument();
  });

  it('opens via trigger and closes via the X button', async () => {
    const user = userEvent.setup();
    render(<Harness />);
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Open' }));
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Close' }));
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });
});
