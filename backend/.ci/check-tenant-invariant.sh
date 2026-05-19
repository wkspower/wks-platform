#!/usr/bin/env bash
# Tenant invariant (D25) — Zero tenant_id lint
#
# Phase 0 scope: backend domain/persistence/audit (Java) + Flyway migrations
# (SQL/XML under src/main/resources/db/migration). Frontend has no equivalent
# packages today; expand the SCAN_PATHS variable when frontend grows a domain
# layer (post-Epic 6 if Developer Console introduces one).
#
# Source of truth: docs/zero-tenant-id.md
#
# Invoked from .github/workflows/ci.yml and runnable locally from any cwd.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$REPO_ROOT"

SCAN_PATHS=(
  "backend/src/main/java/com/wkspower/platform/domain"
  "backend/src/main/java/com/wkspower/platform/infrastructure/persistence"
  "backend/src/main/java/com/wkspower/platform/audit"
  "backend/src/test/java/com/wkspower/platform/domain"
  "backend/src/test/java/com/wkspower/platform/infrastructure/persistence"
  "backend/src/test/java/com/wkspower/platform/audit"
  "backend/src/main/resources/db/migration"
)
INCLUDE_GLOBS=(--include='*.java' --include='*.sql' --include='*.xml')

FORBIDDEN='(tenant_id|tenantId|WHERE[[:space:]]+tenant_id|@TenantId)'
ALLOWLIST_FILE="backend/.ci/tenant-invariant-allowlist.txt"
DOC_LINK="docs/zero-tenant-id.md"
FAIL_SUFFIX="Forbidden by Decision 25 (Zero tenant_id invariant). See ${DOC_LINK} for the rationale and approved alternatives."

# Load allowlist globs (skip blank/# lines). Tolerate missing file.
# Strip trailing CR so Windows-edited allowlists do not silently fail to match.
allowlist=()
if [[ -f "$ALLOWLIST_FILE" ]]; then
  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line%$'\r'}"
    case "$line" in
      ''|\#*) continue ;;
      *) allowlist+=("$line") ;;
    esac
  done < "$ALLOWLIST_FILE"
fi

is_allowlisted() {
  local path="$1"
  local glob
  for glob in "${allowlist[@]+"${allowlist[@]}"}"; do
    # shellcheck disable=SC2254
    case "$path" in
      $glob) return 0 ;;
    esac
  done
  return 1
}

# Build existing-paths list. Tolerate individual missing paths (first-run /
# refactor safety), but FAIL if every configured path is missing — that almost
# certainly means a rename or refactor moved the protected packages and the
# scan would otherwise vacuously pass.
existing=()
for p in "${SCAN_PATHS[@]}"; do
  [[ -d "$p" ]] && existing+=("$p")
done

if [[ ${#existing[@]} -eq 0 ]]; then
  echo "Tenant invariant (D25) FAILED: none of the configured SCAN_PATHS exist." >&2
  echo "If the protected packages were renamed, update SCAN_PATHS in $0 in the same PR." >&2
  exit 1
fi

violations=0
files_seen=""  # newline-delimited; uniqued at end (bash 3.2 has no assoc arrays)

# grep -I skips binary files; --include filters keep migration tree narrow.
# grep returns non-zero when no matches — tolerate via || true.
while IFS= read -r match; do
    [[ -z "$match" ]] && continue
    # match format: path:line:content
    path="${match%%:*}"
    rest="${match#*:}"
    line="${rest%%:*}"
    content="${rest#*:}"

    if is_allowlisted "$path"; then
      continue
    fi

    # Identify which forbidden token matched (first hit on the line).
    if [[ "$content" =~ WHERE[[:space:]]+tenant_id ]]; then
      ident="WHERE tenant_id"
    elif [[ "$content" =~ @TenantId ]]; then
      ident="@TenantId"
    elif [[ "$content" =~ tenantId ]]; then
      ident="tenantId"
    elif [[ "$content" =~ tenant_id ]]; then
      ident="tenant_id"
    else
      ident="(unknown)"
    fi

    printf '%s:%s: %s — %s\n' "$path" "$line" "$ident" "$FAIL_SUFFIX"
    violations=$((violations + 1))
    files_seen="${files_seen}${path}"$'\n'
done < <(grep -IRnE "${INCLUDE_GLOBS[@]}" "$FORBIDDEN" "${existing[@]}" || true)

if [[ "$violations" -eq 0 ]]; then
  echo "Tenant invariant (D25) OK"
  exit 0
fi

file_count=$(printf '%s' "$files_seen" | sort -u | grep -c .)
echo "Tenant invariant (D25) FAILED: ${violations} violations across ${file_count} files"
exit 1
