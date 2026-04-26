# WKS API conventions

Source of truth for every HTTP surface shipped by the backend. Story 1.4 set the shape; all later
stories extend it without reshaping.

## Envelope

Every response — success or error — uses the same wrapper.

**Success** (single resource):

```json
{ "data": { ... }, "meta": {} }
```

**Success** (list):

```json
{ "data": [ ... ], "meta": { "total": 42, "page": 0, "size": 20 } }
```

**Error** (single):

```json
{ "error": { "code": "WKS-API-003", "message": "size must be >= 1", "field": "size" }, "meta": {} }
```

**Error** (multi, 422 only):

```json
{
  "error": {
    "code": "WKS-CFG-000",
    "message": "Configuration invalid",
    "errors": [
      { "code": "WKS-CFG-101", "message": "name missing", "field": "name", "line": 3 },
      { "code": "WKS-CFG-102", "message": "status 'foo' not declared", "field": "status", "line": 17 }
    ]
  },
  "meta": {}
}
```

Rules:

- Success responses have no `error` key. Error responses have no `data` key. `@JsonInclude(NON_NULL)` keeps absent fields out.
- `meta` is always present and never `null`. Single-resource responses use `{}`.
- The envelope lives in `api/dto/` (`ApiResponse`, `ErrorPayload`, `domain/exception/ErrorDetail`). Domain services must not reference it.

## Pagination

Query params: `?page=0&size=20&sort=field[,asc|desc]`.

- `page` is **zero-based**. Default `0`.
- `size` defaults to `20`, clamps silently to `[1, 100]` above `100`, and raises `WKS-API-003` at 400 below `1` or for negative `page`.
- `sort` is repeatable (`?sort=updatedAt,desc&sort=createdAt,asc`). Direction defaults to `asc`; omitting the comma means ascending.

Responses include `meta.total`, `meta.page`, `meta.size`.

**Producer pattern**:

```java
Pageable pageable = PageRequestParams.of(page, size, sort).toPageable(SORT_WHITELIST);
Page<MyEntity> page = repository.findAll(pageable);
return PageMetaBuilder.paged(page, MyMapper::toDto);
```

- Each list controller implements `SortWhitelist` (typically as a `Set<String>` constant).
- Unknown sort properties raise `WKS-API-004` — never fall through to Hibernate.
- `PageMetaBuilder.paged(...)` is the **only** path for building list responses — do not hand-build `Map.of("total", …)`.

## Date + ID conventions

- Every timestamp on the wire is ISO 8601 with UTC offset: `"2026-04-24T14:22:18Z"`. Never millis, never arrays.
- `WRITE_DATES_AS_TIMESTAMPS` is disabled explicitly in `JacksonConfig`. Do not re-enable via `spring.jackson.*` yaml keys.
- Every identifier in request/response bodies is a `java.util.UUID`. Never auto-increment integers.

## Error codes (stable)

| Code          | HTTP | Reason                                                                   |
| ------------- | ---- | ------------------------------------------------------------------------ |
| `WKS-API-001` | 400  | Request body malformed / unreadable (reserved)                           |
| `WKS-API-002` | 400  | JSON parse error                                                         |
| `WKS-API-003` | 400  | Pagination param out of range (`size < 1`, `page < 0`, etc.)             |
| `WKS-API-004` | 400  | Unknown sort property (not in resource allow-list)                       |
| `WKS-API-005` | 400  | Unknown sort direction (not `asc` / `desc`)                              |
| `WKS-API-401` | 401  | Authentication failed                                                    |
| `WKS-API-403` | 403  | Forbidden                                                                |
| `WKS-API-404` | 404  | Resource not found                                                       |
| `WKS-API-413` | 413  | Multipart upload exceeds the 1 MB per-part / 2 MB per-request cap        |
| `WKS-CFG-000` | 422  | Multi-error config aggregate (umbrella for `WksConfigException`)         |
| `WKS-RTM-409` | 409  | Optimistic-locking conflict (case update raced with another transaction) |
| `WKS-RTM-500` | 500  | Uncaught exception                                                       |

Codes are defined once in `domain/exception/ErrorCode.java`. Wire strings are part of the public
contract — never renumber or reuse a code once shipped.

### Config validation codes (WKS-CFG-001..099)

Case-type YAML validation (Story 2.1). Every entry carries `field` (dotted JSON-Pointer-flavour
path — e.g. `fields[2].type`, `roles[0].permissions[1]`, 0-based array indices) and `line`
(1-based YAML line number when derivable). Collected all at once; the validator never fails on
first error.

