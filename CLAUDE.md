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

### Inner loop: `-Pfast-it` for between-iteration runs

For the per-story development loop (human or agent) — i.e. checking work *between* commits while iterating — use:

```bash
cd backend && ./mvnw -B -ntp verify -Pfast-it
```

The `fast-it` Maven profile (defined in `backend/pom.xml`) excludes `**/*PostgresIT.java` from failsafe. That cuts ~11 Testcontainer-based ITs whose Docker startup dominates wall-clock (~2–5 min), bringing the inner loop from ~4–8 min to ~1–2 min. This matters most for agent dispatches, where long `verify` runs raise tool-call-ceiling risk and API 529 retry cost.

**This is not a CI bypass.** Postgres ITs still run in CI on every push, and the `.githooks/pre-push` hook still runs the full `verify` (without `-Pfast-it`) before any push leaves the machine — that pre-push gate is the "match CI locally" contract and is not negotiable. `-Pfast-it` is for the iteration cycle *before* the pre-push gate.

When adding a new `*PostgresIT.java` class, run that one class directly to verify it passes:

```bash
cd backend && ./mvnw -B -ntp failsafe:integration-test -Dit.test=NewPostgresIT
```

Filename convention: any IT that requires Testcontainers/Postgres MUST be named `*PostgresIT.java` so the profile excludes it. H2-based ITs use the plain `*IT.java` suffix.

### Pre-push hook (one-time setup)

A committed `pre-push` hook at `.githooks/pre-push` runs the full CI mirror automatically. Enable it once per clone:

```bash
git config core.hooksPath .githooks
```

Emergency bypass (use only if you know why): `WKS_SKIP_CI_LOCAL=1 git push`. Do **not** use `--no-verify` — the hook exists because the memory-backed "match CI locally" rule kept getting violated.

---

## Security rules (Story 1.2)

- `JwtTokenProvider` is the **only** class allowed to import `io.jsonwebtoken.*` — ArchUnit enforces this.
- `api/` and `security/` must not import `infrastructure.persistence.entity.*` — ArchUnit enforces this.

## Frontend rules (Story 1.3)

- **Never `bg-[#…]` or raw px in style props** — ESLint's `no-restricted-syntax` rules ban hex literals and px values outside `src/styles/**` and tests. Reference tokens via Tailwind utilities (`bg-primary`) or `bg-[var(--token)]`.
- **Single fetch entry point.** `src/api/client.ts` (`apiFetch`) is the only place allowed to call `fetch()`. The `api/no-direct-fetch.test.ts` greps the source tree to enforce it.
- **MSW for all fetch mocking.** Per-test handlers go through `server.use(...)`; lifecycle is owned by `src/test/setup.ts`. Do not `vi.spyOn(globalThis, 'fetch')`.
- **Use `renderWithProviders`** (`src/test/renderWithProviders.tsx`) for every component test — it owns MemoryRouter + QueryClientProvider + auth-store seeding.
- **Self-hosted fonts only.** No `googleapis.com` / `gstatic.com` in `index.html` or `index.css` — guarded by `styles/fonts.test.ts`.

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

## Case type configuration

- **Directory**: `WKS_CASE_TYPES_DIR` env var; defaults to `./case-types/` — resolves to `/app/case-types/` in container, relative to `backend/` in local `./mvnw spring-boot:run`.
- **Startup scan**: on `ApplicationReadyEvent`, every `*.yaml|*.yml` in the directory is parsed, validated, and — on success — registered. Invalid files log one WARN per error (`wksErrorCode`, `file`, `errorField`, `line`) and are skipped; startup does not abort by default.
- **Fail-fast mode**: `WKS_CASE_TYPES_FAIL_ON_INVALID=true` aborts the context when any file fails validation.
- **Hot reload contract**: `ConfigService.validateAndRegister(Path)` — higher `version` swaps atomically; same version is idempotent; lower version is rejected with `WKS-CFG-011`.
- **Validation philosophy**: collect-all. The validator returns a `ValidationResult` with every violation — never throws, never stops at the first failure. Reviewers grep for early `return` / `throw` inside validator methods during review.
- **BPMN deploy**: `workflow.bpmn` is a path **relative to the YAML file's parent directory**. `POST /api/admin/deploy` is multipart (`caseType` + `bpmn`), each capped at 1 MB; `ROLE_ADMIN` required. Every `bpmn:userTask` must declare exactly one archetype (`draft_section` / `submit_for_processing` / `business_final`) via `<camunda:properties><camunda:property name="archetype" value="..."/>`. `enableDuplicateFiltering(true)` makes redeploy of identical bytes a no-op.

### Case CRUD (Story 2.3)

- **JSON column**: `cases.data` is `JSON` on H2 and `JSONB` on Postgres. Hibernate 6.6+ native `@JdbcTypeCode(SqlTypes.JSON)` maps it — no third-party hibernate-types adapter.
- **Optimistic locking**: `cases.version` (from `BaseJpaEntity.@Version`) drives `PUT` semantics. Mismatch → HTTP 409 `WKS-RTM-409`.
- **Permission gating**: `create` verb on POST, `view` on GET / list / PUT. `edit` tightening is Story 5.2.
- **Engine ordering**: `CaseService.create` is engine-first / DB-second. Engine failure aborts the transaction cleanly; DB failure after engine success leaves a dangling process instance (accepted Phase 0 partial state — Phase 1 wraps in an outbox).

