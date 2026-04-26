# WKS Platform v2

Case management platform for system integrators. OSS core (Apache 2.0). Embedded CIB seven (Camunda 7 fork) BPMN engine.

## Project Status

v2 is a full rewrite. v1 code is archived (tagged `v1-final`, on the `v1` branch). During v2 development, the active trunk is **`v2-develop`** (forked from `develop` at `v1-final`); `develop` stays frozen at v1 until a bulk merge-back decision. Feature branches target `v2-develop`, not `develop`.

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
│   ├── src/
│   └── components/ui/       (Shadcn/ui owned-source components)
├── docker/                  (Compose files, Dockerfiles)
├── templates/               (BFSI template: YAML + BPMN + sample data)
├── specs/                   (OpenAPI, JSON Schema — auto-generated in Phase 0)
└── docs/                    (Getting-started, config reference)
```

## Critical Architectural Rules

1. **`domain/` package has ZERO framework dependencies.** No Spring annotations, no JPA annotations, no CIB seven imports. Pure Java only. This is NFR36 — non-negotiable.
2. **`engine/` is the ONLY package that imports CIB seven.** Engine abstractions go through `domain/port/WorkflowEngine`. NFR35.
3. **API controllers are thin.** Validate input, call domain service, return response. No business logic in controllers.
4. **JPA entities are separate from domain models.** `infrastructure/persistence/` bridges JPA and domain. Domain models never have `@Entity`.
5. **All stage transitions are BPMN-only.** YAML defines stage names/metadata. BPMN defines transition rules. Every case type has a BPMN process.
6. **Config-driven rendering.** YAML case type config generates JSON Schema server-side. Frontend renders forms/tables/filters from that schema. No hardcoded UI for case-specific fields.

## Tech Stack

### Backend
- Java 21 (required — compiler target `--release 21`; virtual threads for SSE)
- Spring Boot 3.x
- CIB seven (embedded, Spring Boot starter)
- JPA/Hibernate + Flyway migrations
- H2 (dev/eval, zero config) + PostgreSQL (production)
- Argon2id password hashing
- JWT in httpOnly cookies (not localStorage)
- SLF4J + Logback structured JSON logging

### Frontend
- Vite + React + TypeScript
- Tailwind CSS + Shadcn/ui
- TanStack Query (server state) + Zustand (client state)
- TanStack Table (config-driven dynamic columns)
- React Hook Form + Zod (forms)
- React Router
- Native `fetch` (no axios) + native `EventSource` (SSE)
- `t(key)` function for i18n (JSON resource bundles, no framework in Phase 0)

### Infrastructure
- Multi-stage Dockerfile: Node (vite build) -> Maven (package) -> JRE slim
- GitHub Actions CI
- JUnit 5 + Spring Boot Test + H2 (backend tests)
- Vitest + React Testing Library (frontend tests)
- Testcontainers for Postgres integration tests
- ghcr.io for Docker image publishing

## API Conventions

- Envelope: `{ data, error, meta }`
- Error format: `{ error: { code: "WKS-CFG-003", message: "...", field: "..." } }`
- Pagination: `?page=0&size=20` -> `meta: { total, page, size }`
- Sorting: `?sort=updatedAt,desc`
- Dates: ISO 8601
- IDs: UUIDs
- Auth: JWT in httpOnly cookie. 401 = expired/missing. 403 = insufficient permissions.
- No API version prefix in Phase 0 (labeled v0/unstable)
- springdoc-openapi auto-generates docs at `/swagger-ui`

## Error Code Taxonomy

- `WKS-CFG-001–009, 011, 099`: Case-type YAML validation (Story 2.1)
- `WKS-CFG-010, 012, 020, 021`: BPMN validation (Story 2.2 — variance from `architecture.md`
  §Decision 14 which lists `100..199`; we use the contiguous `010..099` band per the epic AC, so
  all deploy-time validation codes stay below 100. Architecture doc gets a follow-up patch.)
- `WKS-CFG-200–299`: Template validation (reserved)
- `WKS-API-001–099`: API input validation (`WKS-API-413` for multipart cap, Story 2.2)
- `WKS-RTM-001–099`: Runtime errors (`WKS-RTM-409` optimistic-lock conflict, Story 2.3)
- Error structure: `{ code, message, field, line }`
- Collection pattern: collect ALL errors, never fail-on-first

## Environment Variables

Zero-config for dev (H2 + local storage + console logging). Production:
- `WKS_DB_URL`, `WKS_DB_USER`, `WKS_DB_PASSWORD`
- `WKS_STORAGE_ENDPOINT`, `WKS_STORAGE_KEY`
- `WKS_ADMIN_EMAIL`, `WKS_ADMIN_PASSWORD` (required on first boot in production)
- `WKS_CORS_ORIGINS`
- `WKS_LOCALE` (default `en-IN`)

## Design System

- CSS variables for all tokens — never hardcoded hex or spacing values
- Colors: primary `#3B5BDB`, secondary `#22D3EE`, brand navy `#0B1437`, zinc neutral scale
- Typography: Poppins (headings), Rubik (body), JetBrains Mono (code) — all self-hosted, zero CDN
- Spacing: 4px base unit scale
- Motion: fast 150ms, normal 300ms, slow 500ms
- All animations respect `prefers-reduced-motion`

