# WKS Platform — Docker

Two compose files cover single-tenant operator surfaces: a zero-config dev stack (`docker-compose.yml`) and a single-tenant production stack (`docker-compose.prod.yml`, Postgres + MinIO + WKS app). An optional Keycloak SSO seam stacks on top of production via `docker-compose.sso.yml`. Per-client / multi-instance deployments live at [`deploy/`](../deploy/) (Story 14.7).

Architecture reference: zero `tenant_id` invariant — see [`docs/zero-tenant-id.md`](../docs/zero-tenant-id.md).

## Dev quick-start (zero env vars)

```bash
docker compose -f docker/docker-compose.yml up --build
```

H2 file-mode datasource at `./data/`, local storage, console logging. Story 1.1 contract — boots without any operator-supplied secrets and without any env file. The dev compose file contains only the dev `wks-platform` service, so port-8080 collision with the production stack is structurally impossible (Story 14.1.1 fix-up replaced the previous `profiles:`/tracked-`.env` mechanism with a file-split).

### Database access (dev / evaluation)

H2 console is enabled out-of-the-box. Once the stack is up:

- **Browser**: <http://localhost:8080/h2-console>
- **JDBC URL**: `jdbc:h2:file:./data/wksplatform`
- **User**: `sa` &nbsp; **Password**: *(empty)*

The H2 console is disabled under the production profile (`H2ConsoleAutoConfiguration` is excluded in `application-production.yml`).

For desktop tools (DBeaver / DataGrip / IntelliJ), the backend must be stopped first — H2 holds an exclusive lock on the file in this configuration. Stop the backend, point the desktop client at the same JDBC URL above (using an absolute path), browse, then restart the backend. Inside Docker the file lives in the `wks-data` named volume; the in-app `/h2-console` is the easiest SQL path for that case.

## Production quick-start (single-tenant, Postgres + MinIO)

```bash
cp docker/.env.production.example docker/.env
# fill in values; generate secrets with: openssl rand -base64 32
docker compose -f docker/docker-compose.prod.yml up
```

Brings up:

- `wks-platform-prod` — same image as dev, `SPRING_PROFILES_ACTIVE=production`, env-driven config.
- `postgres` (postgres:16-alpine) — pinned, named volume `wks-pgdata`.
- `minio` (RELEASE.2025-09-07T16-13-09Z) — pinned, named volume `wks-miniodata`.

The application waits for Postgres + MinIO healthchecks before booting. `/api/health` returns 200 once Flyway has migrated the schema. Story 14.1 hard-overrides the production datasource driver to `org.postgresql.Driver` — no H2 entanglement on the production classpath (verified in CI by the `production-profile-smoke` job).

The `docker/.env` file is gitignored (Story 14.1.1 fix-up — no longer tracked). `.env.production.example` is the tracked template. Per Decision 25, neither file may carry `tenant_id` / `tenantId` — customer identity lives in the license file (Decision 24, Epic 7), not in domain rows or env config.

## Optional SSO seam (Keycloak)

```bash
docker compose -f docker/docker-compose.prod.yml -f docker/docker-compose.sso.yml up
```

The Keycloak/SSO seam is **gated by the `auth.sso` license feature flag since Story 7-5.** Operators no longer toggle it directly — the announcer reads `LicenseService.isFeatureEnabled(AUTH_SSO)` at startup and per request. Enterprise / Demo licenses unlock the surface (the seam announces and `/api/auth/saml/**` paths fall through to the security chain, currently returning 401 until Story 10.4); OSS / Team / Expired / Degraded licenses 404 the SAML paths with `WKS-LIC-003`. Stacking `docker-compose.sso.yml` provisions a Keycloak container so realms can be pre-baked ahead of Story 10.4, but no WKS code path consumes the container until 10.4 wires the SAML adapter. Realms / clients are NOT pre-configured — Story 10.4 owns that.

When the license unlocks SSO, the application emits `WKS-AUTH-001` at WARN explicitly stating the seam is INERT (Story 14.1.1 AC4 wire string preserved — only the trigger source changed in Story 7-5).

## Production rotation guidance (Story 14.1.1 AC7)

Every value marked `<MUST-BE-ROTATED>` in `docker/.env.production.example` is a sentinel. The backend boot validator (`ProductionBootstrapValidator`) rejects unrotated values under the production profile and fails closed with `WKS-API-055` — the application refuses to start until each is replaced with a real secret. Rotate in `docker/.env` **before** `docker compose -f docker/docker-compose.prod.yml up`:

