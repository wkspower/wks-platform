import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { ActivityTabPlaceholder } from './ActivityTabPlaceholder';

describe('ActivityTabPlaceholder', () => {
  it('renders heading + body via t() and exposes the test id', () => {
    render(<ActivityTabPlaceholder />);
    expect(screen.getByText('Activity feed coming soon')).toBeInTheDocument();
    expect(screen.getByText('Real-time case activity arrives in Epic 4.')).toBeInTheDocument();
    expect(screen.getByTestId('activity-placeholder')).toBeInTheDocument();
  });
});
