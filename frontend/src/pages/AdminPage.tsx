import { ChevronRight, FileCog, Shield, Workflow } from 'lucide-react';
import { Link } from 'react-router-dom';

import { Badge } from '@/components/ui/Badge';
import { Spinner } from '@/components/ui/Spinner';
import { useCaseTypes } from '@/hooks/useCaseTypes';

export function AdminPage() {
  const { data: types, isLoading } = useCaseTypes();

  return (
    <div className="px-6 py-6 max-w-5xl">
      <h1 className="font-heading text-2xl font-semibold">Admin</h1>
      <p className="text-foreground-muted text-[13px] mt-0.5">Manage case types, deployments, and license.</p>

      <div className="grid lg:grid-cols-3 gap-3 mt-5">
        <Card to="/admin/license" Icon={Shield} title="License">
          View status, tier, and feature gates.
        </Card>
        <Card href="/swagger-ui/index.html" external Icon={FileCog} title="API docs">
          OpenAPI docs (springdoc) for backend endpoints.
        </Card>
        <Card to="/admin" Icon={Workflow} title="BPMN deploy" disabled>
          Multipart deploy of case-type YAML + BPMN — coming soon.
        </Card>
      </div>

      <h2 className="mt-8 mb-2 text-[11px] uppercase tracking-wider text-foreground-subtle font-medium">
        Deployed case types
      </h2>
      <div className="rounded-lg border border-border bg-canvas overflow-hidden">
        {isLoading ? (
          <div className="grid place-items-center py-10">
            <Spinner />
          </div>
        ) : !types || types.length === 0 ? (
          <p className="px-4 py-8 text-center text-[13px] text-foreground-muted">No case types deployed.</p>
        ) : (
          <ul className="divide-y divide-divider">
            {types.map((t) => (
              <li
                key={t.id}
                className="flex items-center justify-between gap-3 px-4 py-3 hover:bg-surface-hover"
              >
                <div>
                  <div className="font-medium text-[13px]">{t.displayName}</div>
                  <div className="text-[11px] text-foreground-muted mt-0.5 flex items-center gap-1.5">
                    <span className="font-mono">{t.id}</span>
                    <Badge>v{t.version}</Badge>
                    <span>
                      · {t.statusCount} statuses · {t.fieldCount} fields
                    </span>
                  </div>
                </div>
                <Link
                  to={`/admin/mapping-inspector/${encodeURIComponent(t.id)}`}
                  className="inline-flex items-center gap-1 text-[12px] text-[var(--primary)] hover:underline"
                >
                  Mapping inspector <ChevronRight className="size-3" />
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

function Card({
  to,
  href,
  external,
  Icon,
  title,
  children,
  disabled,
}: {
  to?: string;
  href?: string;
  external?: boolean;
  Icon: typeof Shield;
  title: string;
  children: React.ReactNode;
  disabled?: boolean;
}) {
  const cls =
    'block rounded-lg border border-border bg-canvas p-4 hover:border-border-strong transition-colors';
  const inner = (
    <>
      <Icon className="size-4 text-[var(--primary)]" />
      <div className="mt-1.5 font-semibold text-[13px]">{title}</div>
      <p className="text-[12px] text-foreground-muted mt-0.5">{children}</p>
    </>
  );
  if (disabled) return <div className={`${cls} opacity-60 pointer-events-none`}>{inner}</div>;
  if (href)
    return (
      <a
        className={cls}
        href={href}
        target={external ? '_blank' : undefined}
        rel={external ? 'noreferrer' : undefined}
      >
        {inner}
      </a>
    );
  return (
    <Link to={to ?? '#'} className={cls}>
      {inner}
    </Link>
  );
}
