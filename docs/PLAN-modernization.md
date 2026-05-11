# wks-docs modernization plan

Outside sprint/epic tracking. Five independent tracks, each one PR off `v2-develop`.

**Stack:** fumadocs 15 / Next.js 15 / Tailwind. Source: `content/docs/**/*.mdx`. Nav: `meta.json` per folder.

**Strategic role:** activation funnel between wks-website (conversion) and a running local instance. Optimizes for SI-dev evaluation and SI-architect "coming from Camunda."

## Conventions

- Branch per track: `docs/<N>-<slug>` off `v2-develop`.
- Worktree per track: `../wks-platform-docs-<N>-<slug>/`.
- PR title prefix: `[docs]` — keeps these visually distinct from Sprint PRs.
- No Flyway, no ErrorCode, no backend, no sprint-status entry, no epic linkage.
- All paths below are relative to `wks-platform/docs/`.

---

## DOCS-1 — Brand bridge

**Goal:** visual continuity with `wks-website`. After this, every doc page inherits the right tokens for free.

**Surface:**
- `app/layout.tsx` — wrap `RootProvider` with CSS-vars override mirroring `wks-website/src/styles/global.css` (`--color-primary`, `--color-brand-navy`, `--color-brand-cyan`, Poppins/Rubik/JetBrains Mono).
- `public/fonts/` — port the four woff2 files from `wks-website/public/fonts/`.
- `app/docs/layout.tsx` — replace `nav={{ title: 'WKS Platform' }}` with a custom navbar component mirroring `wks-website` `StageNav` links (Overview / Demo / Proof / Product / Pricing) targeting `https://wkspower.com/*`, plus GitHub.
- `components/DocsFooter.tsx` (new) — React port of `wks-website/src/components/AuditStrip.astro`.

**ACs:**
1. Theme tokens visible: primary buttons/links render `#3B5BDB`; headings use Poppins.
2. Navbar shows StageNav links pointing to product site; GitHub link present.
3. Footer renders on every docs page.
4. Lighthouse a11y score ≥ 95 (the product site's bar).
5. No content changes — only chrome.

**Est:** ~150 LOC, one session.

---

## DOCS-2 — Cases-first concept page

**Goal:** unblock the mental-model entry point. This is the principle inversion (locked 2026-04-27) and the single most strategically important concept page.

**Surface:**
- `content/docs/concepts/cases-first-processes-optional.mdx` — replace stub with full page.

**Source:** `wks-platform2/_bmad-output/planning-artifacts/CONCEPTS.md §principle inversion`. Voice-rules pass only — content already comprehensive.

**ACs:**
1. Page opens with the inversion stated in one sentence.
2. Side-by-side diagram or table: "Camunda mental model" vs "WKS mental model."
3. Concrete walk-through: zero-stage case → add stages → attach BPMN — each step framed as additive.
4. Closes with link to `/docs/start/your-first-case-type` and `/docs/concepts/coming-from-camunda`.

**Est:** content-only, one session.

---

## DOCS-3 — YAML schema reference

**Goal:** SIs cannot evaluate without a complete reference page. Handwritten now; auto-generation later.

**Surface:**
- `content/docs/reference/yaml-schema.mdx` — replace stub.

**ACs:**
1. Every top-level YAML key documented: `id`, `displayName`, `version`, `description`, `roles`, `stages`, `forms`, `statuses`, `workflows`, `attachments`, `mapping`.
2. For each key: type, required/optional, default, validator citation (file:line in `wks-platform`), and a minimal example.
3. One complete example case-type YAML at the bottom exercising every key.
4. Cross-links to validator error codes in `reference/error-codes.mdx`.

**Est:** content-only, one session. Citations require grep against `wks-platform/backend/` validators.

---

## DOCS-4 — Deploy-to-production ops page

**Goal:** turn "I demoed it" into "I shipped it for a client." Required for the Managed Hosting tier to be credible.

**Surface:**
- `content/docs/operations/deploy-to-production.mdx` — replace stub.
- `content/docs/reference/environment-variables.mdx` — populate (currently stub).

**ACs:**
1. docker-compose example with Postgres + MinIO + production profile.
2. Required env vars table — sourced from `wks-platform/backend/src/main/resources/application-production.yml` and Spring `@ConfigurationProperties` classes.
3. `wks.bootstrap.production-validation.enabled` switch documented (per memory `project_postgres_it_parity_gap.md` family).
4. Smoke check: `curl /api/health` + Postgres migration count assertion.
5. Two-switch Keycloak seam (`WKS_KEYCLOAK_ENABLED` + `--profile production-sso`) documented per Story 14.1 milestone.

**Est:** content-only, one session.

---

## DOCS-5 — IA cleanup + landing page

**Goal:** fix navigation defects + replace the bare redirect with a persona router.

**Surface:**
- `content/docs/start/meta.json` — drop `demo-to-a-client`, add `coming-from-camunda` (move file from `concepts/`).
- `content/docs/concepts/meta.json` — remove `coming-from-camunda`.
- `content/docs/meta.json` — reorder to `start, concepts, guides, operations, reference`.
- `content/docs/guides/meta.json` — add `demo-to-a-client` here.
- `app/page.tsx` — replace `redirect('/docs')` with a real landing page using DOCS-1 design tokens.
- New: `app/components/PersonaCard.tsx`.

**Landing page personas (four cards):**
- "New to WKS" → `/docs/start/run-wks-in-5-minutes`
- "Coming from Camunda" → `/docs/start/coming-from-camunda`
- "Going to production" → `/docs/operations/deploy-to-production`
- "API & schema reference" → `/docs/reference/yaml-schema`

**ACs:**
1. `/` renders persona-router, not a redirect.
2. All internal links resolve (no 404s after `meta.json` moves).
3. Visual identity matches DOCS-1.
4. Mobile-responsive (test ≤ 640px).

**Depends on:** DOCS-1 merged (uses its tokens). Ideally DOCS-2/3/4 merged too so the landing's "Going to production" and "API reference" links don't land on stubs — but not strictly required.

**Est:** ~200 LOC, one session.

---

## Suggested order

1. **DOCS-1** first — unlocks visual identity for everything else.
2. **DOCS-2 / DOCS-3 / DOCS-4** in any order (or parallel worktrees if desired) — pure content, no shared files.
3. **DOCS-5** last — wants the design tokens and ideally the populated reference pages.

## What's intentionally deferred

- Filling the other 18 stub pages — fold into next-touching backend story per existing memory rule.
- Search config / "edit on GitHub" / feedback widget.
- Error-code auto-generation (spec exists at `wks-platform2/docs/error-codes-automation-spec.md` — defer).
- Versioned docs (per-release branches) — not needed before public launch.