## Dev Workflow

```bash
# Start everything (dev mode, zero config)
cd docker && docker compose up

# Backend only
cd backend && ./mvnw spring-boot:run

# Frontend only (hot reload)
cd frontend && npm run dev

# Run backend tests
cd backend && ./mvnw test

# Run frontend tests
cd frontend && npm test
```

## Local CI gate before opening a PR

**Rule:** Before pushing a branch and opening a PR, run **every** `run:` step the CI workflow (`.github/workflows/ci.yml`) executes, in the same order — not a curated subset. Skipping even one step produces avoidable CI failures that waste review cycles.

Current CI step sequence (keep this list in sync with `ci.yml` — when the workflow adds a step, add it here in the same commit):

```bash
# Backend job
cd backend && ./mvnw -B -ntp verify

# Frontend job
cd frontend
npm ci                         # if deps changed
npm run lint
npm run format:check           # easy to forget — runs Prettier
npm test
npm run build
find dist/assets -name '*.woff2' | wc -l   # must be >= 4 (Story 1.3 AC #13)

# Docker job
docker buildx build -f docker/Dockerfile -t wks:ci .
```

"Lint + test + build" is **not** the full set. `format:check` is a separate step and Prettier regressions only surface there. When in doubt, grep `ci.yml` for `run:` and copy every command verbatim.

### Pre-push hook (one-time setup)

A committed `pre-push` hook at `.githooks/pre-push` runs the full CI mirror automatically. Enable it once per clone:

```bash
git config core.hooksPath .githooks
```

Emergency bypass (use only if you know why): `WKS_SKIP_CI_LOCAL=1 git push`. Do **not** use `--no-verify` — the hook exists because the memory-backed "match CI locally" rule kept getting violated.

---

## Story 1.1 artifacts

Story 1.1 (Project skeleton & Docker boot) created the scaffold. Future
agents can find its outputs here:

- `pom.xml` — parent POM; `backend/pom.xml` is the only Maven module
- `backend/src/main/java/com/wkspower/platform/WksPlatformApplication.java` —
  entry point; CIB seven autoconfig excluded by name (keeps root package
  free of `org.cibseven.*` imports so ArchUnit rule stays strict)
- `backend/src/main/java/com/wkspower/platform/{api,domain,engine,infrastructure,security,audit}/` —
  hexagonal packages with `package-info.java` placeholders
- `backend/src/main/java/com/wkspower/platform/api/dto/{ApiResponse,ErrorPayload}.java` —
  envelope used by every REST response
- `backend/src/main/java/com/wkspower/platform/api/controller/HealthController.java` —
  `GET /api/health`
- `backend/src/main/java/com/wkspower/platform/audit/CorrelationIdFilter.java` —
  per-request UUID → MDC → `X-Correlation-Id` response header
- `backend/src/main/java/com/wkspower/platform/infrastructure/config/DataSourceConfig.java` —
  `@Primary` DataSource (CIB seven registers its own in Story 2.2)
