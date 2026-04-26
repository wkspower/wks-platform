import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from './Tooltip';

function setup() {
  return render(
    <TooltipProvider delayDuration={0}>
      <Tooltip>
        <TooltipTrigger>Hover me</TooltipTrigger>
        <TooltipContent>Hello world</TooltipContent>
      </Tooltip>
    </TooltipProvider>,
  );
}

describe('Tooltip', () => {
  it('opens on hover', async () => {
    const user = userEvent.setup();
    setup();
    await user.hover(screen.getByText('Hover me'));
    expect(await screen.findByRole('tooltip')).toHaveTextContent('Hello world');
  });

  it('opens on focus', async () => {
    const user = userEvent.setup();
    setup();
    await user.tab();
    expect(await screen.findByRole('tooltip')).toHaveTextContent('Hello world');
  });
});
