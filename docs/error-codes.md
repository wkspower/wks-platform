# WKS error code registry

`WKS-XXX-NNN` strings are a **wire contract**: SI integrators, snapshot tests, and customer runbooks grep against the exact text. Once a code ships it must never be renumbered, reused for a different meaning, or silently retired.

## Source of truth

The enum at `backend/src/main/java/com/wkspower/platform/domain/exception/ErrorCode.java` is canonical. Each constant carries its meaning in the Javadoc. HTTP status mapping is intentionally **not** on the enum — it lives in `backend/src/main/java/com/wkspower/platform/api/GlobalExceptionHandler.java` so the domain layer stays transport-free.

The flat enum shape (no sub-enums) is deliberate — it keeps "find every code" a one-line grep.

## Prefix legend

Prefixes are **namespace, not lifecycle phase.** A single prefix may host both deploy-time and runtime codes (e.g. `WKS-MAP` carries deploy-time validation and runtime miss `404`). The band groups by domain owner, not by when the code fires.

| Prefix | Domain |
|---|---|
| `WKS-API` | Transport, request shape, auth, production bootstrap fail-closed |
| `WKS-CFG` | Case-type + BPMN + mapping deploy-time validation |
| `WKS-MAP` | Mapping layer (deploy validation + runtime rule miss) |
| `WKS-STG` | Stage lifecycle + stage-scoped status sets |
| `WKS-VER` | Case-type version registry |
| `WKS-FORM` | Form definition schema validation |
| `WKS-DOC` | Document upload validation + storage runtime |
| `WKS-LIC` | License verification + EE feature gating |
| `WKS-RTM` | Runtime conflict (`409`) / last-resort (`500`) |
| `WKS-EDIT` | Edit-permission gating |
| `WKS-ROUTE` | Routing |
| `WKS-STAT` | Status |
| `WKS-ARCH` | Architecture |

## Conventions

- Validators **collect every offence** in one pass — clients see them under `error.errors[]`. Never fail-on-first.
- 422 multi-error aggregates carry the umbrella code `WKS-CFG-000`; individual offences carry their specific code plus `field` (dotted path) and `line` (1-based YAML line, when derivable).
- `field` slot in API 422s carries the YAML-declared id verbatim (e.g. `applicant_name`), not a JSON-Pointer fragment.
- Reserved gaps in a band (e.g. `WKS-CFG-014..019`) are intentional — do not renumber to fill them.

## Adding a code

1. Add the constant + Javadoc to `ErrorCode.java`.
2. Wire it into the appropriate thrower / validator.
3. If it deserves customer-facing documentation, write a paragraph in this file under the prefix.

Do not pre-allocate code numbers for unwritten features.
