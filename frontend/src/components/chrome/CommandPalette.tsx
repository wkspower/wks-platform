import { Command } from 'cmdk';
import {
  FileText,
  Inbox,
  LayoutGrid,
  LogOut,
  Search,
  Settings,
  Shield,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

import { Dialog, DialogContent } from '@/components/ui/Dialog';
import { useAuthStore } from '@/stores/authStore';

type Cmd = { id: string; label: string; group: string; Icon: typeof FileText; run: () => void; role?: string };

export function CommandPalette({ open, onOpenChange }: { open: boolean; onOpenChange: (v: boolean) => void }) {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);

  const close = () => onOpenChange(false);

  const commands: Cmd[] = [
    { id: 'nav-dashboard', label: 'Go to Dashboard', group: 'Navigate', Icon: LayoutGrid, run: () => navigate('/dashboard') },
    { id: 'nav-cases', label: 'Go to Cases', group: 'Navigate', Icon: FileText, run: () => navigate('/cases') },
    { id: 'nav-tasks', label: 'Go to My Tasks', group: 'Navigate', Icon: Inbox, run: () => navigate('/tasks') },
    { id: 'nav-admin', label: 'Go to Admin', group: 'Navigate', Icon: Settings, run: () => navigate('/admin'), role: 'admin' },
    { id: 'nav-license', label: 'View License Status', group: 'Admin', Icon: Shield, run: () => navigate('/admin/license'), role: 'admin' },
    { id: 'logout', label: 'Sign out', group: 'Account', Icon: LogOut, run: () => void logout() },
  ].filter((c) => !c.role || user?.roles.includes(c.role));

  const grouped = commands.reduce<Record<string, Cmd[]>>((acc, c) => {
    (acc[c.group] ||= []).push(c);
    return acc;
  }, {});

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="p-0 max-w-xl overflow-hidden">
        <Command label="Command palette" className="bg-surface">
          <div className="flex items-center gap-2 border-b border-border px-3">
            <Search className="size-4 text-foreground-subtle" />
            <Command.Input
              autoFocus
              placeholder="Type a command or search…"
              className="h-11 w-full bg-transparent text-[13px] placeholder:text-foreground-subtle focus:outline-none"
            />
          </div>
          <Command.List className="max-h-80 overflow-auto p-2">
            <Command.Empty className="px-3 py-6 text-center text-[12px] text-foreground-muted">
              No results
            </Command.Empty>
            {Object.entries(grouped).map(([group, cmds]) => (
              <Command.Group
                key={group}
                heading={group}
                className="[&_[cmdk-group-heading]]:px-2 [&_[cmdk-group-heading]]:py-1 [&_[cmdk-group-heading]]:text-[11px] [&_[cmdk-group-heading]]:font-medium [&_[cmdk-group-heading]]:text-foreground-subtle [&_[cmdk-group-heading]]:uppercase [&_[cmdk-group-heading]]:tracking-wider"
              >
                {cmds.map(({ id, label, Icon, run }) => (
                  <Command.Item
                    key={id}
                    value={label}
                    onSelect={() => {
                      run();
                      close();
                    }}
                    className="flex items-center gap-2 rounded-md px-2 py-2 text-[13px] data-[selected=true]:bg-surface-hover cursor-pointer"
                  >
                    <Icon className="size-4 text-foreground-muted" />
                    <span>{label}</span>
                  </Command.Item>
                ))}
              </Command.Group>
            ))}
          </Command.List>
        </Command>
      </DialogContent>
    </Dialog>
  );
}
