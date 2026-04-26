import { describe, expect, it } from 'vitest';

import { router } from './routes';

/**
 * Story 2.6 — `/cases/:caseId` was added so the split-pane workspace can be deep-linked. Lock
 * the route registration here so the next refactor of `routes.tsx` (or a stray collision with
 * the `*` catch-all) doesn't silently regress deep-linking back into the 404 page.
 */
describe('app router — case workspace deep-linking', () => {
  it('registers /cases as a top-level route', () => {
    const match = router.routes.flatMap((r) => r.children ?? []).flatMap((r) => r.children ?? [r]);
    const paths = match.map((r) => r.path).filter(Boolean);
    expect(paths).toContain('/cases');
  });

  it('registers /cases/:caseId so deep links land on CasesPage instead of the 404 catch-all', () => {
    const match = router.routes.flatMap((r) => r.children ?? []).flatMap((r) => r.children ?? [r]);
    const paths = match.map((r) => r.path).filter(Boolean);
    expect(paths).toContain('/cases/:caseId');
  });

  it('keeps the `*` catch-all as the last sibling of the authenticated tree so /cases/:caseId is matched first', () => {
    const authedRoute = router.routes.find((r) =>
      (r.children ?? []).some((c) => c.path === '/cases/:caseId'),
    );
    expect(authedRoute).toBeDefined();
    const childPaths = (authedRoute?.children ?? []).map((c) => c.path);
    const detailIdx = childPaths.indexOf('/cases/:caseId');
    const catchAllIdx = childPaths.indexOf('*');
    expect(detailIdx).toBeGreaterThanOrEqual(0);
    expect(catchAllIdx).toBeGreaterThan(detailIdx);
  });
});
