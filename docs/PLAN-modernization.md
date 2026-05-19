# wks-docs plan

The docs site (`docs/`) is Fumadocs 15 / Next.js 15 / Tailwind. Source: `content/docs/**/*.mdx`. Nav: `meta.json` per folder.

Goal: docs are the activation funnel between the marketing site and a running local instance. Optimize for SI evaluation ("can I stand this up and try it?") and SI-architect onboarding ("how do I think about this if I come from Camunda?").

## What exists

- **Start** — `run-wks-in-5-minutes`, `your-first-case-type`, `coming-from-camunda`.
- **Concepts** — `cases-first-processes-optional` (the load-bearing mental-model page).
- **Guides** — `troubleshoot-deployment-errors`.
- **Operations** — `deploy-to-production`, `security`.
- **Reference** — `yaml-schema`, `environment-variables`.

## Working principle

Add a doc page when a feature ships that needs it. Don't pre-create stubs to occupy nav slots — empty `meta.json` entries imply unfinished surface and rot.

When you build something user-visible:

1. If a guide for it would have helped someone evaluating WKS, write the guide in the same PR.
2. Register it in the relevant `meta.json`.
3. If it's a one-paragraph "how do I do X" answer, prefer a `## section` inside an existing guide over a new file.

Auto-generated reference (API surface, full error-code per-page detail) is deferred — the `springdoc-openapi` Swagger UI at `/swagger-ui` already answers the "what endpoints exist" question for now.

## Branching

Docs PRs branch off `v2-develop` like everything else. Prefix the PR title with `[docs]` so they're easy to scan. No separate worktree convention — `git checkout -b docs/<slug>` is enough.
