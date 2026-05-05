import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import type { ReactNode } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';

import { TooltipProvider } from '@/components/ui/Tooltip';
import { server } from '@/test/server';
import type { CaseDto } from '@/types/case';

import { CaseDetailPanel } from './CaseDetailPanel';

const ID = '11111111-2222-3333-4444-555555555555';

function baseDto(over: Partial<CaseDto> = {}): CaseDto {
  return {
    id: ID,
    caseTypeId: 'loan',
    caseTypeVersion: 1,
    status: 'open',
    assignee: null,
    data: {},
    processInstanceId: null,
    documentCount: 0,
    createdAt: '2026-04-01T00:00:00Z',
    createdBy: null,
    updatedAt: '2026-04-02T00:00:00Z',
    version: 1,
    caseType: {
      id: 'loan',
      displayName: 'Loan',
      version: 1,
      fields: [],
      statuses: [],
      listColumns: [],
      stages: [],
    },
    stages: [],
    ...over,
  };
}

function wrap(ui: ReactNode) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <MemoryRouter>
      <QueryClientProvider client={client}>
        <TooltipProvider delayDuration={0}>{ui}</TooltipProvider>
      </QueryClientProvider>
    </MemoryRouter>,
  );
}

describe('CaseDetailPanel × StageTimeline integration (AC2, AC5, AC11.5)', () => {
  it('does NOT mount StageTimeline for a zero-stage CaseType', async () => {
    server.use(
      http.get(`/api/cases/${ID}`, () => HttpResponse.json({ data: baseDto(), meta: {} })),
    );
    const { container } = wrap(<CaseDetailPanel caseId={ID} onClose={() => undefined} />);
    // Wait for the case to load (heading appears).
    await screen.findByRole('heading', { level: 1 });
    expect(container.querySelector('nav.wks-stage-timeline')).toBeNull();
  });

  it('mounts StageTimeline above the tab list for a multi-stage case', async () => {
    const dto = baseDto({
      caseType: {
        id: 'loan',
        displayName: 'Loan',
        version: 1,
        fields: [],
        statuses: [],
        listColumns: [],
        stages: [
          { id: 'intake', displayName: 'Intake', ordinal: 0 },
          { id: 'decision', displayName: 'Decision', ordinal: 1 },
        ],
      },
      stages: [
        {
          stageId: 'intake',
          displayName: 'Intake',
          ordinal: 0,
          state: 'COMPLETED',
          enteredAt: '2026-04-01T09:00:00Z',
          exitedAt: '2026-04-02T13:00:00Z',
          source: 'manual',
          sourceRef: null,
        },
        {
          stageId: 'decision',
          displayName: 'Decision',
          ordinal: 1,
          state: 'ACTIVE',
          enteredAt: '2026-04-02T13:00:00Z',
          exitedAt: null,
          source: 'manual',
          sourceRef: null,
        },
      ],
    });
    server.use(http.get(`/api/cases/${ID}`, () => HttpResponse.json({ data: dto, meta: {} })));
    const { container } = wrap(<CaseDetailPanel caseId={ID} onClose={() => undefined} />);

    await waitFor(() => {
      expect(container.querySelector('nav.wks-stage-timeline')).not.toBeNull();
    });
    // The timeline appears inside the <header>, which sits before the <CaseActionBar> + <Tabs>.
    const header = container.querySelector('header');
    expect(header?.querySelector('nav.wks-stage-timeline')).not.toBeNull();

    // Both stage labels render.
    expect(screen.getByText('Intake')).toBeInTheDocument();
    expect(screen.getByText('Decision')).toBeInTheDocument();
  });
});
