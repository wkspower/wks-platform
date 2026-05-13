import { screen, waitFor } from '@testing-library/react';
import { HttpResponse, http } from 'msw';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';

import type { MappingInspectorDto, RecentSignalsDto } from '@/api/mappingInspector';
import { renderWithProviders } from '@/test/renderWithProviders';
import { server } from '@/test/server';
import type { ApiSuccessEnvelope } from '@/types/api';

import { MappingInspectorPage } from './MappingInspectorPage';

function mappingHandler(caseTypeId: string, dto: MappingInspectorDto) {
  return http.get(`/api/admin/case-types/${caseTypeId}/mapping-inspector`, () =>
    HttpResponse.json<ApiSuccessEnvelope<MappingInspectorDto>>(
      { data: dto, meta: {} },
      { status: 200 },
    ),
  );
}

function signalsHandler(caseTypeId: string, dto: RecentSignalsDto) {
  return http.get(`/api/admin/case-types/${caseTypeId}/recent-signals`, () =>
    HttpResponse.json<ApiSuccessEnvelope<RecentSignalsDto>>(
      { data: dto, meta: {} },
      { status: 200 },
    ),
  );
}

const ADMIN_USER = {
  id: 'u-admin',
  email: 'admin@example.com',
  displayName: 'Admin',
  roles: ['admin' as const],
};

function renderAt(path: string) {
  return renderWithProviders(
    <Routes>
      <Route path="/admin/mapping-inspector/:caseTypeId" element={<MappingInspectorPage />} />
    </Routes>,
    { initialPath: path, initialAuth: { user: ADMIN_USER, status: 'authenticated' } },
  );
}

describe('MappingInspectorPage', () => {
  it('renders the zero-attachment notice for an empty mapping', async () => {
    server.use(
      mappingHandler('simple', {
        caseTypeId: 'simple',
        version: '1',
        attachments: [],
        emptyMapping: true,
      }),
      signalsHandler('simple', { caseTypeId: 'simple', signals: [] }),
    );
    renderAt('/admin/mapping-inspector/simple');

    expect(await screen.findByText(/no BPMN attachments yet/i)).toBeInTheDocument();
  });

  it('renders WKS-MAP-404 rows with a deep link to the mapping panel', async () => {
    server.use(
      mappingHandler('auto-loan', {
        caseTypeId: 'auto-loan',
        version: '1',
        attachments: [
          {
            name: 'attachment-0',
            bpmnSource: 'auto-loan.bpmn',
            elements: [
              {
                bpmnElement: 'endEvent',
                wksEffect: 'stageTransition',
                target: 'underwriting',
                rule: 'endEventMapping',
              },
            ],
          },
        ],
        emptyMapping: false,
      }),
      signalsHandler('auto-loan', {
        caseTypeId: 'auto-loan',
        signals: [
          {
            timestamp: '2026-05-09T14:22:55.012Z',
            kind: 'TASK_COMPLETED',
            source: 'userTask_review',
            decision: 'unmapped',
            matchedRule: null,
            effect: null,
            caseId: 'b3d2a000-0000-0000-0000-000000000000',
            errorCode: 'WKS-MAP-404',
          },
        ],
      }),
    );
    renderAt('/admin/mapping-inspector/auto-loan');

    const deepLink = await screen.findByTestId('map-404-deep-link');
    expect(deepLink).toBeInTheDocument();
    expect(deepLink).toHaveAttribute('href', '#routing-block');
    // Warning row treatment.
    expect(await screen.findByTestId('recent-signal-row-miss')).toBeInTheDocument();
  });

  it('polls /recent-signals on the configured 5s interval', async () => {
    let signalsCallCount = 0;
    server.use(
      mappingHandler('auto-loan', {
        caseTypeId: 'auto-loan',
        version: '1',
        attachments: [],
        emptyMapping: true,
      }),
      http.get('/api/admin/case-types/auto-loan/recent-signals', () => {
        signalsCallCount += 1;
        return HttpResponse.json<ApiSuccessEnvelope<RecentSignalsDto>>(
          { data: { caseTypeId: 'auto-loan', signals: [] }, meta: {} },
          { status: 200 },
        );
      }),
    );
    vi.useFakeTimers({ shouldAdvanceTime: true });
    renderAt('/admin/mapping-inspector/auto-loan');

    // Initial fetch happens during mount.
    await waitFor(() => expect(signalsCallCount).toBeGreaterThanOrEqual(1));
    const baseline = signalsCallCount;

    // Advance by ~6s — one polling interval should fire.
    await vi.advanceTimersByTimeAsync(6_000);
    await waitFor(() => expect(signalsCallCount).toBeGreaterThan(baseline));
    vi.useRealTimers();
  });

  it('renders the loading state before mapping data resolves', async () => {
    // Delay the mapping response so the loading branch is observable.
    server.use(
      http.get('/api/admin/case-types/auto-loan/mapping-inspector', async () => {
        await new Promise((resolve) => setTimeout(resolve, 50));
        return HttpResponse.json<ApiSuccessEnvelope<MappingInspectorDto>>(
          {
            data: {
              caseTypeId: 'auto-loan',
              version: '1',
              attachments: [],
              emptyMapping: true,
            },
            meta: {},
          },
          { status: 200 },
        );
      }),
      signalsHandler('auto-loan', { caseTypeId: 'auto-loan', signals: [] }),
    );
    renderAt('/admin/mapping-inspector/auto-loan');
    expect(await screen.findByText(/Loading mapping/i)).toBeInTheDocument();
    // Eventually transitions to the empty mapping state.
    expect(await screen.findByText(/no BPMN attachments yet/i)).toBeInTheDocument();
  });
});
