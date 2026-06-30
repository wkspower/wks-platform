# Changelog — The WKS Case Configuration Standard

All notable changes to the configuration schemas are documented here. The
`schemaVersion` on config documents and the npm package version track these.

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