| Env var | Generate with | Notes |
| --- | --- | --- |
| `WKS_DB_PASSWORD` | `openssl rand -base64 32` | Postgres role password; matches `POSTGRES_PASSWORD` for the bundled container. |
| `WKS_STORAGE_KEY` | `openssl rand -base64 32` | MinIO secret key consumed by the WKS app (Decision 8 §437). |
| `WKS_ADMIN_EMAIL` | operator-chosen address | First-boot admin login. `WKS-API-051` fails closed if empty. |
| `WKS_ADMIN_PASSWORD` | `openssl rand -base64 24` | First-boot admin password. |
| `WKS_JWT_SECRET` | `openssl rand -base64 48` | Base64-encoded HMAC key, ≥32 bytes after decode. `WKS-API-053` fails closed if missing / invalid Base64 / too short. |
| `WKS_MINIO_ROOT_USER` | operator-chosen identifier | MinIO root user; the bundled container's admin login. |
| `WKS_MINIO_ROOT_PASSWORD` | `openssl rand -base64 32` | MinIO root password. |
| `WKS_KEYCLOAK_ADMIN` | operator-chosen identifier | Only consumed when stacking `docker-compose.sso.yml`; rotate before bringing up SSO. |
| `WKS_KEYCLOAK_ADMIN_PASSWORD` | `openssl rand -base64 24` | Only consumed when stacking `docker-compose.sso.yml`. |

Failed validation aborts startup with a multi-line `WKS-API-055` log entry naming every offending var. Rotate, restart, repeat.

## Multi-tenant on same host (Story 14.1.1 AC8)

Operators running multiple single-tenant 14.1 instances on one host MUST set `COMPOSE_PROJECT_NAME` per deploy. Compose scopes named volumes and container names by project; without distinct project names, two deploys share the `wks-pgdata` / `wks-miniodata` volumes and clobber each other.

```bash
# First deploy.
COMPOSE_PROJECT_NAME=wks-clientA docker compose -f docker/docker-compose.prod.yml up -d

# Second deploy on the same host — distinct volumes + container names.
COMPOSE_PROJECT_NAME=wks-clientB docker compose -f docker/docker-compose.prod.yml up -d
```

The volume names declared at the bottom of `docker-compose.prod.yml` (`wks-pgdata`, `wks-miniodata`) and `docker-compose.sso.yml` (`wks-keycloakdata`) intentionally have NO `name:` override; they inherit Compose's project-scoping. Setting an explicit `name:` would defeat that and force ALL deploys to share volumes — the opposite of what we want here. For per-client production deploys with hardened name suffixes, use `deploy/` (Story 14.7) instead — that surface uses slug-suffixed names to make collision impossible by construction.

## Rollback

```bash
# Stop containers, KEEP data volumes (resume later with `up`):
docker compose -f docker/docker-compose.prod.yml down

# Stop containers AND DELETE data volumes (Postgres, MinIO):
docker compose -f docker/docker-compose.prod.yml down -v
```

**`down -v` is irreversible.** It removes `wks-pgdata` and `wks-miniodata` (plus `wks-keycloakdata` if `docker-compose.sso.yml` was stacked). This IS the GDPR-delete path. Verify with `docker volume ls` — the named volumes must be absent post-`down -v`. A subsequent `up` re-creates fresh empty volumes.

For per-client / multi-instance rollback (different naming convention), see [`docs/ops/per-client-deploy-runbook.md`](../docs/ops/per-client-deploy-runbook.md) §7.

## Cross-references

- **Per-client deployment** (Story 14.7): [`deploy/`](../deploy/) — driver script `wks-deploy.sh`, per-client compose template, runtime invariant probe. Same env-var contract as this single-tenant surface; service/volume/network names are slug-suffixed per client.
- **Architecture Decision 8** — production-profile contract, env-only secrets, single-image multi-stage build.
- **Architecture Decision 25** — ZERO `tenant_id` invariant in any compose value or env file.
- **Architecture Decision 24** — license file delivers customer identity (Epic 7).
- **Story 14.1** — discharges Story 14.7 §8 / WKS-API-053 H2/Postgres bug; this profile is the canonical hardened production env contract.
