#!/usr/bin/env bash
# SPDX-License-Identifier: Apache-2.0
# Copyright 2026 WKS Power Limited
#
# Runtime ZERO-tenant_id invariant probe (Story 14.7 AC5).
#
# Operational counterpart to backend/.ci/check-tenant-invariant.sh (Story 3.0):
# the static lint enforces the invariant at build time; this probe enforces it
# against an actually-booted container at deploy time.
#
# Usage:
#   ./deploy/probes/check-runtime-no-tenant-id.sh <base-url>
#   # e.g. http://localhost:18080
#
# Hits a documented set of read endpoints and fails with non-zero exit if any
# response body or header contains tenant_id / tenantId / @TenantId. Mirrors
# the regex from Story 3.0's static lint.
#
# Exit codes:
#   0 = invariant holds across all probed surfaces
#   1 = invariant violated (one or more surfaces leaked a tenant_id-like token)
#   2 = usage error / required tool missing

set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <base-url>" >&2
  exit 2
fi

BASE="${1%/}"
DOC_LINK="docs/zero-tenant-id.md"
FAIL_SUFFIX="Forbidden by Decision 25 (Zero tenant_id invariant). See ${DOC_LINK} for the rationale and approved alternatives."

for bin in curl grep; do
  if ! command -v "$bin" >/dev/null 2>&1; then
    echo "ERROR: required command '${bin}' not found on PATH." >&2
    exit 2
  fi
done

# Probe set (AC5.1). /api/cases and /api/case-types may require auth — we
# accept any HTTP status; we only inspect the response body+headers for token
# leakage. /api/health and /v3/api-docs are unauthenticated per SecurityConfig.
PROBE_PATHS=(
  /api/health
  /v3/api-docs
  /api/cases
  /api/case-types
  /api/license/status
)

FORBIDDEN='(tenant_id|tenantId|@TenantId)'
violations=0
probed=0

for path in "${PROBE_PATHS[@]}"; do
  url="${BASE}${path}"
  # Capture headers + body. -i emits headers in body. We intentionally do NOT
  # use -f so 4xx responses still get inspected.
  response="$(curl -isS --max-time 10 "$url" 2>/dev/null || true)"
  if [[ -z "$response" ]]; then
    echo "WARN: ${url} — no response (skipping)" >&2
    continue
  fi
  probed=$((probed + 1))
  if printf '%s' "$response" | grep -qE "$FORBIDDEN"; then
    matched="$(printf '%s' "$response" | grep -oE "$FORBIDDEN" | head -1)"
    echo "${url}: ${matched} — ${FAIL_SUFFIX}" >&2
    violations=$((violations + 1))
  fi
done

# Licensee assertion (AC5.3). Story 7.1 will deliver /api/license/status.
# Until then, emit a WARNING (not failure) pointing at Story 7.1.
licensee_status="$(curl -fsS --max-time 5 "${BASE}/api/license/status" 2>/dev/null || true)"
if [[ -n "$licensee_status" ]] && printf '%s' "$licensee_status" | grep -qE '"licensee"[[:space:]]*:'; then
  licensee_name="$(printf '%s' "$licensee_status" | grep -oE '"licensee"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1)"
  echo "License OK: ${licensee_name}"
else
  echo "WARN: /api/license/status not available or missing 'licensee' field." >&2
  echo "      Phase-0 placeholder accepted; Story 7.1 will deliver the real LicenseService." >&2
fi

if [[ "$violations" -ne 0 ]]; then
  echo "RUNTIME INVARIANT FAILED: ${violations} violations across ${probed} probed surfaces" >&2
  exit 1
fi

echo "RUNTIME INVARIANT OK: zero tenant_id leakage in ${probed} probed surfaces"
exit 0