- `backend/src/main/resources/{application,application-production}.yml` +
  `logback-spring.xml` + `db/migration/V202604160001__baseline.sql`
- `backend/src/test/java/com/wkspower/platform/architecture/ArchitectureTest.java` —
  ArchUnit rules enforcing domain purity + CIB seven isolation
- `frontend/` — Vite 6 + React 19 + TS-strict scaffold (placeholder page only)
- `docker/{Dockerfile,docker-compose.yml,.dockerignore}` — multi-stage
  build (frontend-build → backend-build → runtime); SPA embedded in the JAR
- `.github/workflows/ci.yml` — backend + frontend + docker-build jobs

Story 1.1 intentionally creates **scaffolding only**. Domain models,
security config, and business logic belong to Stories 1.2–1.5 and later
epics — do not add them here.

## Story 1.2 artifacts

Story 1.2 (Authentication & session management) added the auth stack. Key
files:

- `backend/pom.xml` — added `spring-boot-starter-security`,
  `io.jsonwebtoken:jjwt-{api,impl,jackson}:0.12.6`, `bcprov-jdk18on`
- `backend/src/main/resources/db/migration/V202604170001__create_users_and_roles.sql` —
  `users` / `roles` / `user_roles` schema, fixed-UUID admin role seed
- `backend/src/main/java/com/wkspower/platform/domain/model/{User,Role,AuthenticationMaterial}.java` —
  framework-free records. Domain `User` has no `passwordHash` — the hash
  is exposed only by `UserRepository.findAuthMaterialByEmail`
- `backend/src/main/java/com/wkspower/platform/domain/port/UserRepository.java` —
  port with `findByEmail`, `findAuthMaterialByEmail`, `save`, `existsWithRole`
- `backend/src/main/java/com/wkspower/platform/domain/exception/{WksException,WksAuthenticationException,WksAuthorizationException,WksValidationException}.java`
- `backend/src/main/java/com/wkspower/platform/infrastructure/persistence/{UserRepositoryAdapter.java,entity/{UserEntity,RoleEntity}.java,repository/{UserEntityRepository,RoleEntityRepository}.java}`
- `backend/src/main/java/com/wkspower/platform/security/{SecurityConfig,JwtTokenProvider,JwtAuthenticationFilter,WksUserPrincipal,WksAuthenticationEntryPoint,AuthenticatedUser,AdminUserSeeder}.java`
- `backend/src/main/java/com/wkspower/platform/api/controller/AuthController.java` —
  `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`
- `backend/src/main/java/com/wkspower/platform/api/dto/{request/LoginRequest,response/AuthUserDto}.java`
- `backend/src/main/java/com/wkspower/platform/api/GlobalExceptionHandler.java` —
  expanded with `WksAuthenticationException`, `WksAuthorizationException`,
  `WksValidationException`, `MethodArgumentNotValidException`,
  `HttpMessageNotReadableException` handlers
- `backend/src/main/java/com/wkspower/platform/infrastructure/config/FilterConfig.java` —
  orders `CorrelationIdFilter` first, suppresses duplicate
  auto-registration of `JwtAuthenticationFilter`
- `backend/src/main/resources/application.yml` — `wks.admin.*`,
  `wks.jwt.*`, `wks.cors.*` config keys
- Tests: `JwtTokenProviderTest`, `AdminUserSeederTest`,
  `AuthControllerTest`, `HealthControllerTest` (updated to import
  `SecurityConfig`), `AuthFlowIT`; `ArchitectureTest` grew rules for
  api/security ↛ entity, JJWT-only-in-security, and domain ↛ jjwt.

Rules added for future agents: `JwtTokenProvider` is the **only** class
allowed to import `io.jsonwebtoken.*`; `api/` and `security/` must not
import `infrastructure.persistence.entity.*` (ArchUnit enforces both).


## Story 1.3 artifacts

