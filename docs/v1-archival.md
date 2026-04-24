# v1 Archival

WKS Platform v1 is archived. v2 is a full rewrite on this repository's
default branch. If you are landing here from the v1 README or a v1
link, this page summarises what that means.

## Four things to know

1. **Frozen — no new features, no security updates.** The last v1
   release is tagged [`v1-final`](https://github.com/wkspower/wks-platform/releases/tag/v1-final).
   No further commits will land on the v1 codebase. See
   [`SECURITY.md`](../SECURITY.md#supported-versions) for the supported-
   versions table.

2. **Why a rewrite.** v1 was a monorepo of seven microservices with a
   shared Docker network and Keycloak + OPA for authentication.
   Elegant on a whiteboard; brutal in the first 30 minutes of
   evaluation. An open-source case-management platform lives or dies
   on that first 30 minutes. v2 is a single-container monolith with
   strict hexagonal boundaries so the evaluation path is
   `docker compose up` → open localhost → evaluate.

3. **v1 is still readable.** The v1 code is preserved on the
   [`v1` branch](https://github.com/wkspower/wks-platform/tree/v1) and
   at the [`v1-final` tag](https://github.com/wkspower/wks-platform/releases/tag/v1-final).
   Clone, browse, fork — nothing is deleted or moved. The v1 branch's
   own `README.md` is retained untouched: editing a frozen branch
   would contradict the "no further changes" promise this archival
   makes.

4. **v2 is reachable from v1.** The `v1` branch lives inside this
   repository (`wkspower/wks-platform`). Navigating one level up from
   `/tree/v1` lands on the default branch, which serves v2. There is
   no separate v1 repo and no separate v2 repo — both are branches of
   the same canonical project.

## What this means for existing v1 users

- **Keep running v1 if you need to**, but plan a migration. No
  security updates will land.
- **Do not expect compatibility.** v2's data model, API surface, and
  configuration format all differ from v1. An automated upgrade path
  is tracked on the public [`ROADMAP.md`](../ROADMAP.md) under
  _Later → Platform evolution_.
- **Follow the default branch of this repo** for v2 progress, or see
  [`ROADMAP.md`](../ROADMAP.md) for the shipped / next / later view.
