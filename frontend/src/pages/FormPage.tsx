import { useNavigate, useParams } from 'react-router-dom';

import { MultiSectionFormRenderer } from '@/components/forms/MultiSectionFormRenderer';
import { SinglePageFormRenderer } from '@/components/forms/SinglePageFormRenderer';
import { Button } from '@/components/ui/Button';
import { useCase } from '@/hooks/useCases';
import { t } from '@/i18n';

/**
 * Story 5.2 — Route-accessible form page. Fetches the case (which embeds the case-type view,
 * including any declared {@code forms[]}), resolves the form by id from the path, and renders
 * {@link SinglePageFormRenderer}.
 *
 * <p>Route: {@code /cases/:caseId/forms/:formId}
 *
 * <p>The case DTO embeds the case-type view in one round-trip (architecture.md §Decision 12) so no
 * separate {@code GET /api/case-types/{id}} call is needed here.
 */
export function FormPage() {
  const { caseId, formId } = useParams<{ caseId: string; formId: string }>();
  const navigate = useNavigate();

  const caseQuery = useCase(caseId ?? null);

  if (caseQuery.isPending) {
    return (
      <div className="flex items-center justify-center p-8 text-sm text-[var(--muted-foreground)]">
        {t('form.loading')}
      </div>
    );
  }

  if (caseQuery.isError || !caseQuery.data) {
    return (
      <div className="flex items-center justify-center p-8 text-sm text-[var(--destructive)]">
        {t('form.error.caseLoad')}
      </div>
    );
  }

  const caseDto = caseQuery.data;
  const formDef = (caseDto.caseType.forms ?? []).find((f) => f.id === formId);

  // CF2 fix — invalid formId shows an error state with navigation link, not floating text
  if (!formDef) {
    return (
      <div className="mx-auto max-w-2xl p-6">
        <p className="mb-4 text-sm text-[var(--destructive)]">{t('form.error.notFound')}</p>
        <Button variant="ghost" onClick={() => navigate(`/cases/${caseId}`)}>
          {t('form.error.backToCase')}
        </Button>
      </div>
    );
  }

  // Renderer dispatch — route to MultiSectionFormRenderer for multi-section rendering
  const renderer =
    formDef.rendering === 'multi-section' ? (
      <MultiSectionFormRenderer
        formDefinition={formDef}
        caseId={caseId!}
        defaultValues={caseDto.data}
        onSuccess={() => navigate(`/cases/${caseId}`)}
      />
    ) : (
      <SinglePageFormRenderer
        formDefinition={formDef}
        caseId={caseId!}
        defaultValues={caseDto.data}
        onSuccess={() => navigate(`/cases/${caseId}`)}
      />
    );

  return (
    <div className="mx-auto max-w-2xl p-6">
      <h1 className="mb-6 text-xl font-semibold">{formDef.id}</h1>
      {renderer}
    </div>
  );
}