Story 1.3 (Frontend application shell & design system) replaced the
"Coming Soon" placeholder with the full frontend chrome. New top-level
folders under `frontend/src/`: `api/`, `components/{ui,layout,errors,
routing}/`, `hooks/`, `i18n/`, `lib/`, `pages/`, `providers/`, `stores/`,
`styles/`, `test/`, `types/`, `assets/fonts/`. The token contract lives
in `frontend/src/styles/tokens.css` (CSS custom properties); Tailwind 4
maps tokens via the `@theme inline { ... }` block in
`frontend/src/index.css`.

Rules added for future agents:

- **Never `bg-[#…]` or raw px in style props** — ESLint's
  `no-restricted-syntax` rules ban hex literals and px values outside
  `src/styles/**` and tests. Reference tokens via Tailwind utilities
  (`bg-primary`) or `bg-[var(--token)]`.
- **Single fetch entry point.** `src/api/client.ts` (`apiFetch`) is
  the only place allowed to call `fetch()`. The
  `api/no-direct-fetch.test.ts` greps the source tree to enforce it.
- **MSW for all fetch mocking.** Per-test handlers go through
  `server.use(...)`; lifecycle is owned by `src/test/setup.ts`. Do
  not `vi.spyOn(globalThis, 'fetch')`.
- **Use `renderWithProviders`** (`src/test/renderWithProviders.tsx`)
  for every component test — it owns MemoryRouter +
  QueryClientProvider + auth-store seeding.
- **Self-hosted fonts only.** No `googleapis.com` / `gstatic.com` in
  `index.html` or `index.css` — guarded by `styles/fonts.test.ts`.


## Flyway conventions

Migrations live under `backend/src/main/resources/db/migration/` and are split by dialect:

```
db/migration/
├── common/         — runs on every profile (dialect-portable SQL only)
├── h2/             — dev profile only (H2-specific variants)
└── postgresql/     — production profile only (Postgres-specific variants)
```

`spring.flyway.locations` picks the right pair per profile:

- Dev (`application.yml`): `classpath:db/migration/common,classpath:db/migration/h2`
- Production (`application-production.yml`): `classpath:db/migration/common,classpath:db/migration/postgresql`

**Rules**:

- **Append-only.** Never edit a committed migration — add a new `V{YYYYMMDD}{seq}__…sql` file. The numeric date is lexicographic and avoids conflicts.
- **No conditional SQL.** Don't branch on `CURRENT_SCHEMA` or database name inside a script — split into dialect folders instead. Conditional SQL is how the lowercase-identifier bug bites.
- **Default location: `common/`.** Use dialect folders only when one database needs a genuine variant (e.g. `UUID` type, `CITEXT`, native JSON operators).
- **`ALTER TABLE … ADD COLUMN IF NOT EXISTS`** is portable across H2 (≥ 1.4) and Postgres (≥ 9.6) — prefer it when adding columns.
- **Identifiers stay lowercase.** H2 upper-cases unquoted identifiers by default; mixing cases is the fastest way to hit a "relation does not exist" surprise under Postgres. Keep `users`, `user_roles`, `password_hash` — not `Users`, `UserRoles`.

Example: a dialect-portable audit-column addition belongs in `common/`; a Postgres-specific `CREATE EXTENSION pg_trgm` belongs in `postgresql/`.

## Case type configuration

Case types are declared as YAML files in a directory mounted into the container. Story 2.1 ships the loader, multi-error validator, JSON Schema generator, and in-memory hot-reloadable registry; Story 2.2 adds the admin deploy endpoint.

