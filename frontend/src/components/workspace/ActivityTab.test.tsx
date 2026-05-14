import { screen, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';

import { renderWithProviders } from '@/test/renderWithProviders';
import { server } from '@/test/server';
import type { ApiSuccessEnvelope } from '@/types/api';
import type { AuditEventList, AuditEventView } from '@/types/auditEvent';

import { ActivityTab } from './ActivityTab';

const CASE_ID = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee';
const ACTOR_ID = '11111111-1111-1111-1111-111111111111';
const OTHER_USER_ID = '22222222-2222-2222-2222-222222222222';

function envelope(data: AuditEventList): Response {
  return HttpResponse.json<ApiSuccessEnvelope<AuditEventList>>({ data, meta: {} });
}

let seq = 0;
function nextId(): string {
  seq += 1;
  return `00000000-0000-0000-0000-${String(seq).padStart(12, '0')}`;
}

function evt(partial: Partial<AuditEventView> & Pick<AuditEventView, 'source'>): AuditEventView {
  return {
    id: partial.id ?? nextId(),
    eventType: partial.eventType ?? 'case.data.edit',
    source: partial.source,
    result: partial.result ?? 'APPLIED',
    previousResult: partial.previousResult ?? null,
    fieldId: partial.fieldId ?? 'priority',
    openTaskId: partial.openTaskId ?? null,
    formId: partial.formId ?? null,
    occurredAt: partial.occurredAt ?? '2026-05-13T10:00:00Z',
  };
}

describe('ActivityTab', () => {
  it('renders empty state when zero events', async () => {
    server.use(
      http.get(`/api/cases/${CASE_ID}/audit-events`, () =>
        envelope({ items: [], truncated: false }),
      ),
    );
    renderWithProviders(<ActivityTab caseId={CASE_ID} />);
    expect(await screen.findByTestId('activity-empty')).toBeInTheDocument();
    expect(screen.getByText('No activity yet on this case')).toBeInTheDocument();
  });

  it('renders loading indicator while the fetch is in flight', async () => {
    server.use(
      http.get(`/api/cases/${CASE_ID}/audit-events`, async () => {
        await new Promise((resolve) => setTimeout(resolve, 30));
        return envelope({ items: [], truncated: false });
      }),
    );
    renderWithProviders(<ActivityTab caseId={CASE_ID} />);
    expect(screen.getByTestId('activity-loading')).toBeInTheDocument();
    // Drains the loading state before the test exits.
    await screen.findByTestId('activity-empty');
  });

  it('renders rows for all four AuditSource variants with correct source copy', async () => {
    server.use(
      http.get(`/api/cases/${CASE_ID}/audit-events`, () =>
        envelope({
          items: [
            evt({ id: 'e1', source: { type: 'USER', payload: { actorId: ACTOR_ID } } }),
            evt({
              id: 'e2',
              source: { type: 'USER', payload: { actorId: OTHER_USER_ID } },
            }),
            evt({ id: 'e3', source: { type: 'AUTO_RULE', payload: { ruleId: 'rule-7' } } }),
            evt({
              id: 'e4',
              source: { type: 'BACKEND', payload: { adapterName: 'bpmn' } },
              result: 'BLOCKED',
              fieldId: 'amount',
              openTaskId: 'task-1',
              formId: 'loan-form',
            }),
            evt({
              id: 'e5',
              source: { type: 'EXECUTION_UNMAPPED', payload: { originAdapter: 'rest' } },
              result: 'REJECTED',
              fieldId: 'status',
            }),
          ],
          truncated: false,
        }),
      ),
    );

    renderWithProviders(<ActivityTab caseId={CASE_ID} />, {
      initialAuth: {
        user: { id: ACTOR_ID, email: 'actor@x', roles: ['officer'] },
      },
    });

    await screen.findByTestId('activity-tab');
    // USER variant — same user id collapses to "You".
    expect(screen.getByTestId('activity-row-e1')).toHaveTextContent('You');
    // USER variant — different user renders "User <short-id>".
    expect(screen.getByTestId('activity-row-e2')).toHaveTextContent('User');
    expect(screen.getByTestId('activity-row-e2')).toHaveTextContent(OTHER_USER_ID.slice(-8));
    // AUTO_RULE variant.
    expect(screen.getByTestId('activity-row-e3')).toHaveTextContent('Automation rule rule-7');
    // BACKEND variant — system + adapter.
    expect(screen.getByTestId('activity-row-e4')).toHaveTextContent('System (bpmn)');
    expect(screen.getByTestId('activity-row-e4')).toHaveTextContent('blocked');
    // EXECUTION_UNMAPPED variant.
    expect(screen.getByTestId('activity-row-e5')).toHaveTextContent(
      'Unmapped backend signal (rest)',
    );
    expect(screen.getByTestId('activity-row-e5')).toHaveTextContent('rejected');
  });

  it('renders eventType-specific copy for non-edit events', async () => {
    server.use(
      http.get(`/api/cases/${CASE_ID}/audit-events`, () =>
        envelope({
          items: [
            // case.created — literal "CREATED" result, no field/topic, "created the case" copy.
            evt({
              id: 'c1',
              eventType: 'case.created',
              source: { type: 'USER', payload: { actorId: ACTOR_ID } },
              result: 'CREATED',
              fieldId: null,
            }),
            // case.status.changed with prior status — "from X to Y" copy.
            evt({
              id: 's1',
              eventType: 'case.status.changed',
              source: { type: 'USER', payload: { actorId: ACTOR_ID } },
              result: 'closed',
              previousResult: 'in_progress',
              fieldId: null,
            }),
            // case.status.changed first transition — no prior status, "set status to Y" copy.
            evt({
              id: 's2',
              eventType: 'case.status.changed',
              source: { type: 'BACKEND', payload: { adapterName: 'bpmn' } },
              result: 'review',
              previousResult: null,
              fieldId: null,
            }),
            // case.document.uploaded — filename in result slot, "uploaded {filename}" copy.
            evt({
              id: 'd1',
              eventType: 'case.document.uploaded',
              source: { type: 'USER', payload: { actorId: ACTOR_ID } },
              result: 'contract.pdf',
              fieldId: null,
            }),
          ],
          truncated: false,
        }),
      ),
    );

    renderWithProviders(<ActivityTab caseId={CASE_ID} />, {
      initialAuth: {
        user: { id: ACTOR_ID, email: 'actor@x', roles: ['officer'] },
      },
    });

    await screen.findByTestId('activity-tab');
    expect(screen.getByTestId('activity-row-c1')).toHaveTextContent('created the case');
    expect(screen.getByTestId('activity-row-s1')).toHaveTextContent(
      'changed status from in_progress to closed',
    );
    expect(screen.getByTestId('activity-row-s2')).toHaveTextContent('set status to review');
    expect(screen.getByTestId('activity-row-d1')).toHaveTextContent('uploaded contract.pdf');
  });

  it('renders truncation notice when wire flag is set', async () => {
    server.use(
      http.get(`/api/cases/${CASE_ID}/audit-events`, () =>
        envelope({
          items: [evt({ id: 't1', source: { type: 'USER', payload: { actorId: ACTOR_ID } } })],
          truncated: true,
        }),
      ),
    );
    renderWithProviders(<ActivityTab caseId={CASE_ID} />);
    await waitFor(() => expect(screen.getByTestId('activity-truncated')).toBeInTheDocument());
    expect(screen.getByTestId('activity-truncated')).toHaveTextContent(
      'Showing the 1 most recent events',
    );
  });

  it('degrades to empty state on fetch error (confidence frame, no red banner)', async () => {
    server.use(
      http.get(`/api/cases/${CASE_ID}/audit-events`, () =>
        HttpResponse.json(
          { error: { code: 'WKS-API-500', message: 'boom' }, meta: {} },
          { status: 500 },
        ),
      ),
    );
    renderWithProviders(<ActivityTab caseId={CASE_ID} />);
    expect(await screen.findByTestId('activity-empty')).toBeInTheDocument();
  });
});
