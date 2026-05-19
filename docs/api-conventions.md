# WKS API conventions

Source of truth for every HTTP surface the backend ships.

## Envelope

Every response — success or error — uses the same wrapper.

**Success** (single):

```json
{ "data": { ... }, "meta": {} }
```

**Success** (list):

```json
{ "data": [ ... ], "meta": { "total": 42, "page": 0, "size": 20 } }
```

**Error**:

```json
{ "error": { "code": "WKS-API-003", "message": "size must be >= 1", "field": "size" }, "meta": {} }
```

**Error** (multi, 422):

```json
{
  "error": {
    "code": "WKS-CFG-000",
    "message": "Configuration invalid",
    "errors": [
      { "code": "WKS-CFG-001", "message": "name missing", "field": "name", "line": 3 },
      { "code": "WKS-CFG-008", "message": "status 'foo' not declared", "field": "status", "line": 17 }
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

- `page` is **zero-based**, default `0`.
- `size` defaults to `20`, clamps silently to `[1, 100]` above `100`, raises `WKS-API-003` at 400 below `1` or for negative `page`.
- `sort` is repeatable (`?sort=updatedAt,desc&sort=createdAt,asc`). Direction defaults to `asc`.

Responses include `meta.total`, `meta.page`, `meta.size`.

**Producer pattern**:

```java
Pageable pageable = PageRequestParams.of(page, size, sort).toPageable(SORT_WHITELIST);
Page<MyEntity> page = repository.findAll(pageable);
return PageMetaBuilder.paged(page, MyMapper::toDto);
```

- Each list controller declares a sort allow-list (typically a `Set<String>` constant).
- Unknown sort properties raise `WKS-API-004` — never fall through to Hibernate.
- `PageMetaBuilder.paged(...)` is the **only** path for building list responses.

## Dates and IDs

- Every timestamp on the wire is ISO 8601 with UTC offset: `"2026-04-24T14:22:18Z"`. Never millis, never arrays.
- `WRITE_DATES_AS_TIMESTAMPS` is disabled in `JacksonConfig`. Don't re-enable via `spring.jackson.*` yaml.
- Every identifier in request/response bodies is `java.util.UUID`. Never auto-increment integers.

## Error codes

Codes are defined once in `domain/exception/ErrorCode.java`. Wire strings are part of the public contract — never renumber or reuse a code once shipped. See [error-codes.md](./error-codes.md) for the full registry and conventions.

Common codes you'll see at the API edge:

| Code | HTTP | Meaning |
|---|---|---|
| `WKS-API-002` | 400 | JSON parse error |
| `WKS-API-003` | 400 | Pagination param out of range |
| `WKS-API-004` | 400 | Unknown sort property |
| `WKS-API-401` | 401 | Authentication failed |
| `WKS-API-403` | 403 | Forbidden |
| `WKS-API-404` | 404 | Resource not found |
| `WKS-API-413` | 413 | Multipart upload over cap |
| `WKS-CFG-000` | 422 | Multi-error config aggregate |
| `WKS-RTM-409` | 409 | Optimistic-locking conflict |
| `WKS-RTM-500` | 500 | Uncaught exception |

## Endpoints

### Case CRUD

| Method | Path | Verb gate | Wire success |
|---|---|---|---|
| `POST` | `/api/cases` | `create` | `201 ApiResponse<CaseDto>` |
| `GET` | `/api/cases/{id}` | `view` | `200 ApiResponse<CaseDto>` (embedded `caseType`) |
| `GET` | `/api/cases` | `view` | `200 ApiResponse<List<CaseSummaryDto>>` + meta |
| `PUT` | `/api/cases/{id}` | `view` | `200 ApiResponse<CaseDto>` |

`POST` body: `{ "caseTypeId", "data", "assignee" }`. `PUT` body: `{ "data", "version" }` — version mismatch surfaces as `WKS-RTM-409`.

The list endpoint paginates via the standard envelope; sort allow-list is `[updatedAt, createdAt, status]`. The JSON `data` column is server-side projected — list rows carry only system columns plus the case-type's `listColumns` projections.

### Transitions and tasks

All three gate on the `transition` verb.

| Method | Path | Wire success |
|---|---|---|
| `POST` | `/api/cases/{id}/transition` | `200 ApiResponse<CaseDto>` |
| `POST` | `/api/tasks/{id}/complete` | `200 ApiResponse<TaskActionResponse>` |
| `POST` | `/api/tasks/{id}/claim` | `200 ApiResponse<TaskActionResponse>` |

`/transition` body: `{ "action", "variables" }`. `action` is the BPMN message name attached to an `<bpmn:intermediateCatchEvent>` / `<bpmn:receiveTask>`. Unknown / non-enabled actions return `WKS-RTM-409`.

`/complete` body: `{ "variables" }` (optional). Response carries `archetype` from the user task's `<camunda:property name="archetype" .../>` — the frontend's `TaskLifecycleButton` reads this to drive async UI states.

`/claim` has no body. Already-claimed tasks (by anyone) return `WKS-RTM-409`.

Status changes are pushed by an engine-side `ExecutionListener` (`CaseStatusListener`) that writes through `CaseStatusUpdater` and publishes `CaseStatusChanged` via `EventPublisher`.

### Case types

Read-only public surface, gated by per-case-type `view`.

`GET /api/case-types` — sorted by `displayName`, returns id + counts + `permissions[]` (verb subset the caller holds).

`GET /api/case-types/{id}` — full `fields[]`, `statuses[]`, `listColumns[]`. Field entries are flattened `FieldView` records carrying per-type validation slots (`minLength`/`maxLength`/`min`/`max`/`step`/`dateMin`/`dateMax`/`options[]`/`maxBytes`/`allowedMimeTypes`). The wire field names mirror the YAML grammar tokens exactly.

`field.type` and `status.color` are emitted as lowercase wire tokens. `roles[]` and the workflow `bpmn` reference are not echoed.

## Interactive docs

`GET /v3/api-docs` returns the OpenAPI 3 JSON; Swagger UI lives at `GET /swagger-ui/index.html`. Both reachable unauthenticated under the dev profile. In production they are disabled by default; set `WKS_OPENAPI_ENABLED=true` to opt in.

The generated spec declares a `cookieAuth` security scheme (`type: apiKey, in: cookie, name: WKS_SESSION`) applied globally.
