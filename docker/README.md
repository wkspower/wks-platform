# WKS Platform — Docker

Two operator surfaces share `docker-compose.yml`: a zero-config dev stack and a single-tenant production profile (Postgres + MinIO + optional Keycloak). Per-client / multi-instance deployments live at [`deploy/`](../deploy/) (Story 14.7).

Architecture reference: [`_bmad-output/planning-artifacts/architecture.md`](../_bmad-output/planning-artifacts/architecture.md) Decision 8 (§388–443) and Decision 25 (§932–958, ZERO `tenant_id` invariant).

## Dev quick-start (zero env vars)

```bash
cd docker
docker compose up --build
```

H2 file-mode datasource at `./data/`, local storage, console logging. Story 1.1 contract — boots without any `.env`.

## Production quick-start (single-tenant, Postgres + MinIO)

```bash
cp docker/.env.production.example docker/.env
# fill in values; generate secrets with: openssl rand -base64 32
cd docker
docker compose --profile production up
```

Brings up:

- `wks-platform-prod` — same image as dev, `SPRING_PROFILES_ACTIVE=production`, env-driven config.
- `postgres` (postgres:16-alpine) — pinned, named volume `wks-pgdata`.
- `minio` (RELEASE.2025-09-07T16-13-09Z) — pinned, named volume `wks-miniodata`.

The application waits for Postgres + MinIO healthchecks before booting. `/api/health` returns 200 once Flyway has migrated the schema. Story 14.1 hard-overrides the production datasource driver to `org.postgresql.Driver` — no H2 entanglement on the production classpath (verified in CI by the `production-profile-smoke` job).

The `.env` file is gitignored. `.env.production.example` is the tracked template. Per Decision 25, neither file may carry `tenant_id` / `tenantId` — customer identity lives in the license file (Decision 24, Epic 7), not in domain rows or env config.

## Optional SSO seam (Keycloak)

```bash
docker compose --profile production --profile production-sso up
```

Two switches gate SSO activation:

1. `WKS_KEYCLOAK_ENABLED=true` in `.env`, AND
2. `--profile production-sso` on the compose command.

The Keycloak container is provisioned but auth-provider integration is gated on Story 10.4 / Epic 7 (license-tier SSO). Until then the application emits `WKS-AUTH-001` at WARN and built-in cookie-JWT auth (Story 1.2) remains active. Realms / clients are NOT pre-configured — Story 10.4 owns that.

## Rollback

```bash
# Stop containers, KEEP data volumes (resume later with `up`):
docker compose --profile production down

# Stop containers AND DELETE data volumes (Postgres, MinIO, optional Keycloak):
docker compose --profile production down -v
```

**`down -v` is irreversible.** It removes `wks-pgdata` and `wks-miniodata` (plus `wks-keycloakdata` if `production-sso` was used). This IS the GDPR-delete path. Verify with `docker volume ls` — the named volumes must be absent post-`down -v`. A subsequent `up` re-creates fresh empty volumes.

For per-client / multi-instance rollback (different naming convention), see [`docs/ops/per-client-deploy-runbook.md`](../docs/ops/per-client-deploy-runbook.md) §7.

## Cross-references

- **Per-client deployment** (Story 14.7): [`deploy/`](../deploy/) — driver script `wks-deploy.sh`, per-client compose template, runtime invariant probe. Same env-var contract as this single-tenant surface; service/volume/network names are slug-suffixed per client.
- **Architecture Decision 8** — production-profile contract, env-only secrets, single-image multi-stage build.
- **Architecture Decision 25** — ZERO `tenant_id` invariant in any compose value or env file.
- **Architecture Decision 24** — license file delivers customer identity (Epic 7).
- **Story 14.1** — discharges Story 14.7 §8 / WKS-API-053 H2/Postgres bug; this profile is the canonical hardened production env contract.
