import { screen } from '@testing-library/react';
import { HttpResponse, http } from 'msw';
import { describe, expect, it } from 'vitest';

import { renderWithProviders } from '@/test/renderWithProviders';
import { server } from '@/test/server';
import type { ApiSuccessEnvelope } from '@/types/api';
import type { CaseTypeSummary } from '@/types/caseType';

import { AdminPage } from './AdminPage';

const ADMIN_USER = {
  id: 'u-admin',
  email: 'admin@example.com',
  displayName: 'Admin',
  roles: ['admin' as const],
};

describe('AdminPage', () => {
  it('renders Mapping Inspector cards when case-types listing returns rows', async () => {
    const summaries: CaseTypeSummary[] = [
      {
        id: 'auto-loan',
        displayName: 'Auto Loan',
        version: 1,
        statusCount: 3,
        fieldCount: 5,
        permissions: ['view'],
      },
    ];
    server.use(
      http.get('/api/case-types', () =>
        HttpResponse.json<ApiSuccessEnvelope<CaseTypeSummary[]>>(
          { data: summaries, meta: {} },
          { status: 200 },
        ),
      ),
    );

    renderWithProviders(<AdminPage />, {
      initialAuth: { user: ADMIN_USER, status: 'authenticated' },
    });

    const list = await screen.findByTestId('mapping-inspector-case-type-list');
    expect(list).toBeInTheDocument();
    const link = await screen.findByRole('link', { name: /Auto Loan/i });
    expect(link).toHaveAttribute('href', '/admin/mapping-inspector/auto-loan');
  });
});
