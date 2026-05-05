#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
# Copyright 2026 WKS Power Limited
#
# Per-client deploy driver (Story 14.7, Phase-0 manual runbook).
#
# Usage:
#   ./deploy/wks-deploy.sh <client-slug>
#
# Reads deploy/clients/<slug>/client.env, renders the per-client compose
# template, places the license file, brings up the stack, polls /api/health,
# and runs the runtime ZERO-tenant_id invariant probe.
#
# Idempotent: re-running on an existing slug performs `docker compose up -d`
# and exits 0 once health + invariant probe pass.
#
# Phase-0 manual procedure. Hetzner automation is Story 15.1.
# Source of truth for the runbook: docs/ops/per-client-deploy-runbook.md.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
RUNBOOK="docs/ops/per-client-deploy-runbook.md"

usage() {
  cat <<USAGE
Usage: ./deploy/wks-deploy.sh <client-slug>

Phase-0 per-client deploy driver. Reads deploy/clients/<slug>/client.env,
renders the compose template, brings up the stack, and runs the runtime
ZERO-tenant_id invariant probe. See ${RUNBOOK} for the full procedure.
USAGE
}

if [[ $# -ne 1 ]]; then
  usage >&2
  exit 2
fi

SLUG="$1"
case "$SLUG" in
  -h|--help|help) usage; exit 0 ;;
esac

# Allowed slug shape: lowercase alnum + dash, 2-32 chars. Keeps Docker happy
# and prevents shell-meaningful characters reaching the rendered compose.
if ! [[ "$SLUG" =~ ^[a-z0-9][a-z0-9-]{1,31}$ ]]; then
  echo "ERROR: invalid <client-slug>: '${SLUG}' (expected lowercase alnum + dash, 2-32 chars)" >&2
  exit 2
fi

cd "$REPO_ROOT"

# --- Step 0: prerequisite check ---------------------------------------------
for bin in docker envsubst curl grep; do
  if ! command -v "$bin" >/dev/null 2>&1; then
    echo "ERROR: required command '${bin}' not found on PATH." >&2
    echo "       See ${RUNBOOK} §0 (Prerequisites)." >&2
    exit 1
  fi
done
if ! docker compose version >/dev/null 2>&1; then
  echo "ERROR: 'docker compose' plugin not available." >&2
  echo "       See ${RUNBOOK} §0 (Prerequisites)." >&2
  exit 1
fi

CLIENT_DIR="deploy/clients/${SLUG}"
ENV_FILE="${CLIENT_DIR}/client.env"
LICENSE_HOST_PATH="${CLIENT_DIR}/license.jwt"
RENDERED_COMPOSE="${CLIENT_DIR}/docker-compose.yml"
TEMPLATE="deploy/templates/docker-compose.client.yml.template"

# --- Step 1: load client.env ------------------------------------------------
if [[ ! -f "$ENV_FILE" ]]; then
  echo "ERROR: ${ENV_FILE} not found." >&2
  echo "       Copy deploy/templates/client.env.template, fill it in, then re-run." >&2
  echo "       See ${RUNBOOK} §2 (Per-client artefact preparation)." >&2
  exit 1
fi

# Required key set (AC3, exact). Order matters only for documentation; the
# script enforces presence + rejects unknown keys.
REQUIRED_KEYS=(
  WKS_CLIENT_SLUG
  WKS_PUBLIC_URL
  WKS_DB_PASSWORD
  WKS_MINIO_ROOT_PASSWORD
  WKS_ADMIN_EMAIL
  WKS_ADMIN_PASSWORD
  WKS_LICENSE_PATH
  WKS_IMAGE_TAG
  WKS_JWT_SECRET
)

# --- Step 2: validate keys + values ----------------------------------------
# Defence-in-depth scan: reject tenant_id / tenantId anywhere in the env file
# (D25 invariant surfaced at deploy time, mirroring backend/.ci/check-tenant-invariant.sh).
if grep -E '(tenant_id|tenantId|@TenantId)' "$ENV_FILE" >/dev/null; then
  echo "ERROR: ${ENV_FILE} contains a tenant_id-like identifier." >&2
  echo "       Forbidden by Decision 25 (Zero tenant_id invariant). See docs/zero-tenant-id.md." >&2
  exit 1
fi

# Build the set of declared keys. Any KEY=VAL or KEY= line counts; comments and
# blanks are ignored. envsubst will read the live shell environment, so we
# `set -a; source; set +a` to export them; but we first lint the file shape.
declared_keys=()
while IFS= read -r line || [[ -n "$line" ]]; do
  line="${line%$'\r'}"
  case "$line" in
    ''|\#*) continue ;;
    *=*)
      key="${line%%=*}"
      # strip leading whitespace
      key="${key#"${key%%[![:space:]]*}"}"
      declared_keys+=("$key")
      ;;
    *)
      echo "ERROR: malformed line in ${ENV_FILE}: ${line}" >&2
      exit 1
      ;;
  esac
done < "$ENV_FILE"

# Reject unknown keys (typo guard).
for k in "${declared_keys[@]}"; do
  found=0
  for r in "${REQUIRED_KEYS[@]}"; do
    if [[ "$k" == "$r" ]]; then found=1; break; fi
  done
  if [[ $found -eq 0 ]]; then
    echo "ERROR: unknown key '${k}' in ${ENV_FILE}." >&2
    echo "       Allowed keys: ${REQUIRED_KEYS[*]}" >&2
    exit 1
  fi
