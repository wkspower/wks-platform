import { X } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';

import { ApiError } from '@/api/client';
import { Button } from '@/components/ui/Button';
import { Skeleton } from '@/components/ui/Skeleton';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/Tabs';
import { useCase } from '@/hooks/useCases';
import { t } from '@/i18n';

import { ActivityTab } from './ActivityTab';
import { CaseActionBar } from './CaseActionBar';
import { CaseBreadcrumbs } from './CaseBreadcrumbs';
import { DocumentsTab } from './DocumentsTab';
import { PropertiesTab } from './PropertiesTab';
import { StageTimeline } from './StageTimeline';
import { StatusBadge } from './StatusBadge';
import { StatusTransitionControl } from './StatusTransitionControl';

const HEADING_ID = 'case-detail-heading';

export interface CaseDetailPanelProps {
  caseId: string;
  onClose: () => void;
}

function shorten(id: string): string {
  return id.length > 8 ? id.slice(-8) : id;
}

function CloseButton({ onClose }: { onClose: () => void }) {
  return (
    <button
      type="button"
      aria-label={t('case.close')}
      onClick={onClose}
      className="ml-auto inline-flex size-9 items-center justify-center rounded-[var(--radius-md)] hover:bg-[var(--muted)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--ring)]"
    >
      <X aria-hidden className="size-4" />
    </button>
  );
}

function HeadingFocusable({
  children,
  shouldFocus,
}: {
  children: React.ReactNode;
  /**
   * Focus the heading only the first time it mounts for a given caseId. Subsequent
   * re-renders (refetch-on-focus, retry → success transitions, tab switches) must NOT
   * yank focus from wherever the user has moved it — e.g., from the Retry button they
   * just clicked.
   */
  shouldFocus: boolean;
}) {
  const ref = useRef<HTMLHeadingElement>(null);
  useEffect(() => {
    if (!shouldFocus) return;
    const handle = requestAnimationFrame(() => ref.current?.focus());
    return () => cancelAnimationFrame(handle);
  }, [shouldFocus]);
  return (
    <h1
      id={HEADING_ID}
      ref={ref}
      tabIndex={-1}
      className="text-lg font-semibold focus-visible:outline-none"
    >
      {children}
    </h1>
  );
}

function LoadingSkeleton() {
  return (
    <div aria-hidden className="flex flex-col gap-3 p-4">
      <Skeleton className="h-6 w-1/3" />
      <div className="flex gap-2">
        {Array.from({ length: 3 }).map((_, i) => (
          <Skeleton key={i} className="h-6 w-20" />
        ))}
      </div>
      {Array.from({ length: 8 }).map((_, i) => (
        <div key={i} className="grid grid-cols-[120px_1fr] gap-3">
          <Skeleton className="h-4" />
          <Skeleton className="h-4" />
        </div>
      ))}
    </div>
  );
}

function PanelShell({
  caseIdShort,
  onClose,
  children,
  ariaLabelledBy = HEADING_ID,
}: {
  caseIdShort: string;
  onClose: () => void;
  children: React.ReactNode;
  ariaLabelledBy?: string;
}) {
  return (
    <section
      role="region"
      aria-labelledby={ariaLabelledBy}
      className="flex flex-1 min-w-[500px] flex-col"
    >
      <header className="flex flex-col gap-1 border-b border-[var(--border)] p-4">
        <div className="flex items-center">
          <CaseBreadcrumbs caseIdShort={caseIdShort} />
          <CloseButton onClose={onClose} />
        </div>
        {children}
      </header>
    </section>
  );
}

