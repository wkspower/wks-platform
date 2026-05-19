import { Hash } from 'lucide-react';
import { Link } from 'react-router-dom';

import { ActivityTab } from './ActivityTab';
import { DocumentsTab } from './DocumentsTab';
import { PropertiesTab } from './PropertiesTab';
import { StageTimeline } from './StageTimeline';
import { TasksTab } from './TasksTab';
import { Avatar } from '@/components/ui/Avatar';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/Tabs';
import { formatDateTime } from '@/lib/formatDate';
import type { CaseDto } from '@/types/case';

export function CaseDetailBody({ dto, compact = false }: { dto: CaseDto; compact?: boolean }) {
  const status = dto.caseType.statuses.find((s) => s.id === dto.status);

  return (
    <div className="min-h-full">
      <header className={`border-b border-border bg-canvas ${compact ? 'px-4 pt-3 pb-3' : 'px-6 pt-4 pb-3'}`}>
        <div className="flex items-start justify-between gap-4">
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <h1 className="font-heading text-[18px] font-semibold truncate">
                <Link
                  to={`/cases?caseType=${dto.caseTypeId}`}
                  className="text-foreground-muted hover:text-foreground"
                >
                  {dto.caseType.displayName}
                </Link>
                <span className="text-foreground-subtle mx-1.5">/</span>
                <span className="font-mono text-[14px]">{dto.id.slice(0, 8)}</span>
              </h1>
              <StatusBadge label={status?.displayName ?? dto.status} color={status?.color} />
            </div>
            <div className="mt-1.5 flex flex-wrap items-center gap-3 text-[12px] text-foreground-muted">
              <span className="inline-flex items-center gap-1">
                <Hash className="size-3" />
                <span className="font-mono">{dto.id}</span>
              </span>
              <span>Created {formatDateTime(dto.createdAt)}</span>
              {dto.createdBy && (
                <span className="inline-flex items-center gap-1">
                  by <Avatar name={dto.createdBy} size="xs" /> {dto.createdBy}
                </span>
              )}
              <span>v{dto.version}</span>
            </div>
          </div>
        </div>
        {dto.stages && dto.stages.length > 0 && (
          <div className="mt-3">
            <StageTimeline stages={dto.stages} />
          </div>
        )}
      </header>

      <Tabs defaultValue="properties" className="bg-canvas">
        <div className={compact ? 'px-4' : 'px-6'}>
          <TabsList>
            <TabsTrigger value="properties">Properties</TabsTrigger>
            <TabsTrigger value="tasks">Tasks</TabsTrigger>
            <TabsTrigger value="documents">
              Documents
              {dto.documentCount > 0 && (
                <span className="ml-1.5 rounded-full bg-surface-hover px-1.5 text-[10px]">
                  {dto.documentCount}
                </span>
              )}
            </TabsTrigger>
            <TabsTrigger value="activity">Activity</TabsTrigger>
          </TabsList>
        </div>
        <TabsContent value="properties">
          <PropertiesTab dto={dto} />
        </TabsContent>
        <TabsContent value="tasks">
          <TasksTab caseId={dto.id} />
        </TabsContent>
        <TabsContent value="documents">
          <DocumentsTab caseId={dto.id} />
        </TabsContent>
        <TabsContent value="activity">
          <ActivityTab dto={dto} />
        </TabsContent>
      </Tabs>
    </div>
  );
}
