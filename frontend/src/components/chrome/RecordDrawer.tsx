import { X } from 'lucide-react';
import { type ReactNode, useEffect } from 'react';
import { cn } from '@/lib/cn';
import { useViewport } from '@/hooks/useViewport';

export function RecordDrawer({
  open,
  onClose,
  title,
  children,
  width,
}: {
  open: boolean;
  onClose: () => void;
  title?: ReactNode;
  children: ReactNode;
  width?: number;
}) {
  const { width: vw } = useViewport();
  const fullScreen = vw < 1100;
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  return (
    <>
      <div
        aria-hidden
        onClick={onClose}
        className={cn(
          'fixed inset-0 z-40 bg-black/20 transition-opacity',
          open ? 'opacity-100 pointer-events-auto' : 'opacity-0 pointer-events-none',
        )}
      />
      <aside
        role="dialog"
        aria-modal="true"
        className={cn(
          'fixed right-0 top-0 z-50 h-full bg-surface shadow-xl border-l border-border transition-transform',
          open ? 'translate-x-0' : 'translate-x-full',
        )}
        style={{
          width: fullScreen ? '100vw' : (width ?? 'var(--drawer-w)'),
          maxWidth: fullScreen ? '100vw' : '92vw',
        }}
      >
        <div className="flex items-center justify-between h-12 border-b border-border px-4">
          <div className="font-medium text-[13px] truncate">{title}</div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close drawer"
            className="inline-flex size-7 items-center justify-center rounded-md text-foreground-muted hover:bg-surface-hover"
          >
            <X className="size-4" />
          </button>
        </div>
        <div className="h-[calc(100%-3rem)] overflow-auto">{children}</div>
      </aside>
    </>
  );
}
