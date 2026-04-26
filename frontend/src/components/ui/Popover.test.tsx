import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { Popover, PopoverContent, PopoverTrigger } from './Popover';

describe('Popover', () => {
  it('opens on click and shows content', async () => {
    const user = userEvent.setup();
    render(
      <Popover>
        <PopoverTrigger>Open</PopoverTrigger>
        <PopoverContent>Body</PopoverContent>
      </Popover>,
    );
    expect(screen.queryByText('Body')).not.toBeInTheDocument();
    await user.click(screen.getByText('Open'));
    expect(await screen.findByText('Body')).toBeInTheDocument();
  });
});