done

# Source the env file safely.
set -a
# shellcheck disable=SC1090
. "$ENV_FILE"
set +a

# Required-presence + non-empty check.
for r in "${REQUIRED_KEYS[@]}"; do
  if [[ -z "${!r:-}" ]]; then
    echo "ERROR: required key '${r}' is unset or empty in ${ENV_FILE}." >&2
    exit 1
  fi
done

# Slug must match the CLI argument (copy-paste guard).
if [[ "$WKS_CLIENT_SLUG" != "$SLUG" ]]; then
  echo "ERROR: WKS_CLIENT_SLUG='${WKS_CLIENT_SLUG}' in ${ENV_FILE} does not match CLI arg '${SLUG}'." >&2
  exit 1
fi

# Reject :latest / latest image tags.
if [[ "$WKS_IMAGE_TAG" =~ :latest$ ]] || [[ "$WKS_IMAGE_TAG" == "latest" ]]; then
  echo "ERROR: WKS_IMAGE_TAG must be a pinned tag, not 'latest' (got: ${WKS_IMAGE_TAG})." >&2
  echo "       Pin a digest or version, e.g. ghcr.io/wkspower/wks-platform:0.5.0." >&2
  exit 1
fi

# License file must be present (placeholder OK in Phase-0; Story 7.1 will
# add Ed25519 verification at boot time).
if [[ ! -f "$LICENSE_HOST_PATH" ]]; then
  echo "ERROR: license file not found at ${LICENSE_HOST_PATH}." >&2
  echo "       Place a license.jwt (Phase-0 may use a placeholder). See ${RUNBOOK} §2." >&2
  exit 1
fi

# Compute absolute host path for the license mount.
WKS_CLIENT_LICENSE_HOST_PATH="$(cd "$(dirname "$LICENSE_HOST_PATH")" && pwd)/$(basename "$LICENSE_HOST_PATH")"
export WKS_CLIENT_LICENSE_HOST_PATH

# Provide a sensible default host port if operator didn't pin one (Phase-0
# convenience; reverse proxy is operator's responsibility per Decision 8).
WKS_HOST_PORT="${WKS_HOST_PORT:-18080}"
export WKS_HOST_PORT

# --- Step 3: render compose -------------------------------------------------
mkdir -p "$CLIENT_DIR"
# envsubst with explicit variable list = no surprise substitutions. Single
# quotes are intentional: envsubst itself parses the ${...} tokens, not bash.
# shellcheck disable=SC2016
SUBST_VARS='${WKS_CLIENT_SLUG} ${WKS_PUBLIC_URL} ${WKS_DB_PASSWORD} ${WKS_MINIO_ROOT_PASSWORD} ${WKS_ADMIN_EMAIL} ${WKS_ADMIN_PASSWORD} ${WKS_LICENSE_PATH} ${WKS_IMAGE_TAG} ${WKS_JWT_SECRET} ${WKS_CLIENT_LICENSE_HOST_PATH} ${WKS_HOST_PORT}'
envsubst "$SUBST_VARS" < "$TEMPLATE" > "$RENDERED_COMPOSE"
echo "Rendered compose: ${RENDERED_COMPOSE}"

# Validate compose syntax.
if ! docker compose -f "$RENDERED_COMPOSE" config -q; then
  echo "ERROR: rendered compose failed validation." >&2
  exit 1
fi

# --- Step 4: bring up -------------------------------------------------------
PROJECT="wks-${SLUG}"
echo "Bringing up ${PROJECT}..."
docker compose -p "$PROJECT" -f "$RENDERED_COMPOSE" up -d --pull always

# --- Step 5: poll /api/health ----------------------------------------------
HEALTH_URL="http://localhost:${WKS_HOST_PORT}/api/health"
echo "Polling ${HEALTH_URL} (up to 90s)..."
deadline=$((SECONDS + 90))
healthy=0
while [[ $SECONDS -lt $deadline ]]; do
  if curl -fsS "$HEALTH_URL" >/dev/null 2>&1; then
    healthy=1
    break
  fi
  sleep 3
done
if [[ $healthy -ne 1 ]]; then
  echo "ERROR: ${HEALTH_URL} did not become healthy within 90s." >&2
  echo "       Inspect: docker compose -p ${PROJECT} -f ${RENDERED_COMPOSE} logs" >&2
  exit 1
fi
echo "Health OK: ${HEALTH_URL}"

# --- Step 6: runtime invariant probe ----------------------------------------
PROBE="deploy/probes/check-runtime-no-tenant-id.sh"
if ! "$PROBE" "http://localhost:${WKS_HOST_PORT}"; then
  echo "ERROR: runtime ZERO-tenant_id invariant probe FAILED." >&2
  echo "       See Decision 25 / docs/zero-tenant-id.md." >&2
  exit 1
fi

# --- Step 7: emit handover summary -----------------------------------------
echo ""
echo "================================================================="
echo "Deploy OK for client: ${SLUG}"
echo "  URL (public):     ${WKS_PUBLIC_URL}"
echo "  URL (host port):  http://localhost:${WKS_HOST_PORT}"
echo "  Image:            ${WKS_IMAGE_TAG}"
echo "  Compose project:  ${PROJECT}"
echo "  Runbook:          ${RUNBOOK}"
echo "================================================================="
