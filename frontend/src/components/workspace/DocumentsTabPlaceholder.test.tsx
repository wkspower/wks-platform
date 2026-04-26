import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { DocumentsTabPlaceholder } from './DocumentsTabPlaceholder';

describe('DocumentsTabPlaceholder', () => {
  it('renders heading + body via t() and exposes the test id', () => {
    render(<DocumentsTabPlaceholder />);
    expect(screen.getByText('Documents coming soon')).toBeInTheDocument();
    expect(screen.getByText('Document upload and download arrive in Epic 3.')).toBeInTheDocument();
    expect(screen.getByTestId('documents-placeholder')).toBeInTheDocument();
  });
});
