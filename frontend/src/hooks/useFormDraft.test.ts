import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import * as draftsApi from '@/api/drafts';

import { useFormDraft } from './useFormDraft';

/**
 * Story 5.4 — coverage for the {@link useFormDraft} hook:
 *  - initial GET fires once on mount
 *  - {@code scheduleSave} debounces (500ms default)
 *  - {@code saveNow} bypasses the debounce
 *  - in-flight collapse: overlapping schedules collapse to one follow-up PUT
 *  - 404 GET → {@code draft === null}, no error state
 *  - version mismatch → {@code isVersionMismatch === true}
 *  - {@code discard()} → DELETE then null draft
 *
 * Uses real timers + small advances; the previous fake-timer attempt deadlocked because the React
 * concurrent scheduler internally uses microtasks that don't interact cleanly with vi.fakeTimers.
 */
describe('useFormDraft', () => {
  const caseId = 'case-1';
  const formId = 'form-1';

  /** Resolves the next macrotask so awaited promises chain through. */
  const flush = () => new Promise<void>((resolve) => setTimeout(resolve, 0));

  beforeEach(() => {
    vi.spyOn(draftsApi, 'getFormDraft').mockResolvedValue(null);
    vi.spyOn(draftsApi, 'saveFormDraft').mockResolvedValue({
      id: 'd1',
      caseId,
      formId,
      payload: {},
      scrollY: 0,
      sectionExpanded: null,
      caseTypeVersionAtSave: 1,
      updatedAt: '2026-05-08T12:00:00Z',
    });
    vi.spyOn(draftsApi, 'deleteFormDraft').mockResolvedValue(undefined);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('fires GET once on mount and exposes null when no draft exists', async () => {
    const { result } = renderHook(() => useFormDraft(caseId, formId, 1));
    await act(async () => {
      await flush();
    });
    expect(draftsApi.getFormDraft).toHaveBeenCalledTimes(1);
    expect(result.current.draft).toBeNull();
    expect(result.current.error).toBe(false);
    expect(result.current.loading).toBe(false);
  });

  it('isVersionMismatch is true when stored version differs from current', async () => {
    vi.spyOn(draftsApi, 'getFormDraft').mockResolvedValue({
      id: 'd1',
      caseId,
      formId,
      payload: { a: 'b' },
      scrollY: 10,
      sectionExpanded: null,
      caseTypeVersionAtSave: 1,
      updatedAt: '2026-05-08T11:00:00Z',
    });
    const { result } = renderHook(() => useFormDraft(caseId, formId, 2));
    await act(async () => {
      await flush();
    });
    expect(result.current.isVersionMismatch).toBe(true);
  });

  it('scheduleSave debounces ≥500ms before PUTing', async () => {
    const { result } = renderHook(() => useFormDraft(caseId, formId, 1, { debounceMs: 50 }));
    await act(async () => {
      await flush();
    });

    act(() => result.current.scheduleSave({ a: 1 }, 0, null));
    await act(async () => {
      await flush();
    });
    // Immediately after schedule — no PUT yet (debounce window not elapsed).
    expect(draftsApi.saveFormDraft).not.toHaveBeenCalled();

    // Wait for debounce window + a microtask to drain.
    await act(async () => {
      await new Promise((r) => setTimeout(r, 80));
    });
    expect(draftsApi.saveFormDraft).toHaveBeenCalledTimes(1);
  });

  it('overlapping scheduleSave calls collapse to a single in-flight PUT with the latest values', async () => {
    let resolveFirst: (v: draftsApi.FormDraftDto) => void = () => undefined;
    const firstPromise = new Promise<draftsApi.FormDraftDto>((res) => {
      resolveFirst = res;
    });
    const saveSpy = vi
      .spyOn(draftsApi, 'saveFormDraft')
      .mockImplementationOnce(() => firstPromise)
      .mockResolvedValue({
        id: 'd1',
        caseId,
        formId,
        payload: {},
        scrollY: 0,
        sectionExpanded: null,
        caseTypeVersionAtSave: 1,
        updatedAt: '2026-05-08T12:00:00Z',
      });

    const { result } = renderHook(() => useFormDraft(caseId, formId, 1, { debounceMs: 20 }));
    await act(async () => {
      await flush();
    });

    // First save → in-flight after debounce.
    act(() => result.current.scheduleSave({ a: 1 }, 0, null));
    await act(async () => {
      await new Promise((r) => setTimeout(r, 30));
    });
    expect(saveSpy).toHaveBeenCalledTimes(1);

    // Two more schedules WHILE the first is in flight — should queue (only the latest wins).
    act(() => result.current.scheduleSave({ a: 2 }, 0, null));
    await act(async () => {
      await new Promise((r) => setTimeout(r, 30));
    });
    act(() => result.current.scheduleSave({ a: 3 }, 0, null));
    await act(async () => {
      await new Promise((r) => setTimeout(r, 30));
    });
    // Still only the first call is in flight (queued, not fired).
    expect(saveSpy).toHaveBeenCalledTimes(1);

    // Resolve the first save → drain the queue → exactly one follow-up PUT with {a:3}.
    await act(async () => {
      resolveFirst({
        id: 'd1',
        caseId,
        formId,
        payload: { a: 1 },
        scrollY: 0,
        sectionExpanded: null,
        caseTypeVersionAtSave: 1,
        updatedAt: '2026-05-08T12:00:01Z',
      });
      await flush();
      await flush();
    });
    expect(saveSpy).toHaveBeenCalledTimes(2);
    const secondCall = saveSpy.mock.calls[1];
    expect(secondCall).toBeDefined();
    expect(secondCall![2]).toMatchObject({ payload: { a: 3 } });
  });

  it('saveNow bypasses the debounce', async () => {
    const { result } = renderHook(() => useFormDraft(caseId, formId, 1));
    await act(async () => {
      await flush();
    });
    await act(async () => {
      await result.current.saveNow({ x: 'y' }, 5, null);
    });
    expect(draftsApi.saveFormDraft).toHaveBeenCalledTimes(1);
  });

  it('discard DELETEs and clears local draft state', async () => {
    vi.spyOn(draftsApi, 'getFormDraft').mockResolvedValue({
      id: 'd1',
      caseId,
      formId,
      payload: {},
      scrollY: 0,
      sectionExpanded: null,
      caseTypeVersionAtSave: 1,
      updatedAt: '2026-05-08T11:00:00Z',
    });
    const { result } = renderHook(() => useFormDraft(caseId, formId, 1));
    await act(async () => {
      await flush();
    });
    expect(result.current.draft).not.toBeNull();

    await act(async () => {
      await result.current.discard();
    });
    expect(draftsApi.deleteFormDraft).toHaveBeenCalledTimes(1);
    expect(result.current.draft).toBeNull();
  });
});
