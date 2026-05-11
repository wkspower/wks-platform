import { useNavigate, useParams } from 'react-router-dom';

import { MultiSectionFormRenderer } from '@/components/forms/MultiSectionFormRenderer';
import { SinglePageFormRenderer } from '@/components/forms/SinglePageFormRenderer';
import { Alert } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { useCase } from '@/hooks/useCases';
import { useFormDraft } from '@/hooks/useFormDraft';
import { t } from '@/i18n';

/**
 * Story 5.2 — Route-accessible form page. Story 5.4 extends with draft-resume orchestration.
 *
 * <p>AC2 — when a draft exists for {@code (caseId, formId, currentUser)}, merge its {@code payload}
 * over the case's {@code data} so the user resumes from where they left off; render a small "Resumed
 * draft" banner with a Discard link.
 *
 * <p>AC3 — when the draft's {@code caseTypeVersionAtSave} differs from the case's current
 * {@code caseTypeVersion}, the renderer is NOT mounted; instead an inline {@code Alert} prompts the
 * user to discard the draft (and use the current form) or cancel back to the case.
 *
 * <p>Route: {@code /cases/:caseId/forms/:formId}
 */
export function FormPage() {
  const { caseId, formId } = useParams<{ caseId: string; formId: string }>();
  const navigate = useNavigate();

  const caseQuery = useCase(caseId ?? null);
  // Hook is always called (rules-of-hooks) — when caseId/formId are absent we still mount but the
  // GET will 404 fast and AC2/AC3 paths are bypassed by the early returns below.
  const draft = useFormDraft(caseId ?? '', formId ?? '', caseQuery.data?.caseTypeVersion ?? 0);

  if (caseQuery.isPending || draft.loading) {
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

  // AC3 — version mismatch gate. Renderer is NOT mounted with a stale-version draft;
  // discard-only policy (epics §5.4 AC3, no auto-rebase — Story 3.9 owns that surface).
  if (draft.isVersionMismatch) {
    return (
      <div className="mx-auto max-w-2xl p-6">
        <Alert role="alert" aria-live="polite" variant="warning">
          <strong className="block">{t('form.draft.versionChanged.title')}</strong>
          <span className="mt-1 block">{t('form.draft.versionChanged.body')}</span>
          <div className="mt-4 flex gap-2">
            <Button
              type="button"
              onClick={() => {
                void draft.discard().then(() => window.location.reload());
              }}
            >
              {t('form.draft.versionChanged.discardAndUseCurrent')}
            </Button>
            <Button type="button" variant="ghost" onClick={() => navigate(`/cases/${caseId}`)}>
              {t('form.draft.versionChanged.cancel')}
            </Button>
          </div>
        </Alert>
      </div>
    );
  }

  // AC2 — merge draft payload over caseDto.data (draft wins on key collision).
  const mergedDefaults: Record<string, unknown> | undefined = draft.draft
    ? { ...(caseDto.data ?? {}), ...draft.draft.payload }
    : caseDto.data;

  // Renderer dispatch — route to MultiSectionFormRenderer for multi-section rendering
  const renderer =
    formDef.rendering === 'multi-section' ? (
      <MultiSectionFormRenderer
        formDefinition={formDef}
        caseId={caseId!}
        defaultValues={mergedDefaults}
        onValuesChange={(values) =>
          draft.scheduleSave(values, typeof window !== 'undefined' ? window.scrollY : 0, null)
        }
        saveDraftAction={{
          label: t('form.draft.save'),
          onClick: (values) =>
            void draft.saveNow(values, typeof window !== 'undefined' ? window.scrollY : 0, null),
        }}
        onSuccess={() => navigate(`/cases/${caseId}`)}
      />
    ) : (
      <SinglePageFormRenderer
        formDefinition={formDef}
        caseId={caseId!}
        defaultValues={mergedDefaults}
        onValuesChange={(values) =>
          draft.scheduleSave(values, typeof window !== 'undefined' ? window.scrollY : 0, null)
        }
        saveDraftAction={{
          label: t('form.draft.save'),
          onClick: (values) =>
            void draft.saveNow(values, typeof window !== 'undefined' ? window.scrollY : 0, null),
        }}
        onSuccess={() => navigate(`/cases/${caseId}`)}
      />
    );

  return (
    <div className="mx-auto max-w-2xl p-6">
      <div className="mb-6 flex items-center gap-3">
        <h1 className="text-xl font-semibold">{formDef.id}</h1>
        {/* Story 5.5 AC-4 — version-pin chip. caseDto.caseTypeVersion is the pinned version
            (server now returns pinned CaseType, not latest — Decision D20). */}
        <span
          className="rounded-full bg-[var(--muted)] px-2 py-0.5 text-xs text-[var(--muted-foreground)]"
          aria-label={t('form.pinnedVersion', { version: String(caseDto.caseTypeVersion) })}
        >
          {t('form.pinnedVersion', { version: String(caseDto.caseTypeVersion) })}
        </span>
      </div>
      {draft.draft ? (
        <div
          role="status"
          aria-live="polite"
          className="mb-4 flex items-center gap-2 text-sm text-[var(--muted-foreground)]"
        >
          <span>{t('form.draft.resumed')}</span>
          <button
            type="button"
            className="underline"
            onClick={() => {
              void draft.discard().then(() => window.location.reload());
            }}
          >
            {t('form.draft.discard')}
          </button>
        </div>
      ) : null}
      {draft.saveState === 'saving' ? (
        <div className="mb-2 text-xs text-[var(--muted-foreground)]" aria-live="polite">
          {t('form.draft.saving')}
        </div>
      ) : null}
      {draft.saveState === 'saved' ? (
        <div className="mb-2 text-xs text-[var(--muted-foreground)]" aria-live="polite">
          {t('form.draft.saved')}
        </div>
      ) : null}
      {draft.saveState === 'error' ? (
        <div className="mb-2 text-xs text-[var(--destructive)]" aria-live="polite">
          {t('form.draft.error')}
        </div>
      ) : null}
      {renderer}
    </div>
  );
}
