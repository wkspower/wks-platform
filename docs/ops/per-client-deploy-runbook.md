<!--
SPDX-License-Identifier: Apache-2.0
Copyright 2026 WKS Power Limited
-->

# Per-Client Deploy Runbook (Phase-0)

**Phase-0 manual procedure. Hetzner automation is Story 15.1.**

This runbook is the operational counterpart of two locked architecture decisions:

- **Decision 24 — License / Feature-Flag Gating** (`_bmad-output/planning-artifacts/architecture.md` §889–930). License file is a signed JWT-style token (Ed25519). Customer identity = `licensee` claim. License is per-instance, never per-row.
- **Decision 25 — One-Command Per-Client Deployment + ZERO `tenant_id`** (`architecture.md` §932–958). One isolated environment per customer (own Postgres + MinIO). Application invariant: no `tenant_id` in domain code. Phase-0 = this runbook + `deploy/wks-deploy.sh`. Phase-1 = Hetzner Cloud API automation (Story 15.1).

The procedure is portable to any Linux host with Docker. The driver script is `deploy/wks-deploy.sh`. Read this whole document once before your first deploy — it is short.

You have all the levers. Each step has a documented expected output and a rollback path. If a command's output does not match, jump to §7.

---

## §0 Prerequisites

On the **target host** (the box that will run the customer's WKS instance):

- Docker Engine ≥ 24 with the `docker compose` plugin (`docker compose version` works).
- `bash` ≥ 4.
- `gettext` (provides `envsubst`).
- `openssl` (for password generation).
- `curl`, `grep`.
- A reverse proxy with TLS termination already pointed at the host's chosen port. WKS does NOT bundle a reverse proxy (per Decision 8).
- SSH access for the operator.

On the **operator's workstation**:

- A clone of `wkspower/wks-platform` at the tag you intend to deploy.
- The customer's pinned image tag (e.g. `ghcr.io/wkspower/wks-platform:0.5.0`). **Never `latest`.**
- The customer's license file (`license.jwt`). If Story 7.1 has not shipped yet, a placeholder file is acceptable for Phase-0; the boot fallback to `tier: oss` per Decision 24 will surface.

---

## §1 Pre-flight invariant check

Before deploying, confirm the source tag you are about to ship has a clean baseline:

```bash
./backend/.ci/check-tenant-invariant.sh
```

Expected output:

```
Tenant invariant (D25) OK
```

Any other output: **STOP**. Investigate before deploying. This catches regressions in cherry-pick scenarios and before any operator pushes a tag that violates Decision 25.

---

## §2 Per-client artefact preparation

Pick a `<client-slug>` (lowercase alnum + dash, 2–32 chars; e.g. `acme`).

```bash
mkdir -p deploy/clients/<client-slug>
cp deploy/templates/client.env.template deploy/clients/<client-slug>/client.env
```

Edit `deploy/clients/<client-slug>/client.env`. Generate secrets:

```bash
openssl rand -base64 32   # WKS_DB_PASSWORD
openssl rand -base64 32   # WKS_MINIO_ROOT_PASSWORD
openssl rand -base64 24   # WKS_ADMIN_PASSWORD
openssl rand -base64 48   # WKS_JWT_SECRET (Base64, ≥32 bytes — required by production profile)
```

Place the customer's license file:

```bash
cp /path/to/customer-license.jwt deploy/clients/<client-slug>/license.jwt
```

**Worked example for fictional client `acme`:**

```
WKS_CLIENT_SLUG=acme
WKS_PUBLIC_URL=https://acme.wks.example.com
WKS_DB_PASSWORD=<generated>
WKS_MINIO_ROOT_PASSWORD=<generated>
WKS_ADMIN_EMAIL=ops@acme.example.com
WKS_ADMIN_PASSWORD=<generated>
WKS_LICENSE_PATH=/etc/wks/license.jwt
WKS_IMAGE_TAG=ghcr.io/wkspower/wks-platform:0.5.0
WKS_JWT_SECRET=<generated>
```

`deploy/clients/<client-slug>/` is gitignored — never commit per-client artefacts.

---

## §3 Image pull + bring-up

Run the driver script:

```bash
./deploy/wks-deploy.sh <client-slug>
```

The script performs, in order:

1. Verifies prerequisites (`docker`, `envsubst`, `curl`, `grep`).
2. Loads `deploy/clients/<client-slug>/client.env` and validates: required keys present, no unknown keys, `WKS_CLIENT_SLUG` matches the CLI arg, `WKS_IMAGE_TAG` is not `latest`, no value contains a tenant-identifier.
3. Renders `deploy/templates/docker-compose.client.yml.template` to `deploy/clients/<client-slug>/docker-compose.yml` via `envsubst` and validates with `docker compose config -q`.
4. Runs `docker compose -p wks-<client-slug> up -d --pull always`.
5. Polls `/api/health` for up to 90 seconds.
6. Runs the runtime ZERO-`tenant_id` invariant probe (§5).
7. Emits a handover summary with the URL and resolved licensee.

**Pinned image tags only.** The script rejects `latest` and any tag ending `:latest`. This is a hard guardrail; do not work around it.

The driver is **idempotent**. Re-running on an already-deployed slug performs `docker compose up -d` and exits 0 once health and the runtime probe pass.

---

## §4 License install + boot verification

The license file is mounted read-only at `${WKS_LICENSE_PATH}` (default `/etc/wks/license.jwt`). On boot, the WKS application reads it and surfaces the `licensee` claim in the admin banner.

To verify:

```bash
curl -fsS http://localhost:18080/api/license/status
```

Expected (post-Story-7.1):

```json
{ "licensee": "<your client-slug or contracted name>", "tier": "oss|ee|demo-showcase", ... }
```

**Phase-0 placeholder behaviour (until Story 7.1 ships):** the endpoint may not exist yet. The runtime probe (§5) emits a `WARN` line and does NOT fail. Once 7.1 ships, the probe will hard-assert.

---

## §5 Smoke tests (incl. ZERO-`tenant_id` runtime check)

Standalone re-run any time:

```bash
./deploy/probes/check-runtime-no-tenant-id.sh http://localhost:18080
```

This hits `/api/health`, `/v3/api-docs`, `/api/cases`, `/api/case-types`, and `/api/license/status`. Any response body or header containing `tenant_id`, `tenantId`, or `@TenantId` fails the probe.

Expected success line:

```
RUNTIME INVARIANT OK: zero tenant_id leakage in 5 probed surfaces
```

Expected failure line (if regression):

```
http://localhost:18080/v3/api-docs: tenant_id — Forbidden by Decision 25 ...
RUNTIME INVARIANT FAILED: 1 violations across 5 probed surfaces
```

Failure is a hard stop. Do not hand the URL to the customer; jump to §7 rollback while you investigate.

---

## §6 Handover

Deliver to the customer **out-of-band** (encrypted channel — not committed, not in any chat log):

- Public URL (the `WKS_PUBLIC_URL` value).
- Admin email (`WKS_ADMIN_EMAIL`) and admin password (`WKS_ADMIN_PASSWORD`).
- Pinned image tag (operator-side reference for upgrades).
- License `licensee` name as it will appear in the banner.

Do NOT share `WKS_DB_PASSWORD` or `WKS_MINIO_ROOT_PASSWORD`. They are infrastructure-internal and rotate via redeploy.

---

## §7 Rollback procedure

**Soft rollback** (stops containers, preserves data):

```bash
docker compose -p wks-<client-slug> down
```

**Hard rollback** (removes volumes — **IRREVERSIBLE**, this is the GDPR-delete path per Decision 25):

```bash
docker compose -p wks-<client-slug> down -v
```

Remove the per-client artefacts:

```bash
rm -rf deploy/clients/<client-slug>/
```

Verify nothing of the client persists:

```bash
docker volume ls | grep <client-slug>     # MUST return empty
docker network ls | grep <client-slug>    # MUST return empty
```

If either grep returns a row, remove the leftover by name (`docker volume rm <name>` / `docker network rm <name>`) before declaring rollback complete.

---

## §8 Known limitations / Phase-1 follow-ups

- **Manual operator step.** No Hetzner Cloud API integration — Story 15.1.
- **No automatic backups.** Story 15.2.
- **No license rotation/expiry tooling.** Story 15.2.
- **No cross-client observability dashboard.** Phase 1+.
- **No automated health monitoring beyond the deploy-time probe.** Phase 1+.
- **TLS termination is the operator's reverse-proxy responsibility.** Decision 8 — runbook does NOT bundle one.
- **Hetzner-specific provisioning (`hcloud server create ...`) is intentionally out of scope.** The runbook works on any Docker host so Story 15.1 can wrap it later.
- **Production environment table is provisional.** This Phase-0 template carries the minimum env vars needed for the runbook + driver + probe to be reviewable. Story 14.1 (Production Docker Profile) owns the canonical, hardened production env contract — its work supersedes this template's env block.
- **Open known issue (Story 14.1 territory):** the current `production` Spring profile in v2-develop bundles the H2 driver and does not autoconfigure the Postgres datasource from `WKS_DB_URL`/`WKS_DB_USER`/`WKS_DB_PASSWORD` alone. The smoke run during Story 14.7 development confirmed this: containers come up, compose validates, the runtime probe correctly reports `INCONCLUSIVE` when no surfaces respond, but the WKS application crashes on Flyway init with `Driver org.h2.Driver claims to not accept jdbcUrl, jdbc:postgresql://...`. Story 14.1 owns wiring the production profile to honour the per-client compose env contract. Until 14.1 lands, this runbook is reviewable end-to-end but not boot-clean against an unmodified `production` profile image.

Each limitation is a future story. Nothing here is permanent — the boundary is documented so a reader six months from now knows where to push.
