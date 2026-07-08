# Changelog — The WKS Case Configuration Standard

All notable changes to the configuration schemas are documented here. The
`schemaVersion` on config documents and the npm package version track these.

## 2.1.0 — Document requirements

Additive, backward-compatible extension of the 2.0 contract (minor bump).
Existing documents remain valid — the new field is optional.

- **Case Definition** gains an optional `requiredDocuments` array: the documents
  a case of this type is expected to hold (`id`, `label`, optional
  `description`, `required` (default true), advisory `acceptedFileTypes` /
  `maxSizeBytes`). Drives the case Documents checklist and completeness.
- Uploaded documents reference a requirement via `CaseDocument.requirementId`
  to mark it satisfied. Absent means a free-form attachment.
- No presentation or storage change: this is the config-declared **discipline**
  layer; storage mode and document lifecycle are tracked as later DMS slices.

## 2.0.0 — Board config extracted (BREAKING)

Removes the deprecated presentation field from the core contract and gives board
rendering its own versioned artifact.

- **BREAKING: `kanbanConfig` removed from the Case Definition contract.** It was a
  presentation concern, not part of the case-definition data model. Board
  **columns are derived from `stages`** (already the case in the portal), so no
  board information is lost.
- **New schema: `board-config.schema.json`** — a board configuration keyed by
  `caseDefinitionId`, carrying only card-presentation choices (`card.titleFields`,
  `card.contentFields`); columns come from the referenced case definition's stages.
  This is the artifact `kanbanConfig` is extracted into.
- Backend: `kanbanConfig` dropped from `CaseDefinition`, its JPA converter, the
  `kanban_config` column, and the Mongo write path. Seeds re-stamped to `2.0` with
  `kanbanConfig` removed.
- Portal: the Case Builder "Kanban" tab is retired (columns derive from stages);
  the board renderer no longer reads `kanbanConfig`.
- `schemaVersion` semantics unchanged: **absent still means the `1.0` baseline**
  (a document with no stamp predates versioning).

Note: this release does not yet add a persisted board-config store — there is no
board data to migrate (all seed `kanbanConfig` was empty). Standing up a
board-config store/endpoint is a follow-up; the schema is published now so it can
be built against.

## 1.0.0 — Initial Standard

First published version of the WKS Case Configuration Standard, capturing the
existing config contract as-is (no behavior change):

- Schemas for **Case Definition**, **Form**, **Record Type**, **Queue**.
- Pinned hook vocabulary (`eventType`, `actionType`) matching the Java enums.
- Documented the `recordtype` Form.io extension previously implicit in the portal.
- Documented (not removed) legacy/storage fields seen in seed data (`bpmEngineId`, `_id`).
- Marked `kanbanConfig` **deprecated**: it is a presentation concern (board rendering, largely duplicating `stages`), not part of the core case-definition contract. Kept valid for backward compatibility.
- JS validator (`validate(type, doc)`) and the `dist/types.d.ts` derivation seam.

Baseline: documents without a `schemaVersion` are treated as `1.0`.

## Planned

- **Persisted board-config store** — an endpoint/repository backing
  `board-config.schema.json` so per-case-type card presentation can be authored and
  saved again (2.0 published the schema and derives columns from stages, but does
  not yet persist board configs).
- Beyond-schema structural validation (dangling stage references, `formKey`
  existence) at the backend command layer.
