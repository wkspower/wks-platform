import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { App } from '@/App';

describe('App', () => {
  it('renders the WKS Platform v2 placeholder', () => {
    render(<App />);
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('WKS Platform v2');
    expect(screen.getByText(/coming soon/i)).toBeInTheDocument();
  });
});
