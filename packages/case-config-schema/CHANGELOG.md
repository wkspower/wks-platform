# Changelog — The WKS Case Configuration Standard

All notable changes to the configuration schemas are documented here. The
`schemaVersion` on config documents and the npm package version track these.

## 1.1.0 — Document requirements

Additive, backward-compatible extension (minor bump). Documents without a
`schemaVersion`, or set to `1.0`, remain valid — the new field is optional.

- **Case Definition** gains an optional `requiredDocuments` array: the documents
  a case of this type is expected to hold (`id`, `label`, optional
  `description`, `required` (default true), advisory `acceptedFileTypes` /
  `maxSizeBytes`). Drives the case Documents checklist and completeness.
- Uploaded documents reference a requirement via `CaseDocument.requirementId`
  to mark it satisfied. Absent means a free-form attachment (pre-1.1 behavior).
- No presentation or storage change: this is the config-declared **discipline**
  layer; storage mode and document lifecycle are tracked as later DMS slices.

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

- **Extract board configuration** out of `CaseDefinition.kanbanConfig` into a
  separate **board-config** schema that references a case definition by id
  (presentation concern, kept orthogonal to the core contract). This is a
  behavior-changing slice — model, JPA converter, portal Kanban tab, and seed —
  tracked separately from the documentation/versioning work.
