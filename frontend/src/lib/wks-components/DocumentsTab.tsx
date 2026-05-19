import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Download, FileText, Upload } from 'lucide-react';
import { useRef, useState } from 'react';

import { downloadUrl, listDocuments, uploadDocument } from '@/api/documents';
import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { Spinner } from '@/components/ui/Spinner';
import { formatRelativeTime } from '@/lib/formatDate';
import { caseQueryKeys } from '@/lib/queryKeys';
import { cn } from '@/lib/cn';

function formatBytes(b: number) {
  if (b < 1024) return `${b} B`;
  if (b < 1024 * 1024) return `${(b / 1024).toFixed(1)} KB`;
  return `${(b / 1024 / 1024).toFixed(1)} MB`;
}

export function DocumentsTab({ caseId }: { caseId: string }) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [dragOver, setDragOver] = useState(false);
  const qc = useQueryClient();
  const { data: docs, isLoading } = useQuery({
    queryKey: ['documents', caseId],
    queryFn: () => listDocuments(caseId),
  });

  const upload = useMutation({
    mutationFn: (file: File) => uploadDocument(caseId, file),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['documents', caseId] });
      qc.invalidateQueries({ queryKey: caseQueryKeys.detail(caseId) });
    },
  });

  const handleFiles = (files: FileList | null) => {
    if (!files || files.length === 0) return;
    for (const f of Array.from(files)) upload.mutate(f);
  };

  return (
    <div className="px-6 py-5">
      <div
        onDragOver={(e) => {
          e.preventDefault();
          setDragOver(true);
        }}
        onDragLeave={() => setDragOver(false)}
        onDrop={(e) => {
          e.preventDefault();
          setDragOver(false);
          handleFiles(e.dataTransfer.files);
        }}
        className={cn(
          'rounded-lg border-2 border-dashed px-4 py-6 text-center transition-colors',
          dragOver ? 'border-[var(--primary)] bg-[var(--primary-soft)]' : 'border-border bg-surface',
        )}
      >
        <Upload className="size-5 mx-auto text-foreground-subtle" />
        <p className="mt-1.5 text-[13px]">Drop files here or click to upload</p>
        <p className="text-[11px] text-foreground-muted mt-0.5">Multiple files supported</p>
        <input
          ref={inputRef}
          type="file"
          multiple
          className="hidden"
          onChange={(e) => handleFiles(e.target.files)}
        />
        <Button variant="secondary" size="sm" className="mt-3" onClick={() => inputRef.current?.click()}>
          Choose files
        </Button>
        {upload.isPending && (
          <div className="mt-2 inline-flex items-center gap-1.5 text-[12px] text-foreground-muted">
            <Spinner className="size-3.5" /> Uploading…
          </div>
        )}
      </div>

      <h3 className="mt-6 mb-2 text-[11px] uppercase tracking-wider text-foreground-subtle font-medium">
        Documents {docs && `(${docs.length})`}
      </h3>
      {isLoading ? (
        <div className="grid place-items-center py-8">
          <Spinner />
        </div>
      ) : !docs || docs.length === 0 ? (
        <p className="text-[13px] text-foreground-muted py-6 text-center">No documents yet.</p>
      ) : (
        <ul className="divide-y divide-divider rounded-md border border-border bg-canvas overflow-hidden">
          {docs.map((d) => (
            <li key={d.id} className="flex items-center gap-3 px-3 py-2.5 hover:bg-surface-hover">
              <FileText className="size-4 text-foreground-subtle shrink-0" />
              <div className="flex-1 min-w-0">
                <div className="text-[13px] font-medium truncate">{d.fileName}</div>
                <div className="text-[11px] text-foreground-muted flex items-center gap-1.5 mt-0.5">
                  <Avatar name={d.uploadedBy} size="xs" />
                  {d.uploadedBy} · {formatRelativeTime(d.uploadedAt)} · {formatBytes(d.sizeBytes)}
                </div>
              </div>
              <a
                href={downloadUrl(d.id)}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-1 text-[12px] text-[var(--primary)] hover:underline"
              >
                <Download className="size-3.5" /> Download
              </a>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