### Case Transitions & Tasks (Story 2.4)

- **Transition dispatch**: Phase 0 supports BPMN message correlation only — `action` is the `<bpmn:message name="...">` attached to an `intermediateCatchEvent`/`receiveTask`. Signal-based transitions can be added in Story 8.2.
- **Engine-callback hexagonal pattern**: `CaseStatusListener` (a CIB seven `ExecutionListener`) updates `cases.status` via the `CaseStatusUpdater` port — never via direct JPA writes inside the listener. The JPA adapter participates in the engine transaction so listener failure rolls back both atomically. The listener is registered by `CaseStatusEnginePlugin` (`ProcessEnginePlugin` + `BpmnParseListener`) so BPMN XML stays free of `camunda:executionListener` boilerplate.
- **Status mapping**: end event status comes from `<camunda:property name="status"/>` (or element id when absent). For user-task end, the listener picks the next active activity id via `runtimeService.getActiveActivityIds(...)`.
- **Persistent process-definition mapping**: `case_type_deployments` Flyway table holds `caseTypeId → processDefinitionKey` durably; the in-memory cache is write-through. JVM restart no longer drops admin-deployed mappings.
- **Concurrent-deploy hardening**: `ConfigService.deploy` holds a per-`caseTypeId` lock around the `reader.find → registrar.register` window.

### Case List (Story 2.5)

- **Wire-shape contract**: `StatusColor` and `FieldType` enums emit their lowercase `wire()` token via `@JsonValue`. The frontend column generator and status palette mapper read these tokens verbatim. `roles[]` and the `bpmn` reference are NOT echoed on the public surface.
- **Phase-0 client-side filtering**: `GET /api/cases?caseType=…&size=100`; multi-case-type fans out into parallel `useQueries`. Phase 1 collapses to a single backend call.
- **a11y**: semantic `<table>` + `<th scope="col">`, `aria-sort` on sortable headers, every `StatusBadge` shows colour + text label (never colour alone). Arrow keys move row focus, Enter / Space fires `onRowSelect`.
- **Performance budget**: `CaseDataTable.perf.test.tsx` renders 1,000 fixture rows under 1000 ms wall-clock on CI (paginated to 50 visible rows). Virtualisation is Phase 1.

### Case Workspace & Split-Pane (Story 2.6)

- **Two CSS states, no width animation**: discrete `workspace-list-only` and `workspace-list-and-detail` classes. `transition-none` defeats inherited transitions — UX spec rejects layout-thrash animation explicitly.
- **URL-driven selection**: `/cases` and `/cases/:caseId` both render `CasesPage`. Selection state from `useParams`; transitions via `useNavigate`. Selected case ID is **not** persisted across sessions — only `caseListFilters` and `sidebarCollapsed` are.
- **Keyboard navigation**: Esc (close), J (next case), K (previous case). Skips when target is `<input>`, `<textarea>`, contenteditable, or `event.isComposing`. J on last / K on first row are no-ops.
- **`useCase(id)` query key**: `['case', id]`. Story 4.3 SSE will `queryClient.invalidateQueries({queryKey: ['case', id]})` on `CaseStatusChanged` — keep this shape stable.
- **Properties tab**: read-only Phase 0. `lib/fieldFormatters.ts` is the value-formatter shared with table cells. Inline edit is Phase 1.
- **Embedded `caseType` in `CaseDto`**: `useCase(id)` returns the full `CaseDto` with embedded `caseType: CaseTypeView` frozen at `caseTypeVersion` — use this, not a separate `useCaseType()` call.

### Forms (Stories 2.7 + 2.8)

- **RHF canonical config**: `mode: 'onBlur'` + `reValidateMode: 'onChange'` + `shouldFocusError: true`. First error focuses on submit, then per-field rechecks are immediate.
- **`<FormField>`**: render-prop wrapper that wires `id` / `htmlFor` / `aria-invalid` / `aria-describedby` / `aria-required` from RHF context automatically. Consumer renders the input via `children(fieldProps)`.
- **Universal mutation lifecycle** (Story 2.8 AC7): every platform mutation MUST use one of two components — no parallel patterns:
  - `<MutationButton>` (4-state: `idle | confirming | confirmed | failed`) for synchronous REST round-trips.
  - `<TaskLifecycleButton>` (5-state, adds `processing`) for mutations whose completion is signalled async or may genuinely take >2s.
- **Confidence-not-safety copy**: confirm the user's mental model — "Confirmed" / "This task was already completed by {name}. No changes were made." Failure copy interpolates `{reason}`.
- **`LoginPage` is the reference pattern**: read `pages/LoginPage.tsx` for canonical wiring of `FormProvider` + `useForm({resolver: zodResolver(schema)})` + `<FormField>` + `<MutationButton>`.
- **Server errors via `setError`**: on 422 with `errors[].field`, map onto RHF via `form.setError(fieldName, {type: 'server', message})`. `pointerToField` on the backend guarantees `field` is the YAML-declared id. Envelope-level errors render in a banner: "Couldn't create case. Your input is preserved. Try again or correct the highlighted fields."
