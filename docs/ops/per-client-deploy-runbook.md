<!--
SPDX-License-Identifier: Apache-2.0
Copyright 2026 WKS Power Limited
-->

# Per-Client Deploy Runbook (Phase-0)

**Phase-0 manual procedure. Hetzner automation is Story 15.1.**

This runbook is the operational counterpart of two locked architecture decisions:

- **License / Feature-Flag Gating.** License file is a signed JWT-style token (Ed25519). Customer identity = `licensee` claim. License is per-instance, never per-row.
- **One-Command Per-Client Deployment.** One isolated environment per customer (own Postgres + MinIO). Phase-0 = this runbook + `deploy/wks-deploy.sh`. Phase-1 = Hetzner Cloud API automation (Story 15.1).

The procedure is portable to any Linux host with Docker. The driver script is `deploy/wks-deploy.sh`. Read this whole document once before your first deploy — it is short.

You have all the levers. Each step has a documented expected output and a rollback path. If a command's output does not match, jump to §6.

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

## §1 Per-client artefact preparation

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

## §2 Image pull + bring-up

Run the driver script:

```bash
./deploy/wks-deploy.sh <client-slug>
```

The script performs, in order:

1. Verifies prerequisites (`docker`, `envsubst`, `curl`, `grep`).
2. Loads `deploy/clients/<client-slug>/client.env` and validates: required keys present, no unknown keys, `WKS_CLIENT_SLUG` matches the CLI arg, `WKS_IMAGE_TAG` is not `latest`.
3. Renders `deploy/templates/docker-compose.client.yml.template` to `deploy/clients/<client-slug>/docker-compose.yml` via `envsubst` and validates with `docker compose config -q`.
4. Runs `docker compose -p wks-<client-slug> up -d --pull always`.
5. Polls `/api/health` for up to 90 seconds.
6. Emits a handover summary with the URL and resolved licensee.

**Pinned image tags only.** The script rejects `latest` and any tag ending `:latest`. This is a hard guardrail; do not work around it.

The driver is **idempotent**. Re-running on an already-deployed slug performs `docker compose up -d` and exits 0 once health passes.

---

## §3 License install + boot verification

The license file is mounted read-only at `${WKS_LICENSE_PATH}` (default `/etc/wks/license.jwt`). On boot, the WKS application reads it and surfaces the `licensee` claim in the admin banner.

To verify:

```bash
curl -fsS http://localhost:18080/api/license/status
```

Expected (post-Story-7.1):

```json
{ "licensee": "<your client-slug or contracted name>", "tier": "oss|ee|demo-showcase", ... }
```

**Phase-0 placeholder behaviour (until Story 7.1 ships):** the endpoint may not exist yet. Once 7.1 ships, callers can hard-assert on it.

---

## §4 Smoke tests

Once the stack is up, sanity-check the public surfaces:

```bash
curl -fsS http://localhost:18080/api/health
curl -fsS http://localhost:18080/api/cases
curl -fsS http://localhost:18080/api/case-types
```

Each call should return a 2xx. A non-2xx is a hard stop. Do not hand the URL to the customer; jump to §6 rollback while you investigate.

---

## §5 Handover

Deliver to the customer **out-of-band** (encrypted channel — not committed, not in any chat log):

- Public URL (the `WKS_PUBLIC_URL` value).
- Admin email (`WKS_ADMIN_EMAIL`) and admin password (`WKS_ADMIN_PASSWORD`).
- Pinned image tag (operator-side reference for upgrades).
- License `licensee` name as it will appear in the banner.

Do NOT share `WKS_DB_PASSWORD` or `WKS_MINIO_ROOT_PASSWORD`. They are infrastructure-internal and rotate via redeploy.

---

## §6 Rollback procedure

**Soft rollback** (stops containers, preserves data):

```bash
docker compose -p wks-<client-slug> down
```

**Hard rollback** (removes volumes — **IRREVERSIBLE**, this is the GDPR-delete path):

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

## §7 Known limitations / Phase-1 follow-ups

- **Manual operator step.** No Hetzner Cloud API integration — Story 15.1.
- **No automatic backups.** Story 15.2.
- **No license rotation/expiry tooling.** Story 15.2.
- **No cross-client observability dashboard.** Phase 1+.
- **No automated health monitoring beyond the deploy-time check.** Phase 1+.
- **TLS termination is the operator's reverse-proxy responsibility.** Decision 8 — runbook does NOT bundle one.
- **Hetzner-specific provisioning (`hcloud server create ...`) is intentionally out of scope.** The runbook works on any Docker host so Story 15.1 can wrap it later.
- **Production environment table is provisional.** This Phase-0 template carries the minimum env vars needed for the runbook + driver to be reviewable. Story 14.1 (Production Docker Profile) owns the canonical, hardened production env contract — its work supersedes this template's env block.
- **Closed by Story 14.1 (2026-05-06):** the H2/Postgres production-profile bug (`Driver org.h2.Driver claims to not accept jdbcUrl, jdbc:postgresql://...`) is fixed. `application-production.yml` now hard-overrides `spring.datasource.driver-class-name: org.postgresql.Driver` and excludes H2 autoconfig under the production profile (Strategy B). Verified by the new `production-profile-smoke` CI job (Story 14.1 AC6) and a clean end-to-end run of `./deploy/wks-deploy.sh smoke` against a 14.1-built image: health OK, 7 Postgres migrations applied, zero `org.h2.Driver` references in container logs.

Each limitation is a future story. Nothing here is permanent — the boundary is documented so a reader six months from now knows where to push.
