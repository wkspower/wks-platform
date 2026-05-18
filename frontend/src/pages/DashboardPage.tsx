import { FileText, Inbox, LayoutGrid, Shield } from 'lucide-react';
import { Link } from 'react-router-dom';

import { Spinner } from '@/components/ui/Spinner';
import { useAllTasks } from '@/hooks/useTasks';
import { useCases } from '@/hooks/useCases';
import { useCaseTypes } from '@/hooks/useCaseTypes';
import { useAuthStore } from '@/stores/authStore';

export function DashboardPage() {
  const user = useAuthStore((s) => s.user);
  const { data: types, isLoading: tLoad } = useCaseTypes();
  const typeIds = (types ?? []).map((t) => t.id);
  const { data: rows, isLoading: cLoad } = useCases({ caseTypeIds: typeIds });
  const { data: tasks, isLoading: kLoad } = useAllTasks();

  return (
    <div className="px-6 py-6 max-w-5xl">
      <h1 className="font-heading text-2xl font-semibold">
        Hi {user?.email?.split('@')[0] ?? 'there'}, welcome back.
      </h1>
      <p className="text-foreground-muted text-[13px] mt-0.5">
        Here's a snapshot of your workspace.
      </p>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 mt-5">
        <Stat label="Cases" Icon={FileText} value={rows.length} loading={cLoad} to="/cases" />
        <Stat label="My tasks" Icon={Inbox} value={tasks?.items.length ?? 0} loading={kLoad} to="/tasks" />
        <Stat label="Case types" Icon={LayoutGrid} value={types?.length ?? 0} loading={tLoad} to="/admin" />
        <Stat label="License" Icon={Shield} value={user?.roles.includes('admin') ? '—' : 'OK'} loading={false} to="/admin/license" />
      </div>

      <div className="mt-8 grid lg:grid-cols-2 gap-4">
        <section className="rounded-lg border border-border bg-canvas p-4">
          <h3 className="text-[13px] font-semibold mb-2">Quick actions</h3>
          <ul className="space-y-1.5 text-[13px]">
            <li><Link to="/cases" className="text-[var(--primary)] hover:underline">Browse cases</Link></li>
            <li><Link to="/tasks" className="text-[var(--primary)] hover:underline">Work on tasks</Link></li>
            {user?.roles.includes('admin') && (
              <li><Link to="/admin" className="text-[var(--primary)] hover:underline">Admin panel</Link></li>
            )}
          </ul>
        </section>
        <section className="rounded-lg border border-border bg-canvas p-4">
          <h3 className="text-[13px] font-semibold mb-2">Tips</h3>
          <ul className="space-y-1.5 text-[12px] text-foreground-muted">
            <li>Press <kbd className="rounded border border-border bg-surface-hover px-1">⌘K</kbd> to jump anywhere.</li>
            <li>Use case-type chips to filter the list.</li>
            <li>Drop files on a case's Documents tab to upload.</li>
          </ul>
        </section>
      </div>
    </div>
  );
}

function Stat({
  label, Icon, value, loading, to,
}: { label: string; Icon: typeof FileText; value: number | string; loading: boolean; to: string }) {
  return (
    <Link
      to={to}
      className="block rounded-lg border border-border bg-canvas p-3 hover:border-border-strong transition-colors"
    >
      <div className="flex items-center gap-1.5 text-[11px] uppercase tracking-wider text-foreground-subtle font-medium">
        <Icon className="size-3" /> {label}
      </div>
      <div className="mt-1.5 text-2xl font-heading font-semibold">
        {loading ? <Spinner /> : value}
      </div>
    </Link>
  );
}
