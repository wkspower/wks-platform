# Contributing to WKS Platform

WKS Platform is open-source case management for system integrators —
YAML case types, BPMN lifecycles, 30-minute evaluation. Thanks for
considering a contribution.

## Is the project accepting contributions?

Yes, with a named scope:

- **Bug reports** — open a GitHub issue with a reproduction.
- **Typo and documentation fixes** — PR directly.
- **Focused code contributions** against the published roadmap items.

v2 is pre-1.0 and spec-driven. The public
[`ROADMAP.md`](./ROADMAP.md) lists what is in flight and what comes
next. If you want to contribute a feature, open an issue first so it
can be sized and slotted against the roadmap. PRs adding features
outside the roadmap are not accepted pre-1.0.

## How do I set up?

Follow the [README Quick start](./README.md#quick-start) — `docker
compose up --build` from `docker/`, then
`http://localhost:8080/`. Before opening a PR, run the local CI-parity
sequence, which mirrors [`.github/workflows/ci.yml`](./.github/workflows/ci.yml):

```bash
# Backend (Temurin 21)
cd backend && ./mvnw -B -ntp verify

# Frontend (Node 22)
cd frontend && npm ci --no-audit --no-fund
npm run lint && npm run format:check && npm test && npm run build

# Docker image build (same args as CI)
docker buildx build -f docker/Dockerfile -t wks-platform:local .
```

Copy these commands verbatim — paraphrasing causes local-vs-CI drift.

## What's the branching model?

- `v2-develop` is the **v2 trunk**. All v2 PRs target `v2-develop`.
- `develop` is frozen at `v1-final` and does not accept v2 work.
- Feature branches follow a `topic/short-slug` shape, e.g.
  `fix/login-redirect-loop` or `feat/case-list-empty-state`.
- **Never** target `develop` or `main` with a v2 PR.

## What's the commit style?

[Conventional Commits](https://www.conventionalcommits.org/): `feat(scope):`,
`fix(scope):`, `chore:`, `docs(scope):`, `test(scope):`. One concern per
commit. Squash-on-merge is used, so the pre-squash history is still
reviewed and should read cleanly.

PR description should include:

- **Summary** — what changed and why, in plain language.
- **Test plan** — what you ran locally (`mvnw verify`, `npm test`,
  `docker compose up` smoke), and what a reviewer should re-run to be
  confident.
- **Screenshots / output** — for UI changes or observable behaviour
  changes.

## Code of conduct and security policy

See [`CODE_OF_CONDUCT.md`](./CODE_OF_CONDUCT.md) — all contributors are
expected to follow it. See [`SECURITY.md`](./SECURITY.md) for supported
versions, response times, and scope.

## Where do I report a security issue?

**Do NOT open a public GitHub issue.** Follow [`SECURITY.md`](./SECURITY.md) —
email `security@wkspower.com` with the details. Public issues leak the
vulnerability before a fix is available.
