# Zero `tenant_id` Invariant (Decision 25)

## The invariant

The application code in this repository must contain **zero** `tenant_id` (or `tenantId`, `WHERE tenant_id`, `@TenantId`) references inside the protected packages: `backend/src/main/java/com/wkspower/platform/domain/`, `backend/src/main/java/com/wkspower/platform/infrastructure/persistence/`, and `backend/src/main/java/com/wkspower/platform/audit/`. WKS Platform v2 ships as a per-instance product — every customer runs their own image, database, and license file — so application-layer tenant scoping is structurally redundant and structurally forbidden.

## Why

Per-instance isolation (Decision 25) is the architectural foundation that lets WKS sell as both OSS and per-client managed hosting under the license-per-instance model (Decision 24, cross-referenced). Once `tenant_id` enters a domain row or a persistence query, multi-tenancy semantics leak into the model and the per-instance posture is compromised — the surface stops being equivalent across deployments and SI partners cannot reason about isolation by reading the code.

## Approved alternatives

If you reach for `tenant_id`, use one of these instead:

- **Per-environment deploy** — `wks deploy <client>` provisions a distinct image, database, URL, and license file for each customer. The "tenant" is the running instance.
- **`LicenseService.licensee`** — the customer name from the boot-time license (Decision 24) surfaces in the admin banner and audit-export header. It is identification only; it does **not** flow into domain rows or queries.
- **Infrastructure-layer scoping** — file paths, DB connection strings, MinIO buckets are configured per environment, outside application code.

## Escape hatch

If you genuinely need a tenant concept, you are violating D25 — open a PR amending `architecture.md` before touching code.

## CI enforcement

The lint script `backend/.ci/check-tenant-invariant.sh` greps the protected packages for the forbidden identifiers on every PR via the GitHub Actions job `Tenant invariant (D25)` in `.github/workflows/ci.yml`. The script is a single source of truth — invoke it locally before pushing.

The scan is textual: it flags the forbidden identifiers anywhere in a file under the protected packages — code, comments, Javadoc, and string literals alike. This is intentional. Comments saying "we used to use `tenantId`" or strings containing the word still signal pre-pivot thinking that an SI partner reading the code would see, which D25 forbids. If you genuinely need to reference the term in prose (e.g., explaining the invariant itself), do it from outside the protected packages.
