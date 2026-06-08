---
sidebar_position: 3
---

# Support & Release Policy

This policy explains **how often wks-platform releases**, **how long each version
is supported**, and **how security patches reach you**. It is the user-facing
companion to two documents kept in the repository:

- [`CONTRIBUTING.md`](https://github.com/wkspower/wks-platform/blob/main/CONTRIBUTING.md)
  — the mechanics of branching, tagging, and cutting a release (for contributors).
- [`SECURITY.md`](https://github.com/wkspower/wks-platform/blob/main/SECURITY.md)
  — how to report a vulnerability and the remediation SLAs.

It is intentionally aligned with the lifecycle of our core framework,
[Spring Boot](https://spring.io/support-policy/), so the platform never outlives
the stack beneath it.

## Versioning

We follow [Semantic Versioning](https://semver.org): `MAJOR.MINOR.PATCH`.

- **MAJOR** — incompatible changes that require migration (e.g. a Spring
  Boot / Java baseline jump).
- **MINOR** — backward-compatible features and dependency modernization.
- **PATCH** — backward-compatible bug and security fixes on a released line.

Pre-releases use the `-rc.N` suffix (e.g. `v1.5.0-rc.1`) and are published for
validation before a final version is tagged.

## Release cadence

| Release type | Cadence | Contents |
|---|---|---|
| **Minor** (`x.Y.0`) | Roughly every 6 months | Features, dependency modernization, security rollups |
| **Patch** (`x.y.Z`) | As needed, typically monthly | Bug fixes and security fixes for a supported line |
| **Out-of-band patch** | As required | Critical/High security fixes that can't wait for the next patch (see [SECURITY.md](https://github.com/wkspower/wks-platform/blob/main/SECURITY.md#remediation-slas)) |

Like [PostgreSQL](https://www.postgresql.org/support/versioning/), we distinguish
**scheduled** releases from **out-of-band** ones: routine fixes accumulate into
the next scheduled patch, while an actively-exploited or Critical vulnerability
triggers an immediate out-of-band release on the affected line.

## Support window

Each **minor** line is supported with bug and security fixes for **at least 12
months** from its release, mirroring Spring Boot's open-source support window. In
practice we support the **current** and the **previous** minor line at any time:

| Version | Status | Stack | Supported until |
|---|---|---|---|
| `1.5.x` | **Current** — the Stabilization release | Spring Boot 4.0.6 · Java 21 · Camunda 7.24 · zero Critical/High vulns | Active |
| `1.4.x` | **Previous stable** | Java 17 · Spring Boot 3 | `1.5.0` GA + 90-day grace |
| `< 1.4` | End-of-life | — | Unsupported — please upgrade |

When a new minor reaches general availability, the line two minors back moves to
end-of-life after a **90-day grace period**, giving operators time to upgrade.

## Security fixes

Security fixes follow the cadence above and the SLAs in
[`SECURITY.md`](https://github.com/wkspower/wks-platform/blob/main/SECURITY.md):
Critical fixes target 72 hours and may ship out-of-band; High/Medium/Low ride the
appropriate patch or scheduled release. Each fix is correlated with a
[GitHub Security Advisory](https://github.com/wkspower/wks-platform/security/advisories)
and noted in the release notes for the tag that contains it.

To report a vulnerability, use
[private vulnerability reporting](https://github.com/wkspower/wks-platform/security/advisories/new)
— **not** a public issue. See [`SECURITY.md`](https://github.com/wkspower/wks-platform/blob/main/SECURITY.md)
for the full process.

## How releases are announced

Every release is published as a **GitHub Release** on its version tag; the
generated notes (derived from merged pull-request titles) are the canonical
changelog. User-facing changes are also reflected in this documentation site, and
the [Roadmap](./roadmap.md) tracks what's planned for upcoming versions.
