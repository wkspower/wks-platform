import { FileText, Upload, X, Download } from 'lucide-react';
import { useCallback, useEffect, useRef, useState } from 'react';

import { ApiError } from '@/api/client';
import { downloadUrl, getPreview, listDocuments, uploadDocument } from '@/api/documents';
import type { CaseDocument, PreviewResponse } from '@/api/documents';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';
import { ErrorState } from '@/components/ui/ErrorState';
import { Spinner } from '@/components/ui/Spinner';
import { t } from '@/i18n';

export interface DocumentsTabProps {
  caseId: string;
}

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function uploadErrorMessage(err: unknown): string {
  if (err instanceof ApiError) {
    if (err.code === 'WKS-DOC-001') return t('documents.upload.error.size', { limit: '25' });
    if (err.code === 'WKS-DOC-002') return t('documents.upload.error.mime');
    if (err.code === 'WKS-DOC-003') return t('documents.upload.error.filename');
    return err.message;
  }
  return t('documents.error.generic');
}

function friendlyError(err: unknown): string {
  if (err instanceof ApiError) return err.message;
  return t('documents.error.generic');
}

/**
 * Surface a human-readable label for the file type instead of the raw MIME.
 * Falls back to the part after the slash, uppercased, so a misconfigured
 * server response degrades gracefully — never shows `application/x-foo` to
 * users.
 */
function friendlyFileType(mime: string): string {
  if (mime === 'application/pdf') return 'PDF';
  if (mime.startsWith('image/')) return mime.slice('image/'.length).toUpperCase();
  if (mime === 'application/msword' || mime.includes('wordprocessingml')) return 'Word';
  if (mime === 'application/vnd.ms-excel' || mime.includes('spreadsheetml')) return 'Excel';
  if (mime === 'text/plain') return 'Text';
  if (mime === 'text/csv') return 'CSV';
  const tail = mime.split('/')[1];
  return tail ? tail.toUpperCase() : 'File';
}

interface PreviewPaneProps {
  doc: CaseDocument;
  onClose: () => void;
}