- **Directory**: `WKS_CASE_TYPES_DIR` env var; defaults to `./case-types/` — resolves to `/app/case-types/` in container, relative to `backend/` in local `./mvnw spring-boot:run`. See the canonical example in the Story 2.1 file (`_bmad-output/implementation-artifacts/2-1-*.md`).
- **Startup scan**: on `ApplicationReadyEvent`, every `*.yaml|*.yml` in the directory is parsed, validated, and — on success — registered. Invalid files log one WARN per error (`wksErrorCode`, `file`, `errorField`, `line`) and are skipped; startup does not abort by default.
- **Fail-fast mode**: `WKS_CASE_TYPES_FAIL_ON_INVALID=true` (or `wks.case-types.fail-on-invalid: true`) aborts the context when any file fails validation — use in strict CI or production rollouts. The context-failure exits the container non-zero.
- **Hot reload contract**: file-driven on startup; programmatic via `ConfigService.validateAndRegister(Path)` — the admin deploy endpoint (Story 2.2) calls the same method. Higher `version` in a replace swaps atomically; same version is idempotent; lower version is rejected with `WKS-CFG-011`.
- **Validation philosophy**: collect-all. The validator returns a `ValidationResult` with every violation — never throws, never stops at the first failure. Reviewers grep for early `return` / `throw` inside validator methods during review.
- **Error taxonomy**: `WKS-CFG-001..099` (see `docs/api-conventions.md`). `field` paths are JSON-Pointer-flavour minus the leading slash (`fields[2].type`); `line` is 1-based.
- **Not in scope for 2.1**: `POST /api/admin/deploy` (Story 2.2), BPMN validation (`WKS-CFG-010..021`, Story 2.2), SSE deploy events (Stories 2.2 + 4.3), frontend Schema consumption (Stories 2.5 / 2.7), role enforcement (Story 5.2).
- **BPMN deploy (Story 2.2)**: `workflow.bpmn` is a path **relative to the YAML file's parent
  directory**. `POST /api/admin/deploy` is a multipart endpoint with two parts (`caseType`,
  `bpmn`), each capped at 1 MB; `ROLE_ADMIN` required; success returns `{caseTypeId, version,
  deploymentId, processDefinitionId, schemaUri}`. Every `bpmn:userTask` must declare exactly one
  archetype (`draft_section` / `submit_for_processing` / `business_final`) via
  `<camunda:properties><camunda:property name="archetype" value="..."/></camunda:properties>`.
  `enableDuplicateFiltering(true)` makes redeploy of identical bytes a no-op; whitespace-only
  edits trip a new engine version (by design — engine hashes resource bytes).

### Case CRUD (Story 2.3)

- **Endpoints**: `POST/GET/PUT /api/cases` and `GET /api/cases/{id}` (see `docs/api-conventions.md`).
- **JSON column**: `cases.data` is `JSON` on H2 and `JSONB` on Postgres (Java migration
  `V202604260002` upgrades the column on Postgres only). Hibernate 6.6+ native `@JdbcTypeCode(SqlTypes.JSON)`
  maps it — no third-party hibernate-types adapter.
- **Initial status**: comes from the case-type YAML's `statuses[0].id` (declaration order).
  Story 2.4's BPMN execution listener will keep it in sync once transitions ship.
- **Optimistic locking**: `cases.version` (from `BaseJpaEntity.@Version`) drives `PUT` semantics.
  Mismatch → HTTP 409 `WKS-RTM-409`.
- **`documentCount` is always 0** in the response until Epic 3 wires the document store.
- **`case_type_version` snapshot**: the case row records the case-type version at create time.
  Phase 0 forward-compat: validation runs against the **current** schema, not the snapshot.
- **Permission gating**: `@PreAuthorize` SpEL bean refs into `CaseTypePermissionEvaluator` —
  `create` on POST, `view` on GET / list / PUT (PUT-on-`view` is the Phase 0 simplification —
  the YAML grammar already supports `edit` since Story 2.1; tightening is Story 5.2 territory).
- **Engine ordering**: `CaseService.create` is engine-first / DB-second. Engine failure aborts
  the transaction cleanly; DB failure after engine success leaves a dangling process instance
  (accepted Phase 0 partial state — Phase 1 wraps in an outbox).

### Case Transitions & Tasks (Story 2.4)

- **Endpoints**: `POST /api/cases/{id}/transition`, `POST /api/tasks/{id}/complete`,
  `POST /api/tasks/{id}/claim` — all gate on the `transition` verb (see
  `docs/api-conventions.md`).
- **Transition dispatch**: Phase 0 supports BPMN message correlation only — `action` is the
  `<bpmn:message name="...">` attached to an `intermediateCatchEvent`/`receiveTask`. Signal-based
  transitions can be added in Story 8.2 if a real template requires them.
