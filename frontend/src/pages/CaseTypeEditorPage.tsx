import Editor, { loader } from '@monaco-editor/react';
import * as monaco from 'monaco-editor';
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';
import { ArrowLeft, Loader2, Save } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { ApiError } from '@/api/client';
import { deployCaseType, getCaseTypeSource } from '@/api/caseTypes';
import { Button } from '@/components/ui/Button';
import { Spinner } from '@/components/ui/Spinner';
import { toast } from '@/components/ui/Toaster';

if (typeof self !== 'undefined') {
  self.MonacoEnvironment = {
    getWorker() {
      return new editorWorker();
    },
  };
}
loader.config({ monaco });

interface ValidationError {
  code: string;
  message: string;
  field?: string | null;
}

export function CaseTypeEditorPage() {
  const { caseTypeId } = useParams<{ caseTypeId: string }>();
  const navigate = useNavigate();
  const [yaml, setYaml] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState<ValidationError[]>([]);
  const [dirty, setDirty] = useState(false);
  const [bumpVersion, setBumpVersion] = useState(false);

  useEffect(() => {
    if (!caseTypeId) return;
    let cancelled = false;
    setLoading(true);
    getCaseTypeSource(caseTypeId)
      .then((source) => {
        if (cancelled) return;
        setYaml(source);
        setDirty(false);
        setErrors([]);
      })
      .catch((err: unknown) => {
        if (cancelled) return;
        const message = err instanceof Error ? err.message : 'Failed to load case-type source';
        toast({ tone: 'error', message });
      })
      .finally(() => {
        if (cancelled) return;
        setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [caseTypeId]);

  const onSave = useCallback(async () => {
    if (!caseTypeId || saving) return;
    setSaving(true);
    setErrors([]);
    try {
      const result = await deployCaseType(yaml, { bumpVersion });
      toast({ tone: 'success', message: `Deployed ${result.caseTypeId} v${result.version}` });
      setDirty(false);
      setBumpVersion(false);
    } catch (err) {
      if (err instanceof ApiError) {
        const aggregate = err.envelopeErrors ?? [{ code: err.code, message: err.message, field: err.field }];
        setErrors(aggregate);
        toast({ tone: 'error', message: `${err.code}: ${err.message}` });
      } else {
        toast({ tone: 'error', message: err instanceof Error ? err.message : 'Deploy failed' });
      }
    } finally {
      setSaving(false);
    }
  }, [caseTypeId, yaml, bumpVersion, saving]);

  if (!caseTypeId) {
    return <div className="p-6 text-foreground-muted">Missing case-type id.</div>;
  }

  return (
    <div className="flex flex-col h-full">
      <header className="flex items-center justify-between gap-3 px-6 py-3 border-b border-border">
        <div className="flex items-center gap-3 min-w-0">
          <Button variant="ghost" size="sm" onClick={() => navigate('/admin')}>
            <ArrowLeft className="size-3.5" /> Admin
          </Button>
          <div className="min-w-0">
            <div className="font-medium text-[13px] truncate">{caseTypeId}</div>
            <div className="text-[11px] text-foreground-muted">Case-type YAML editor</div>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <label className="flex items-center gap-1.5 text-[12px] text-foreground-muted select-none">
            <input
              type="checkbox"
              checked={bumpVersion}
              onChange={(e) => setBumpVersion(e.target.checked)}
              className="size-3.5"
            />
            Bump version
          </label>
          <Button variant="primary" size="sm" onClick={onSave} disabled={!dirty || saving}>
            {saving ? <Loader2 className="size-3.5 animate-spin" /> : <Save className="size-3.5" />}
            Save
          </Button>
        </div>
      </header>

      {errors.length > 0 && (
        <div className="px-6 py-2 bg-[var(--danger-subtle)] border-b border-border">
          <div className="text-[12px] font-medium text-[var(--danger)] mb-1">
            {errors.length} validation {errors.length === 1 ? 'error' : 'errors'}
          </div>
          <ul className="text-[12px] space-y-0.5">
            {errors.map((e, i) => (
              <li key={i} className="font-mono">
                <span className="text-foreground-muted">{e.code}</span>
                {e.field && <span className="text-foreground-subtle"> [{e.field}]</span>}
                <span> — {e.message}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      <div className="flex-1 min-h-0">
        {loading ? (
          <div className="grid place-items-center h-full">
            <Spinner />
          </div>
        ) : (
          <Editor
            value={yaml}
            language="yaml"
            theme="light"
            options={{
              minimap: { enabled: false },
              fontSize: 13,
              tabSize: 2,
              insertSpaces: true,
              automaticLayout: true,
              scrollBeyondLastLine: false,
            }}
            onChange={(value) => {
              setYaml(value ?? '');
              setDirty(true);
            }}
          />
        )}
      </div>
    </div>
  );
}
