import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';

import { server } from '@/test/server';
import type { ApiSuccessEnvelope } from '@/types/api';

import { DocumentsTab } from './DocumentsTab';

const CASE_ID = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee';
const DOC_ID = '11111111-2222-3333-4444-555555555555';

const DOC = {
  id: DOC_ID,
  caseId: CASE_ID,
  fileName: 'invoice.pdf',
  contentType: 'application/pdf',
  sizeBytes: 1024,
  uploadedBy: 'user-uuid',
  uploadedAt: '2026-05-07T10:00:00Z',
};

function envelope<T>(data: T): Response {
  return HttpResponse.json<ApiSuccessEnvelope<T>>({ data, meta: {} });
}

describe('DocumentsTab', () => {
  it('renders empty state when no documents', async () => {
    server.use(http.get(`/api/cases/${CASE_ID}/documents`, () => envelope([])));
    render(<DocumentsTab caseId={CASE_ID} />);

    expect(await screen.findByTestId('documents-empty')).toBeInTheDocument();
    expect(screen.getByText('No documents yet')).toBeInTheDocument();
  });

  it('renders document list when documents exist', async () => {
    server.use(http.get(`/api/cases/${CASE_ID}/documents`, () => envelope([DOC])));
    render(<DocumentsTab caseId={CASE_ID} />);

    expect(await screen.findByText('invoice.pdf')).toBeInTheDocument();
    expect(screen.getByText('1.0 KB')).toBeInTheDocument();
  });

  it('shows upload button', async () => {
    server.use(http.get(`/api/cases/${CASE_ID}/documents`, () => envelope([])));
    render(<DocumentsTab caseId={CASE_ID} />);

    // Wait for list to load first
    await screen.findByTestId('documents-empty');
    expect(screen.getByTestId('upload-button')).toBeInTheDocument();
  });

  it('shows upload error on WKS-DOC-002 (disallowed MIME)', async () => {
    server.use(
      http.get(`/api/cases/${CASE_ID}/documents`, () => envelope([])),
      http.post(`/api/cases/${CASE_ID}/documents`, () =>
        HttpResponse.json(
          { error: { code: 'WKS-DOC-002', message: 'MIME not allowed', field: null }, meta: {} },
          { status: 422 },
        ),
      ),
    );
    render(<DocumentsTab caseId={CASE_ID} />);
    await screen.findByTestId('documents-empty');

    const user = userEvent.setup();
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    const file = new File(['content'], 'evil.txt', { type: 'text/plain' });
    await user.upload(input, file);

    await waitFor(() =>
      expect(screen.getByTestId('upload-error')).toHaveTextContent('File type not allowed.'),
    );
  });

  it('shows WKS-DOC-001 upload error (file too large)', async () => {
    server.use(
      http.get(`/api/cases/${CASE_ID}/documents`, () => envelope([])),
      http.post(`/api/cases/${CASE_ID}/documents`, () =>
        HttpResponse.json(
          { error: { code: 'WKS-DOC-001', message: 'Too large', field: null }, meta: {} },
          { status: 422 },
        ),
      ),
    );
    render(<DocumentsTab caseId={CASE_ID} />);
    await screen.findByTestId('documents-empty');

    const user = userEvent.setup();
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;
    const file = new File(['x'.repeat(100)], 'large.pdf', { type: 'application/pdf' });
    await user.upload(input, file);

    await waitFor(() =>
      expect(screen.getByTestId('upload-error')).toHaveTextContent(
        'File exceeds the 25 MB size limit.',
      ),
    );
  });

  it('opens preview pane when a document row is clicked', async () => {
    server.use(
      http.get(`/api/cases/${CASE_ID}/documents`, () => envelope([DOC])),
      http.get(`/api/documents/${DOC_ID}/preview`, () =>
        envelope({ previewable: true, url: `/api/documents/${DOC_ID}/download` }),
      ),
    );
    render(<DocumentsTab caseId={CASE_ID} />);

    await screen.findByText('invoice.pdf');

    const user = userEvent.setup();
    await user.click(screen.getByTestId(`doc-row-${DOC_ID}`));

    expect(await screen.findByTestId('preview-pane')).toBeInTheDocument();
  });
});