export function CaseDetailPanel({ caseId, onClose }: CaseDetailPanelProps) {
  const query = useCase(caseId);
  const [tab, setTab] = useState('properties');
  const idShort = shorten(caseId);
  // Track whether we've already focused the heading for this caseId. Reset on caseId change so
  // J/K stepping to a different case does grab focus, but a refetch/retry of the SAME case does
  // not.
  const focusedForCaseRef = useRef<string | null>(null);
  const shouldFocusHeading = focusedForCaseRef.current !== caseId;
  useEffect(() => {
    focusedForCaseRef.current = caseId;
  }, [caseId]);

  if (query.isLoading) {
    return (
      <section
        role="region"
        aria-label={t('case.heading', { idShort })}
        className="flex flex-1 min-w-[500px] flex-col"
      >
        <LoadingSkeleton />
      </section>
    );
  }

  if (query.isError) {
    const apiErr = query.error instanceof ApiError ? query.error : null;
    if (apiErr?.status === 404) {
      return (
        <PanelShell caseIdShort={idShort} onClose={onClose}>
          <HeadingFocusable shouldFocus={shouldFocusHeading}>
            {t('case.notFound.title')}
          </HeadingFocusable>
          <p className="mt-1 text-sm text-[var(--muted-foreground)]">{t('case.notFound.body')}</p>
          <Link
            to="/cases"
            className="mt-3 inline-flex w-fit text-sm text-[var(--primary)] hover:underline"
          >
            {t('case.notFound.backToList')}
          </Link>
        </PanelShell>
      );
    }
    if (apiErr?.status === 403) {
      return (
        <PanelShell caseIdShort={idShort} onClose={onClose}>
          <HeadingFocusable shouldFocus={shouldFocusHeading}>
            {t('case.forbidden.title')}
          </HeadingFocusable>
          <p className="mt-1 text-sm text-[var(--muted-foreground)]">{t('case.forbidden.body')}</p>
          <Link
            to="/cases"
            className="mt-3 inline-flex w-fit text-sm text-[var(--primary)] hover:underline"
          >
            {t('case.forbidden.backToList')}
          </Link>
        </PanelShell>
      );
    }
    return (
      <PanelShell caseIdShort={idShort} onClose={onClose}>
        <HeadingFocusable shouldFocus={shouldFocusHeading}>
          {t('case.error.title')}
        </HeadingFocusable>
        <p className="mt-1 text-sm text-[var(--muted-foreground)]">{t('case.error.body')}</p>
        <div className="mt-3">
          <Button variant="outline" size="sm" onClick={() => query.refetch()}>
            {t('common.retry')}
          </Button>
        </div>
      </PanelShell>
    );
  }

  const caseDto = query.data;
  if (!caseDto) {
    return null;
  }

  return (
    <section
      role="region"
      aria-labelledby={HEADING_ID}
      className="flex flex-1 min-w-[500px] flex-col"
    >
      <header className="flex flex-col gap-1 border-b border-[var(--border)] p-4">
        <div className="flex items-center">
          <CaseBreadcrumbs caseIdShort={idShort} />
          <CloseButton onClose={onClose} />
        </div>
        <div className="flex items-center gap-2">
          <HeadingFocusable shouldFocus={shouldFocusHeading}>
            {t('case.heading', { idShort })}
          </HeadingFocusable>
          <StatusBadge status={caseDto.status} caseType={caseDto.caseType} />
          <StatusTransitionControl caseDto={caseDto} />
        </div>
        {/*
          Story 3.3 — Stage timeline. Single truthy-length gate (AC2 / Decision 19): the absence
          of stages is the empty list, no `else` branch. `caseType.stages` is optional in TS for
          backward-compat with pre-3.3 fixtures; the runtime read is wire-driven.
        */}
        {(caseDto.caseType.stages?.length ?? 0) > 0 && caseDto.stages.length > 0 && (
          <div className="mt-3">
            <StageTimeline stages={caseDto.stages} caseTypeStageDefs={caseDto.caseType.stages} />
          </div>
        )}
        <span className="sr-only" aria-live="polite">
          {t('case.detail.announcement', { idShort })}
        </span>
      </header>
      <CaseActionBar caseId={caseId} caseTypeId={caseDto.caseTypeId} />
      <Tabs value={tab} onValueChange={setTab} className="flex flex-1 flex-col">
        <TabsList className="px-4">
          <TabsTrigger value="activity">{t('tabs.activity')}</TabsTrigger>
          <TabsTrigger value="properties">{t('tabs.properties')}</TabsTrigger>
          <TabsTrigger value="documents">{t('tabs.documents')}</TabsTrigger>
        </TabsList>
        <div className="flex-1 overflow-y-auto px-4 pb-4">
          <TabsContent value="activity">
            <ActivityTab caseId={caseId} />
          </TabsContent>
          <TabsContent value="properties">
            <PropertiesTab caseDto={caseDto} caseTypeView={caseDto.caseType} />
          </TabsContent>
          <TabsContent value="documents">
            <DocumentsTab caseId={caseId} />
          </TabsContent>
        </div>
      </Tabs>
    </section>
  );
}