- **Engine-callback hexagonal pattern**: `CaseStatusListener` (a CIB seven `ExecutionListener`)
  reads `caseId` from the process variable and updates `cases.status` via the `CaseStatusUpdater`
  port — never via direct JPA writes inside the listener. The JPA adapter
  (`CaseStatusAdapter`) participates in the engine transaction so a listener failure rolls back
  both the engine state and the case-row update atomically. The listener is registered on every
  user-task and end-event `end` event by `CaseStatusEnginePlugin` (a
  `ProcessEnginePlugin` + `BpmnParseListener`) so BPMN XML stays free of
  `camunda:executionListener` boilerplate.
- **Status mapping**: an end event's status comes from `<camunda:property name="status"/>` (or
  the element id when absent). For user-task end, the listener picks the next active activity id
  via `runtimeService.getActiveActivityIds(...)`.
- **Three-archetype response surface**: `TaskActionResponse` carries the `archetype` read from
  the user task's `<camunda:property name="archetype"/>` so Story 2.8's `TaskLifecycleButton` can
  drive the right UI state without a follow-up call. Backend never blocks on downstream BPMN work
  for `submit_for_processing` — the SSE bridge (Story 4.3) confirms eventual completion.
- **Persistent process-definition mapping** (folded debt #1 from 2.3): the `case_type_deployments`
  Flyway table holds the `caseTypeId → processDefinitionKey` mapping durably; the in-memory
  cache becomes a write-through layer. JVM restart no longer drops admin-deployed mappings.
- **Concurrent-deploy hardening** (folded debt #2 from 2.2): `ConfigService.deploy` now holds a
  per-`caseTypeId` lock around the `reader.find → registrar.register` window so two concurrent
  deploys of the same case-type id can no longer interleave.

## Change Log

- 2026-04-26 — Story 2.4: Case status transitions via BPMN — `POST /api/cases/{id}/transition`,
  `POST /api/tasks/{id}/complete`, `POST /api/tasks/{id}/claim`. New `Task` domain record +
  `TaskService`, `WorkflowEngine` port extended with `findTask` / `completeTask` / `claimTask` /
  `signalTransition`, `CaseStatusListener` engine-callback adapter via the new `CaseStatusUpdater`
  port + `CaseStatusEnginePlugin`. New `case_type_deployments` Flyway table makes the
  `caseTypeId → processDefinitionKey` mapping durable (closes 2.3 deferred); per-`caseTypeId` lock
  in `ConfigService.deploy` closes the 2.2 TOCTOU deferred. ArchUnit rules added for the
  task-domain and engine-delegate import surfaces (AC8). `CaseFlowIT` extended with the
  create→complete→transition end-to-end path; `CaseAuthIT` extended with JWT round-trip on the
  transition endpoint. No new `WKS-*` wire codes — `WKS-RTM-409` is reused for engine conflicts.

- 2026-04-26 — Story 2.3: Case CRUD shipped — `POST/GET/PUT /api/cases`, JSON column for
  dynamic case data (H2 native + Postgres JSONB upgrade via Java Flyway migration),
  `WorkflowEngine.startProcessInstance` extension, `BaseJpaEntity.equals/hashCode`
  (id-based, transient-aware), `WKS-RTM-409` optimistic-locking code, ArchUnit rules for the
  case-domain boundary. Picks up Epic 1 retro action #5 (BaseJpaEntity equals/hashCode), Story
  1.4 chunk-3 deferred (`AdminUserSeeder.run()` broaden catch for
  `ObjectOptimisticLockingFailureException`). Networknt JSON-Schema-validator promoted from
  test to compile scope to power case-data validation.

- 2026-04-26 — Story 2.2: BPMN engine activated (embedded), `POST /api/admin/deploy` shipped,
  BPMN validator codes `WKS-CFG-010/012/020/021/022` added. The `WKS-CFG-010..099` band is now
  the shipped home for BPMN validation. `architecture.md` §Decision 14 still records the
  earlier `100..199` allocation; that document gets a follow-up patch (logged as a deferred
  item against Story 2.2 code review) — the wire contract here in CLAUDE.md and
  `docs/api-conventions.md` is the source of truth for shipped codes.
