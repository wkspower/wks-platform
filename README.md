# WKS Platform

Open-source case management for system integrators.

---

## What is WKS?

WKS Platform is an open-source case management platform for system integrators.
Define case types in YAML, drive lifecycles with BPMN workflows, and ship to
your clients — no backend code required.

Evaluate it in 30 minutes with `docker run -p 8080:8080 ghcr.io/wkspower/wks-platform` — no clone, no compose file, no config.

## Design system

The frontend ships a single design-token file at
`frontend/src/styles/tokens.css` (CSS custom properties on `:root`).
Tailwind 4 reads them via the `@theme inline { ... }` block in
`frontend/src/index.css` so utilities like `bg-primary`, `text-muted-
foreground`, and `rounded-lg` resolve to the tokens. Components
reference tokens — never raw hex literals (an ESLint rule enforces it).

Application routes (Phase 0):

- `/login` — credentials screen on the brand-navy background.
- `/cases` — placeholder Cases list (real implementation in Epic 2).
- `/tasks` — placeholder Tasks list (Epic 8).
- `/admin` — admin-only landing (Epic 5).
- `/dev` — developer-console landing (Epic 6).

Strings live in `frontend/src/i18n/en.json` and are looked up via a
small `t('key')` helper. `react-i18next` arrives in Phase 1; until
then the bundle is English-only and a Vitest guardrail asserts every
`t('...')` reference has an entry. Self-hosted Poppins + Rubik fonts
are bundled (no Google Fonts CDN).

## Quick start

Requires Docker 24+ and ~2 GB of free RAM. No clone, no Java, no Node, no
additional configuration.

```bash
docker run -p 8080:8080 ghcr.io/wkspower/wks-platform
```

Open `http://localhost:8080/`. For the multi-container path (Postgres,
MinIO, optional Keycloak) used in production and full-stack development,
see [`docker/README.md`](./docker/README.md). The REST health probe is at
`http://localhost:8080/api/health`. Interactive API docs (Swagger UI)
live at `http://localhost:8080/swagger-ui/index.html`; see
[`docs/api-conventions.md`](docs/api-conventions.md) for envelope,
pagination, and error-code conventions. Cold boot after the first
image build takes under two minutes on a 16 GB SSD dev machine.

### First-boot admin credentials

In **dev**, the platform seeds a default admin on first boot:
`admin@wkspower.local` / `admin`. Startup logs a WARN
(`WKS-API-050`) whenever the fallback is used — set `WKS_ADMIN_EMAIL` and
`WKS_ADMIN_PASSWORD` to override.

In **production** (`SPRING_PROFILES_ACTIVE=production`), both
`WKS_ADMIN_EMAIL` and `WKS_ADMIN_PASSWORD` **must** be set or the
application refuses to start (`WKS-API-051`). There is no production
fallback. Also set `WKS_JWT_SECRET` (Base64-encoded, ≥32 bytes) to keep
sessions stable across restarts.

## v1 is archived

v1 code lives on the [`v1` branch](https://github.com/wkspower/wks-platform/tree/v1)
and the [`v1-final` tag](https://github.com/wkspower/wks-platform/releases/tag/v1-final).
It is frozen — no new features, no security updates (see
[`SECURITY.md`](./SECURITY.md#supported-versions)). v2 is a full rewrite
because v1's monorepo-of-microservices (seven services, shared Docker
network, Keycloak + OPA for auth) made evaluation painful and deployment
brittle. v2 is a single-container monolith with strict hexagonal
boundaries so the evaluation takes 30 minutes instead of 30 hours.

For a fuller archival summary see [`docs/v1-archival.md`](./docs/v1-archival.md).

## Project status

**What works today?** The platform foundation is complete: project
skeleton + one-line `docker run` boot, authentication + session
management, React application shell + design system, and the REST API
foundation with H2/PostgreSQL persistence. Login works, `/api/health`
is live, and Swagger UI serves the real endpoints.

**What's next?** Case lifecycle and workspace — YAML case-type
configuration, embedded CIB seven BPMN deployment, case CRUD, and the
split-pane workspace. That milestone is the first evaluable build.

**When can I try the 30-minute demo?** After the case-lifecycle
milestone ships. The
[public roadmap](./ROADMAP.md) tracks shipped vs. next vs. later —
watch this README or the repository's Releases page for the
announcement.

## Architecture

Single-container monolith: Spring Boot backend with an embedded CIB seven
BPMN engine behind a `WorkflowEngine` port, a React SPA frontend, H2 in
development / PostgreSQL in production via JPA + Flyway, Docker-first
deployment. Case types are YAML files; lifecycle rules are BPMN processes;
the UI is rendered from a server-generated JSON schema so no case-specific
frontend code is needed.

See [docs/architecture.md](./docs/architecture.md) for the full decision
log (growing per epic).

## Links

- **API reference** — `http://localhost:8080/swagger-ui/index.html`
- **Architecture** — [`docs/architecture.md`](./docs/architecture.md)
- **API conventions** — [`docs/api-conventions.md`](./docs/api-conventions.md)
- **Roadmap** — [`ROADMAP.md`](./ROADMAP.md)
- **Contributing** — [`CONTRIBUTING.md`](./CONTRIBUTING.md)
- **Security policy** — [`SECURITY.md`](./SECURITY.md)
- **Code of conduct** — [`CODE_OF_CONDUCT.md`](./CODE_OF_CONDUCT.md)
- **v1 archival** — [`docs/v1-archival.md`](./docs/v1-archival.md)

## License

WKS Platform v2 is licensed under the **Apache License, Version 2.0**
(see [`LICENSE`](./LICENSE) and [`NOTICE`](./NOTICE)).

v1 was licensed under the MIT License, and the [`v1` branch](https://github.com/wkspower/wks-platform/tree/v1)
retains MIT unchanged. v2 relicenses to Apache 2.0 for the explicit
patent grant and to align with the embedded CIB seven engine, which is
also Apache 2.0.
