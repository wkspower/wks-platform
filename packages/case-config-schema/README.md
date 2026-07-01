# @wkspower/case-config-schema — The WKS Case Configuration Standard

Canonical, versioned JSON Schemas for **case-type configuration** in the WKS Platform, plus a small validator. This package is the **single source of truth** for the config contract: the backend validates against it, the portal validates and renders against it, and tooling generates from it.

A case type in WKS is config-driven JSON. This Standard makes that config explicit, versioned, documented, and machine-checkable — instead of an implicit shape duplicated across the engine, the portal, and the seed data.

## What's in the Standard

| Type | Schema | Notes |
|------|--------|-------|
| Case Definition | [`schemas/case-definition.schema.json`](schemas/case-definition.schema.json) | Case type: stages, hooks, kanban, form/process links |
| Form | [`schemas/form.schema.json`](schemas/form.schema.json) | Form.io structure (validated permissively) |
| Record Type | [`schemas/record-type.schema.json`](schemas/record-type.schema.json) | Auxiliary structured data |
| Queue | [`schemas/queue.schema.json`](schemas/queue.schema.json) | Work queue |

**Extensions** (non-standard additions, documented as first-class):
- [`schemas/ext/form-recordtype.schema.json`](schemas/ext/form-recordtype.schema.json) — the WKS `recordtype` Form.io component the portal rewrites at load time.
- [`schemas/ext/case-hook-vocabulary.schema.json`](schemas/ext/case-hook-vocabulary.schema.json) — the `eventType`/`actionType` enum vocabulary, pinned in lockstep with the Java enums.

Each top-level schema is **self-contained** (internal `$defs` only, no cross-file `$ref`) so it validates identically across JSON Schema validators in any language — JS (ajv) and Java (networknt) load the exact same files.

## Usage (JavaScript)

```js
const { validate, SCHEMA_VERSION } = require('@wkspower/case-config-schema');

const { valid, errors } = validate('case-definition', myCaseDef);
if (!valid) console.error(errors);
```

## Versioning

- The **`schemaVersion`** field on each config document records which version of the Standard it conforms to.
- Semver-style `MAJOR.MINOR` string. Bump **minor** for additive/backward-compatible changes, **major** for breaking ones.
- Absent/null `schemaVersion` is treated as the **`1.0`** pre-versioning baseline (tolerant read), so legacy and seed data validate without migration.
- The npm package `version` tracks the Standard version.

## Derivation seam (generative asset)

`npm run generate` produces `dist/types.d.ts` from the schemas — the first derivation proving the Standard is a *source of generation*, not just a validation target.

**Open seams for later** (not built yet; the boundary is drawn so they're additive):
- Java DTOs / a backend validator module generated from the same schemas.
- Example/fixture configs generated per type.
- `@wkspower/wks-components` — a schema-driven portal UI layer that renders editors from the Standard (the path toward a fully schema-first Case Builder).

## Tests

`npm test` validates fixtures (happy path + each guard) **and** the shipped seed data against the Standard — the anti-drift gate. The backend runs an equivalent check (`ConfigSchemaValidationTest`) against these same files in `mvn package`.
