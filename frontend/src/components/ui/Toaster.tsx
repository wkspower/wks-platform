import { X } from 'lucide-react';
import { useEffect } from 'react';
import { create } from 'zustand';

import { cn } from '@/lib/cn';

export type ToastTone = 'success' | 'error' | 'info';

export interface Toast {
  id: string;
  message: string;
  tone: ToastTone;
  undo?: () => Promise<void> | void;
  ttlMs: number;
}

interface ToastStore {
  toasts: Toast[];
  push: (t: Omit<Toast, 'id' | 'ttlMs'> & { id?: string; ttlMs?: number }) => string;
  dismiss: (id: string) => void;
}

const MAX_VISIBLE = 3;
const DEFAULT_TTL = 5000;

export const useToastStore = create<ToastStore>((set, get) => ({
  toasts: [],
  push: (input) => {
    const id = input.id ?? `t-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 6)}`;
    const toast: Toast = {
      id,
      message: input.message,
      tone: input.tone,
      undo: input.undo,
      ttlMs: input.ttlMs ?? DEFAULT_TTL,
    };
    const next = [...get().toasts, toast].slice(-MAX_VISIBLE);
    set({ toasts: next });
    return id;
  },
  dismiss: (id) => set({ toasts: get().toasts.filter((t) => t.id !== id) }),
}));

export function toast(input: Omit<Toast, 'id' | 'ttlMs'> & { id?: string; ttlMs?: number }) {
  return useToastStore.getState().push(input);
}

export function ToastViewport() {
  const toasts = useToastStore((s) => s.toasts);
  return (
    <div aria-live="polite" className="fixed bottom-4 right-4 z-[60] flex flex-col gap-2 pointer-events-none">
      {toasts.map((t) => (
        <ToastItem key={t.id} toast={t} />
      ))}
    </div>
  );
}

function ToastItem({ toast: t }: { toast: Toast }) {
  const dismiss = useToastStore((s) => s.dismiss);

  useEffect(() => {
    const handle = window.setTimeout(() => dismiss(t.id), t.ttlMs);
    return () => window.clearTimeout(handle);
  }, [t.id, t.ttlMs, dismiss]);

  return (
    <div
      role="status"
      className={cn(
        'pointer-events-auto flex items-center gap-3 rounded-md border bg-surface px-3 py-2 shadow-md',
        'min-w-[260px] max-w-[420px] text-[13px]',
        t.tone === 'success' && 'border-border',
        t.tone === 'error' && 'border-[var(--destructive)]',
        t.tone === 'info' && 'border-border',
      )}
    >
      <span
        className={cn(
          'size-2 rounded-full shrink-0',
          t.tone === 'success' && 'bg-[var(--success,#22c55e)]',
          t.tone === 'error' && 'bg-[var(--destructive)]',
          t.tone === 'info' && 'bg-[var(--primary)]',
        )}
      />
      <div className="flex-1 truncate">{t.message}</div>
      {t.undo && (
        <button
          type="button"
          onClick={async () => {
            const fn = t.undo!;
            dismiss(t.id);
            try {
              await fn();
            } catch {
              /* inverse mutation surfaces its own error toast */
            }
          }}
          className="text-[12px] font-medium text-[var(--primary)] hover:underline"
        >
          Undo
        </button>
      )}
      <button
        type="button"
        onClick={() => dismiss(t.id)}
        aria-label="Dismiss"
        className="size-5 inline-flex items-center justify-center rounded text-foreground-muted hover:bg-surface-hover"
      >
        <X className="size-3.5" />
      </button>
    </div>
  );
}
