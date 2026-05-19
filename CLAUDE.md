# WKS Platform v2

Case management platform for system integrators. OSS core (Apache 2.0). Embedded CIB seven (Camunda 7 fork) BPMN engine.

## Project status

v2 is a full rewrite. v1 code is archived (tagged `v1-final`, on the `v1` branch). The active trunk for v2 work is **`v2-develop`**; `develop` stays frozen at v1 until a bulk merge-back decision. Feature branches target `v2-develop`.

## Architecture

Monorepo: Spring Boot 3.x backend + Vite React SPA frontend, bundled into a single Docker image.

```
wks-platform/
├── backend/                 (Spring Boot + embedded CIB seven)
│   └── src/main/java/com/wkspower/platform/
│       ├── api/             (REST controllers — thin, no business logic)
│       ├── domain/          (Pure Java — ZERO framework dependencies)
│       │   ├── model/       (Case, Task, CaseType, Document, AuditEntry, User, Role)
│       │   ├── service/     (CaseService, TaskService, WorkflowService, ConfigService)
│       │   └── port/        (Interfaces: CaseRepository, WorkflowEngine, DocumentStore, EventPublisher)
│       ├── engine/          (CIB seven — the ONLY package that imports C7)
│       ├── infrastructure/  (Implements domain ports: persistence, storage, events, config)
│       ├── security/        (Spring Security, RBAC filter, JWT)
│       └── audit/           (Cross-cutting audit logging via domain events)
├── frontend/                (Vite + React + TypeScript)
├── docker/                  (Compose files, Dockerfiles)
├── deploy/                  (Per-client deploy script + templates)
├── case-types/              (YAML case-type definitions loaded at boot)
└── docs/                    (Fumadocs site + reference markdown)
```

## Critical architectural rules

1. **`domain/` has zero framework dependencies.** No Spring, no JPA, no CIB seven imports. Pure Java only.
2. **`engine/` is the only package that imports CIB seven.** Other code goes through `domain/port/WorkflowEngine`.
3. **API controllers are thin.** Validate input, call domain service, return response. No business logic in controllers.
4. **JPA entities are separate from domain models.** `infrastructure/persistence/` bridges JPA and domain. Domain models never have `@Entity`.
5. **Workflow is optional and pluggable.** A case type may attach a BPMN process (via a `workflow:` block in its YAML) to drive stage transitions; if absent, stages advance manually via the domain service. `CaseService.transition` branches on the case's `processInstanceId`: present → route through `WorkflowEngine`; null/blank → update status directly. The BPMN engine (CIB seven) is a plug-in, not the spine.
6. **Config-driven rendering.** YAML case-type config generates JSON Schema server-side. Frontend renders forms/tables/filters from that schema.

## Tech stack

**Backend:** Java 21, Spring Boot 3.x, embedded CIB seven, JPA/Hibernate + Flyway, H2 (dev) + PostgreSQL (prod), Argon2id, JWT in httpOnly cookies, SLF4J + Logback JSON.

**Frontend:** Vite + React + TypeScript, Tailwind + Shadcn/ui, TanStack Query + Zustand, TanStack Table, React Hook Form + Zod, React Router, native `fetch` + native `EventSource`.

**Infra:** Multi-stage Dockerfile (Node → Maven → JRE slim), GitHub Actions CI, JUnit 5 + Spring Boot Test (backend), Vitest + RTL (frontend), Testcontainers for Postgres ITs, ghcr.io for image publishing.

## API conventions

- Envelope: `{ data, error, meta }`
- Error format: `{ error: { code, message, field, line } }`. Collect all errors — never fail-on-first.
- Pagination: `?page=0&size=20` → `meta: { total, page, size }`. Sort: `?sort=updatedAt,desc`.
- Dates ISO 8601; IDs UUID; JWT in httpOnly cookie (401 = expired/missing, 403 = insufficient permissions).
- springdoc-openapi at `/swagger-ui`.

Error code prefixes: `WKS-CFG-*` (config validation), `WKS-API-*` (input validation), `WKS-RTM-*` (runtime, e.g. `WKS-RTM-409` optimistic-lock conflict).

## Environment

Zero-config for dev (H2 + local storage + console logging). Production env:
`WKS_DB_URL`, `WKS_DB_USER`, `WKS_DB_PASSWORD`, `WKS_STORAGE_ENDPOINT`, `WKS_STORAGE_KEY`, `WKS_ADMIN_EMAIL`, `WKS_ADMIN_PASSWORD`, `WKS_CORS_ORIGINS`, `WKS_LOCALE`.

