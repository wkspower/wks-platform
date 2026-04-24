# WKS Platform Roadmap

A plain-English view of what has shipped, what is in flight, and what
comes next. Updated per milestone. For the architectural "why", see
[`docs/architecture.md`](./docs/architecture.md).

## Shipped

### Platform foundation

- **Project skeleton + zero-config boot** — `docker compose up --build`
  from `docker/` brings the full stack up on a fresh machine with no
  Java or Node installed.
- **Authentication + session management** — Argon2id password hashing,
  short-lived JWT in an `HttpOnly` cookie, first-boot admin seed in
  dev, mandatory admin credentials in production.
- **React application shell + design system** — single design-token
  file, Tailwind 4 `@theme inline`, self-hosted Poppins + Rubik fonts,
  placeholder routes for Cases, Tasks, Admin, Dev.
- **REST API foundation + database abstraction** — envelope / error
  conventions, H2 in dev, PostgreSQL in production via JPA + Flyway,
  Swagger UI at `/swagger-ui/index.html`, `/api/health` live.

## Next — case lifecycle and workspace

The first evaluable build. It makes WKS a real product a system
integrator can stand up and try end-to-end.

- YAML case-type configuration with validation.
- Embedded CIB seven BPMN engine behind the `WorkflowEngine` port;
  BPMN process deployment alongside YAML case types.
- Case CRUD and domain model.
- Case status transitions driven by BPMN.
- Config-driven case list view.
- Split-pane workspace and case detail screen.
- Case creation flow.
- Task completion with honest lifecycle semantics.

At the end of this milestone the README's 30-minute evaluation claim
is demonstrable: clone, `docker compose up`, deploy a sample case
type, create a case, move it through its lifecycle.

## Later

Grouped roughly in the order of shipping, not a commitment:

- **Documents** — document storage API, list and download inside case
  detail.
- **Activity feed, audit trail, and real-time events** — domain-event
  infrastructure, business-language activity feed, server-sent events
  for live updates, sidebar notification badges.
- **Users and roles** — user admin panel, role-based access per case
  type.
- **Developer console** — configuration workflow screen, case
  workspace bridge for previewing case types before rollout.
- **Templates and first-run experience** — template package format,
  BFSI example pack, guided launcher.
- **Tasks screen and orchestrative actions** — dedicated tasks view,
  escalate / reassign / add-note flows.
- **Production readiness** — production Docker profile, theming and
  branding, webhooks and SMTP notifications, document preview, live
  demo auto-deploy.
- **Platform evolution** — v1 → v2 transition path, upgrade and data
  export tooling.

## How to follow along

- **Star [`wkspower/wks-platform`](https://github.com/wkspower/wks-platform)** to
  follow releases.
- **Watch this file** — it moves entries from _Next_ to _Shipped_ as
  each milestone closes.
- **Watch the repository's Releases page** for milestone announcements.

## Pre-1.0 caveat

WKS Platform v2 is pre-1.0. Scope below _Later_ may shift as the
case-lifecycle milestone reveals what evaluators actually need first.
When that happens, this file is updated; breaking changes go in the
release notes of the next tagged release.
