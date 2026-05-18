import { Dialog, DialogContent } from '@/components/ui/Dialog';

interface Binding {
  keys: string[];
  description: string;
}

const BINDINGS: Binding[] = [
  { keys: ['J', '↓'], description: 'Next case' },
  { keys: ['K', '↑'], description: 'Previous case' },
  { keys: ['Enter'], description: 'Open focused case' },
  { keys: ['E'], description: 'Edit focused row status' },
  { keys: ['X'], description: 'Toggle row selection' },
  { keys: ['Shift', 'X'], description: 'Range-select from last selection' },
  { keys: ['/'], description: 'Focus search' },
  { keys: ['Esc'], description: 'Close drawer or clear selection' },
  { keys: ['?'], description: 'Show this overlay' },
  { keys: ['⌘', 'K'], description: 'Open command palette' },
];

export function ShortcutsOverlay({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent title="Keyboard shortcuts" description="Available when no input has focus.">
        <ul className="space-y-2">
          {BINDINGS.map((b) => (
            <li key={b.description} className="flex items-center justify-between gap-4 text-[13px]">
              <span className="text-foreground-muted">{b.description}</span>
              <span className="flex gap-1">
                {b.keys.map((k) => (
                  <kbd
                    key={k}
                    className="rounded border border-border bg-background px-1.5 py-0.5 font-mono text-[11px]"
                  >
                    {k}
                  </kbd>
                ))}
              </span>
            </li>
          ))}
        </ul>
      </DialogContent>
    </Dialog>
  );
}