| Code          | HTTP | Reason                                                                                 |
| ------------- | ---- | -------------------------------------------------------------------------------------- |
| `WKS-CFG-001` | 422  | Required key missing (top-level or nested)                                             |
| `WKS-CFG-002` | 422  | Invalid field `type` — not one of `text\|number\|date\|select\|checkbox\|textarea\|file` |
| `WKS-CFG-003` | 422  | Duplicate id (field / status / role / listColumn)                                      |
| `WKS-CFG-004` | 422  | `fields.length > 50` (or `roles > 20`, `permissions per role > 10`, `options > 50`)    |
| `WKS-CFG-005` | 422  | `listColumns.length > 12` OR references unknown field id / system column               |
| `WKS-CFG-006` | 422  | `statuses.length > 10`                                                                 |
| `WKS-CFG-007` | 422  | Any `displayName` exceeds 40 characters                                                |
| `WKS-CFG-008` | 422  | Unknown enum literal (status color, role permission verb)                              |
| `WKS-CFG-009` | 422  | Malformed id — fails `[a-z][a-z0-9-]{1,62}` (field ids also accept `_`)                |
| `WKS-CFG-011` | 422  | Registry rejected `replace` — incoming version older than registered (not validator)   |
| `WKS-CFG-099` | 422  | YAML parse / I/O failure (catastrophic)                                                |

### BPMN validation codes (WKS-CFG-010..099)

BPMN parse + structural validation (Story 2.2). Runs after YAML validation and before the engine
deploy on `POST /api/admin/deploy` and the startup loader's BPMN-present path. Same collect-all
discipline as the YAML validator — every offending user task / expression surfaces a separate
`ErrorDetail` rather than fail-on-first.

| Code          | HTTP | Reason                                                                                 |
| ------------- | ---- | -------------------------------------------------------------------------------------- |
| `WKS-CFG-010` | 422  | BPMN file missing / unreadable / not a BPMN 2.0 document                               |
| `WKS-CFG-012` | 422  | Variable in BPMN expression not declared in the YAML case type (and not in the well-known set: `taskAssignee`, `caseId`, `caseStatus`) |
| `WKS-CFG-020` | 422  | User task missing the required `archetype` declaration in `camunda:properties` (allowed: `draft_section`, `submit_for_processing`, `business_final`) |
| `WKS-CFG-021` | 422  | Archetype contradiction — `business_final` carries `camunda:asyncAfter="true"`, OR `draft_section` has an outgoing flow targeting another task |

Codes `013..019` and `022..099` are reserved for future BPMN findings — do not fill in this band
until a real validator failure mode needs a stable code.

> **Variance from `architecture.md` §Decision 14.** That table allocates `WKS-CFG-100..199` to
> BPMN validation. Story 2.2 follows the epic AC's `010..099` band so all "deploy-time validation"
> codes stay contiguous below 100. Architecture doc gets a follow-up patch.

## Case CRUD endpoints (Story 2.3)

The case-lifecycle surface is rooted at `/api/cases`:

| Method | Path                | Verb gate (per case-type YAML) | Wire success                                          |
| ------ | ------------------- | ------------------------------ | ----------------------------------------------------- |
| `POST` | `/api/cases`        | `create`                       | `201 ApiResponse<CaseDto>`                            |
| `GET`  | `/api/cases/{id}`   | `view`                         | `200 ApiResponse<CaseDto>` (with embedded `caseType`) |
| `GET`  | `/api/cases`        | `view`                         | `200 ApiResponse<List<CaseSummaryDto>>` + meta        |
| `PUT`  | `/api/cases/{id}`   | `view` (Phase 0 simplification — `edit` arrives in Story 5.2) | `200 ApiResponse<CaseDto>` |

`POST` request body: `{ "caseTypeId": "...", "data": { ... }, "assignee": "uuid|null" }`.
`PUT` request body: `{ "data": { ... }, "version": <expected version> }` — version mismatch surfaces
as `WKS-RTM-409` with HTTP 409.

The `documentCount` field on `CaseDto` is always `0` until Epic 3 (Documents). The shape is frozen
here so Story 3.2 can fill it without a DTO bump.

The list endpoint paginates via the standard `?page=...&size=...&sort=...` envelope; the sort
allow-list is `[updatedAt, createdAt, status]`. The JSON `data` column is never fetched on the
list path (server-side projection) — list rows carry only system columns plus the case-type's
`listColumns` field projections.

## Interactive docs

`GET /v3/api-docs` returns the OpenAPI 3 JSON; Swagger UI lives at `GET /swagger-ui/index.html`.
Both are reachable unauthenticated under the dev profile. In production they are disabled by
default; set `WKS_OPENAPI_ENABLED=true` to opt in (see `application-production.yml` and
`SecurityConfig`).

The generated spec declares a `cookieAuth` security scheme (`type: apiKey, in: cookie, name:
WKS_SESSION`) applied globally — Swagger UI's "Try it out" will send the session cookie
automatically once you've logged in.