## Dev workflow

```bash
cd docker && docker compose up        # everything, dev mode, zero config
cd backend && ./mvnw spring-boot:run  # backend only
cd frontend && npm run dev            # frontend hot reload
cd backend && ./mvnw test             # backend tests
cd frontend && npm test               # frontend tests
```

## Local CI gate before pushing

Mirror what CI runs (`.github/workflows/ci.yml`). A committed `pre-push` hook at `.githooks/pre-push` does this automatically — enable once per clone:

```bash
git config core.hooksPath .githooks
```

Inner-loop verify (skips Postgres-only ITs): `cd backend && ./mvnw -B -ntp verify -Pfast-it`. Full mirror runs in CI on every push.

Emergency bypass: `WKS_SKIP_CI_LOCAL=1 git push`. Force full mirror: `WKS_FULL_CI_LOCAL=1 git push` (use before release-candidate pushes or when touching Postgres-specific surface like Flyway migrations under `postgresql/`, JSON column mapping, or dialect SQL).

Filename convention: integration tests requiring Testcontainers/Postgres must be named `*PostgresIT.java`; H2-based ITs use plain `*IT.java`.

## Security rules

- `JwtTokenProvider` is the **only** class allowed to import `io.jsonwebtoken.*` — enforced by ArchUnit.
- `api/` and `security/` must not import `infrastructure.persistence.entity.*` — enforced by ArchUnit.

## Frontend rules

- No `bg-[#…]` or raw px in style props — ESLint bans hex literals and px values outside `src/styles/**` and tests. Use Tailwind utilities or `bg-[var(--token)]`.
- Single fetch entry point: `src/api/client.ts` (`apiFetch`). Don't call `fetch()` directly from other files.
- Self-hosted fonts only — no `googleapis.com` / `gstatic.com` in `index.html` or `index.css`.

## Frontend testing

- Vitest + jsdom + `@testing-library/react`. Default `npm test` runs the full suite; `npm run test:coverage` runs the ratchet.
- `src/test/setup.ts` owns the global lifecycle: jest-dom matchers, `cleanup()` after each test, `restoreAllMocks`, and a `fetch` stub that throws — every test that touches the network must stub fetch explicitly with `vi.spyOn(globalThis, 'fetch')`.
- `src/test/renderWithProviders` is the single component-render entry point — it wraps `MemoryRouter` + `QueryClientProvider` and seeds the auth store. Use it; don't hand-wire providers per test.
- **Coverage ratchet** in `vite.config.ts` `test.coverage.thresholds`. CI runs `npm run test:coverage` and fails if any threshold drops. Raise thresholds when you add coverage; **never lower them**. New features land with their tests in the same PR — that's how the ratchet works.

## Flyway conventions

Migrations under `backend/src/main/resources/db/migration/`, split by dialect:

- `common/` — runs on every profile (dialect-portable SQL only)
- `h2/` — dev profile only
- `postgresql/` — production profile only

Rules: append-only (never edit a committed migration); no conditional SQL inside scripts (split into dialect folders instead); `ALTER TABLE … ADD COLUMN IF NOT EXISTS` is portable across H2 ≥ 1.4 and Postgres ≥ 9.6; identifiers stay lowercase (H2 upper-cases unquoted identifiers by default).

## Case-type configuration

- **Directory**: `WKS_CASE_TYPES_DIR` env var; defaults to `./case-types/` (resolves to `/app/case-types/` in container).
- **Startup scan**: on `ApplicationReadyEvent`, every `*.yaml|*.yml` is parsed and validated. Invalid files log one WARN per error and are skipped; startup continues.
- **Fail-fast**: `WKS_CASE_TYPES_FAIL_ON_INVALID=true` aborts the context on any invalid file.
- **Validation philosophy**: collect-all — the validator returns every violation, never throws, never stops at the first failure.
- **Optional BPMN attachment**: a case type's `workflow:` block is optional. When omitted, the case type runs without a process instance; stages advance manually via the domain service. When present, `workflow.bpmn` is a path relative to the YAML file's parent directory.
- **Deploy endpoint**: `POST /api/admin/deploy` is multipart (`caseType` required, `bpmn` optional), each capped at 1 MB; `ROLE_ADMIN` required. When a BPMN is attached, every `bpmn:userTask` must declare exactly one archetype (`draft_section` / `submit_for_processing` / `business_final`) via `<camunda:property name="archetype" value="..."/>`.
