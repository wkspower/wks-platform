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

- `WKS-CFG-001–099`: Configuration validation
- `WKS-CFG-100–199`: BPMN validation
- `WKS-CFG-200–299`: Template validation
- `WKS-API-001–099`: API input validation
- `WKS-RTM-001–099`: Runtime errors
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