function PreviewPane({ doc, onClose }: PreviewPaneProps) {
  const [preview, setPreview] = useState<PreviewResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    getPreview(doc.id)
      .then((p) => {
        if (!cancelled) {
          setPreview(p);
          setLoading(false);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(friendlyError(err));
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [doc.id]);

  return (
    <div
      data-testid="preview-pane"
      className="mt-3 rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--card)] p-3"
    >
      <div className="mb-2 flex items-center justify-between">
        <span className="truncate text-sm font-medium">{doc.fileName}</span>
        <button
          type="button"
          aria-label="Close preview"
          onClick={onClose}
          className="ml-2 inline-flex size-6 flex-shrink-0 items-center justify-center rounded hover:bg-[var(--muted)]"
        >
          <X aria-hidden className="size-3.5" />
        </button>
      </div>

      {loading && (
        <div className="flex h-24 items-center justify-center">
          <Spinner />
        </div>
      )}

      {!loading && error && <p className="text-sm text-[var(--destructive)]">{error}</p>}

      {!loading && !error && preview && (
        <>
          {preview.previewable && doc.contentType === 'application/pdf' && (
            <iframe src={preview.url} className="h-96 w-full" title={doc.fileName} />
          )}
          {preview.previewable && doc.contentType.startsWith('image/') && (
            <img
              src={preview.url}
              alt={doc.fileName}
              className="max-h-96 max-w-full object-contain"
            />
          )}
          {!preview.previewable && (
            <a
              href={downloadUrl(doc.id)}
              download={doc.fileName}
              className="inline-flex items-center gap-1.5 text-sm font-medium text-[var(--primary)] underline-offset-2 hover:underline"
            >
              <Download aria-hidden className="size-4" />
              {t('documents.preview.download')}
            </a>
          )}
        </>
      )}
    </div>
  );
}

export function DocumentsTab({ caseId }: DocumentsTabProps) {
  const [documents, setDocuments] = useState<CaseDocument[]>([]);
  const [loading, setLoading] = useState(true);
  const [listError, setListError] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [previewDoc, setPreviewDoc] = useState<CaseDocument | null>(null);
  const [isDragOver, setIsDragOver] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const loadDocuments = useCallback(() => {
    setLoading(true);
    setListError(null);
    listDocuments(caseId)
      .then((docs) => {
        setDocuments(docs);
        setLoading(false);
      })
      .catch((err) => {
        setListError(friendlyError(err));
        setLoading(false);
      });
  }, [caseId]);

  useEffect(() => {
    loadDocuments();
  }, [loadDocuments]);

  const handleUpload = useCallback(
    async (file: File) => {
      setUploading(true);
      setUploadError(null);
      try {
        const doc = await uploadDocument(caseId, file);
        setDocuments((prev) => [doc, ...prev]);
      } catch (err) {
        setUploadError(uploadErrorMessage(err));
      } finally {
        setUploading(false);
      }
    },
    [caseId],
  );

  const handleFileChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (file) {
        void handleUpload(file);
        // Reset so the same file can be re-uploaded if needed.
        e.target.value = '';
      }
    },
    [handleUpload],
  );

  const handleDrop = useCallback(
    (e: React.DragEvent<HTMLDivElement>) => {
      e.preventDefault();
      setIsDragOver(false);
      const file = e.dataTransfer.files?.[0];
      if (file) void handleUpload(file);
    },
    [handleUpload],
  );

  const handleDragOver = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragOver(true);
  }, []);

  const handleDragLeave = useCallback(() => {
    setIsDragOver(false);
  }, []);

  return (
    <div data-testid="documents-tab" className="flex flex-col gap-4 py-3">
      {/* Upload zone */}
      <div
        role="region"
        aria-label={t('documents.upload.dragDrop')}
        onDrop={handleDrop}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        className={[
          'flex flex-col items-center justify-center gap-2 rounded-[var(--radius-md)] border-2 border-dashed px-4 py-6 text-center transition-colors',
          isDragOver
            ? 'border-[var(--primary)] bg-[var(--primary)]/5'
            : 'border-[var(--border)] hover:border-[var(--primary)]/50',
        ].join(' ')}
      >
        <Upload aria-hidden className="size-8 text-[var(--muted-foreground)]/60" />
        <p className="text-sm text-[var(--muted-foreground)]">{t('documents.upload.dragDrop')}</p>
        <Button
          type="button"
          size="sm"
          disabled={uploading}
          onClick={() => fileInputRef.current?.click()}
          data-testid="upload-button"
        >
          {uploading ? '…' : t('documents.upload.cta')}
        </Button>
        <input
          ref={fileInputRef}
          type="file"
          className="sr-only"
          aria-hidden
          tabIndex={-1}
          onChange={handleFileChange}
          accept=".pdf,.jpg,.jpeg,.png,.gif,.webp,.svg,.doc,.docx,.xls,.xlsx,.txt,.csv"
        />
      </div>

      {/* Inline upload error */}
      {uploadError && (
        <p role="alert" data-testid="upload-error" className="text-sm text-[var(--destructive)]">
          {uploadError}
        </p>
      )}

      {/* Document list */}
      {loading && (
        <div className="flex items-center justify-center py-6">
          <Spinner />
        </div>
      )}

      {!loading && listError && (
        <ErrorState
          headline={t('documents.error.list')}
          body={listError}
          action={
            <Button variant="secondary" onClick={loadDocuments}>
              {t('common.retry')}
            </Button>
          }
        />
      )}

      {!loading && !listError && documents.length === 0 && (
        <EmptyState
          icon={FileText}
          data-testid="documents-empty"
          headline={t('documents.empty.title')}
          body={t('documents.empty.body')}
        />
      )}

      {!loading && !listError && documents.length > 0 && (
        <ul className="flex flex-col gap-1" role="list" aria-label="Documents">
          {documents.map((doc) => (
            <li key={doc.id}>
              <button
                type="button"
                onClick={() => setPreviewDoc((prev) => (prev?.id === doc.id ? null : doc))}
                className="w-full rounded-[var(--radius-md)] border border-[var(--border)] bg-[var(--card)] px-3 py-2 text-left hover:bg-[var(--muted)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ring)]"
                data-testid={`doc-row-${doc.id}`}
              >
                <div className="flex items-center gap-2">
                  <FileText
                    aria-hidden
                    className="size-4 flex-shrink-0 text-[var(--muted-foreground)]"
                  />
                  <span
                    className="min-w-0 flex-1 truncate text-sm font-medium"
                    title={doc.fileName}
                  >
                    {doc.fileName}
                  </span>
                  <span className="flex-shrink-0 text-xs text-[var(--muted-foreground)]">
                    {formatBytes(doc.sizeBytes)}
                  </span>
                </div>
                <div className="mt-0.5 flex items-center gap-2 pl-6">
                  <span className="truncate text-xs text-[var(--muted-foreground)]">
                    {friendlyFileType(doc.contentType)}
                  </span>
                  <span className="flex-shrink-0 text-xs text-[var(--muted-foreground)]">
                    {formatDate(doc.uploadedAt)}
                  </span>
                </div>
              </button>

              {/* Inline preview pane (shown below the clicked row) */}
              {previewDoc?.id === doc.id && (
                <PreviewPane doc={doc} onClose={() => setPreviewDoc(null)} />
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
