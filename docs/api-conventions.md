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
| `WKS-CFG-000` | 422  | Multi-error config aggregate (umbrella for `WksConfigException`)         |
| `WKS-RTM-500` | 500  | Uncaught exception                                                       |

Codes are defined once in `domain/exception/ErrorCode.java`. Wire strings are part of the public
contract — never renumber or reuse a code once shipped.

## Interactive docs

`GET /v3/api-docs` returns the OpenAPI 3 JSON; Swagger UI lives at `GET /swagger-ui/index.html`.
Both are reachable unauthenticated under the dev profile. In production they are disabled by
default; set `WKS_OPENAPI_ENABLED=true` to opt in (see `application-production.yml` and
`SecurityConfig`).

The generated spec declares a `cookieAuth` security scheme (`type: apiKey, in: cookie, name:
WKS_SESSION`) applied globally — Swagger UI's "Try it out" will send the session cookie
automatically once you've logged in.
