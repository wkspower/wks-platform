import { useCallback, useEffect, useRef, useState } from 'react';

import { deleteFormDraft, getFormDraft, saveFormDraft, type FormDraftDto } from '@/api/drafts';

export type DraftSaveState = 'idle' | 'saving' | 'saved' | 'error';

export interface UseFormDraftResult {
  draft: FormDraftDto | null;
  loading: boolean;
  error: boolean;
  /** True when a draft exists AND its caseTypeVersionAtSave doesn't match the current case version (AC3). */
  isVersionMismatch: boolean;
  saveState: DraftSaveState;
  lastSavedAt: Date | null;
  /** Debounced PUT — collapses overlapping calls; only one in-flight PUT at a time. */
  scheduleSave: (
    payload: Record<string, unknown>,
    scrollY: number,
    sectionExpanded: Record<string, boolean> | null,
  ) => void;
  /** Immediate PUT, bypassing the debounce — used by the explicit "Save Draft" button (AC1). */
  saveNow: (
    payload: Record<string, unknown>,
    scrollY: number,
    sectionExpanded: Record<string, boolean> | null,
  ) => Promise<void>;
  /** DELETE the draft and clear local state — used by AC2/AC3 discard actions. */
  discard: () => Promise<void>;
}

export interface UseFormDraftOptions {
  /** Debounce window in ms; default 500. AC1 requires ≥500ms. */
  debounceMs?: number;
}

/**
 * Story 5.4 — auto-save + resume hook for the form renderers.
 *
 * Lifecycle:
 *   - on mount: GET the existing draft once → exposes {@code draft} + {@code isVersionMismatch}.
 *   - on user input: caller invokes {@link scheduleSave}; the hook debounces (default 500ms) +
 *     collapses overlapping calls (only one in-flight PUT at a time; the latest values queued win).
 *   - on explicit Save Draft button: caller invokes {@link saveNow} which bypasses the debounce.
 *   - on resume "Discard" / version-mismatch "Discard" actions: caller invokes {@link discard}.
 *
 * AC1 — debounced auto-save with collapsed overlapping PUTs.
 * AC3 — {@code isVersionMismatch} surfaces the gate state to the page-level orchestrator.
 *
 * The hook does NOT auto-fire saves; the renderer (or page) decides when to call scheduleSave.
 * This keeps the renderers in control of their RHF lifecycle and avoids cascading effect loops.
 */
export function useFormDraft(
  caseId: string,
  formId: string,
  currentCaseTypeVersion: number,
  options: UseFormDraftOptions = {},
): UseFormDraftResult {
  const debounceMs = options.debounceMs ?? 500;

  const [draft, setDraft] = useState<FormDraftDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [saveState, setSaveState] = useState<DraftSaveState>('idle');
  const [lastSavedAt, setLastSavedAt] = useState<Date | null>(null);

  const debounceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const inFlight = useRef(false);
  const queuedPayload = useRef<{
    payload: Record<string, unknown>;
    scrollY: number;
    sectionExpanded: Record<string, boolean> | null;
  } | null>(null);
  const isMounted = useRef(true);

  // Initial GET — once per (caseId, formId) tuple.
  useEffect(() => {
    isMounted.current = true;
    setLoading(true);
    setError(false);
    getFormDraft(caseId, formId)
      .then((d) => {
        if (!isMounted.current) return;
        setDraft(d);
        setLoading(false);
      })
      .catch(() => {
        if (!isMounted.current) return;
        setError(true);
        setLoading(false);
      });
    return () => {
      isMounted.current = false;
      if (debounceTimer.current !== null) {
        clearTimeout(debounceTimer.current);
        debounceTimer.current = null;
      }
    };
  }, [caseId, formId]);

  const performSave = useCallback(
    async (
      payload: Record<string, unknown>,
      scrollY: number,
      sectionExpanded: Record<string, boolean> | null,
    ): Promise<void> => {
      if (inFlight.current) {
        // Queue — the most recent values overwrite any older queued values.
        queuedPayload.current = { payload, scrollY, sectionExpanded };
        return;
      }
      inFlight.current = true;
      if (isMounted.current) setSaveState('saving');
      try {
        const saved = await saveFormDraft(caseId, formId, {
          payload,
          scrollY,
          sectionExpanded,
          caseTypeVersionAtSave: currentCaseTypeVersion,
        });
        if (isMounted.current) {
          setDraft(saved);
          setSaveState('saved');
          setLastSavedAt(new Date());
        }
      } catch {
        if (isMounted.current) setSaveState('error');
      } finally {
        inFlight.current = false;
        // Drain queue if a newer save was requested while in flight.
        const queued = queuedPayload.current;
        queuedPayload.current = null;
        if (queued && isMounted.current) {
          await performSave(queued.payload, queued.scrollY, queued.sectionExpanded);
        }
      }
    },
    [caseId, formId, currentCaseTypeVersion],
  );

  const scheduleSave = useCallback<UseFormDraftResult['scheduleSave']>(
    (payload, scrollY, sectionExpanded) => {
      if (debounceTimer.current !== null) {
        clearTimeout(debounceTimer.current);
      }
      debounceTimer.current = setTimeout(() => {
        debounceTimer.current = null;
        void performSave(payload, scrollY, sectionExpanded);
      }, debounceMs);
    },
    [debounceMs, performSave],
  );

  const saveNow = useCallback<UseFormDraftResult['saveNow']>(
    async (payload, scrollY, sectionExpanded) => {
      if (debounceTimer.current !== null) {
        clearTimeout(debounceTimer.current);
        debounceTimer.current = null;
      }
      await performSave(payload, scrollY, sectionExpanded);
    },
    [performSave],
  );

  const discard = useCallback<UseFormDraftResult['discard']>(async () => {
    try {
      await deleteFormDraft(caseId, formId);
      if (isMounted.current) {
        setDraft(null);
        setSaveState('idle');
        setLastSavedAt(null);
      }
    } catch {
      if (isMounted.current) setSaveState('error');
    }
  }, [caseId, formId]);

  const isVersionMismatch =
    draft !== null && draft.caseTypeVersionAtSave !== currentCaseTypeVersion;

  return {
    draft,
    loading,
    error,
    isVersionMismatch,
    saveState,
    lastSavedAt,
    scheduleSave,
    saveNow,
    discard,
  };
}
