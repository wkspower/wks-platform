import { ArrowLeft } from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';

import { CaseDetailBody } from '@/components/cases/CaseDetailBody';
import { Button } from '@/components/ui/Button';
import { Spinner } from '@/components/ui/Spinner';
import { useCase } from '@/hooks/useCases';

/**
 * Full-page case detail — kept as a deep-link fallback at `/cases/:caseId/full`. The drawer-mode
 * variant at `/cases/:caseId` is hosted by `CasesPage`.
 */
export function CaseDetailPage() {
  const { caseId } = useParams<{ caseId: string }>();
  const navigate = useNavigate();
  const { data: dto, isLoading, isError } = useCase(caseId ?? null);

  if (isLoading) {
    return (
      <div className="grid place-items-center py-20">
        <Spinner className="size-6" />
      </div>
    );
  }
  if (isError || !dto) {
    return <div className="px-6 py-12 text-center text-[var(--destructive)]">Failed to load case.</div>;
  }

  return (
    <div className="min-h-full">
      <div className="px-6 pt-4">
        <Button variant="ghost" size="xs" onClick={() => navigate('/cases')}>
          <ArrowLeft className="size-3.5" /> Cases
        </Button>
      </div>
      <CaseDetailBody dto={dto} />
    </div>
  );
}
