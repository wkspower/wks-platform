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
  — how to report a vulnerability and the remediation targets.

:::note What's binding
The lifecycle below is a *best-effort* commitment, not a contract. Commercial
support terms become binding only via a signed support agreement — see
[Commercial support](#commercial-support).
:::

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
| **Out-of-band patch** | As required | Critical/High security fixes that can't wait for the next patch (see [SECURITY.md](https://github.com/wkspower/wks-platform/blob/main/SECURITY.md#remediation-targets-community)) |

Like [PostgreSQL](https://www.postgresql.org/support/versioning/), we distinguish
**scheduled** releases from **out-of-band** ones: routine fixes accumulate into
the next scheduled patch, while an actively-exploited or Critical vulnerability
triggers an immediate out-of-band release on the affected line.

## Support window (Community)

We actively support the **current** and the **previous** minor line. The rule is
positional, so it's easy to reason about without tracking per-version dates:

- When a new minor reaches **GA**, the line it displaces to *third-oldest* enters
  a **90-day migration grace period**, then goes end-of-life (EOL).
- During a grace period, three lines briefly receive fixes (current, previous,
  and the line winding down) — giving operators time to upgrade.
- At our ~6-month cadence this works out to **roughly 12–15 months** of support
  per minor. That duration is a *consequence* of the cadence, not an independent
  guarantee — if releases come faster, the window is shorter.

| Version | Status | Stack | Supported until |
|---|---|---|---|
| `1.5.x` | **Current** — the Stabilization release | Spring Boot 4.0.6 · Java 21 · Camunda 7.24 · zero Critical/High vulns | Active |
| `1.4.x` | **Previous stable** | Java 17 · Spring Boot 3 | `1.6.0` GA + 90-day grace |
| `< 1.4` | End-of-life | — | Unsupported — please upgrade |

---

## Commercial support

Extended support for older releases and priority response are available to
commercial users. To discuss a support agreement,
**[contact us](https://wkspower.com/contact-us-wks-platform)**.

## Release approval

Every release — community or commercial — passes through a **human approval gate**
before it ships: maintainers review and publish each tagged release. Remediation
SLAs therefore measure *time to a release-ready fix*, which is what lets us offer
tight targets while keeping releases trustworthy.

## Security fixes

Security fixes follow the cadence above and the targets in
[`SECURITY.md`](https://github.com/wkspower/wks-platform/blob/main/SECURITY.md):
Critical fixes target 72 hours to release-ready and may ship out-of-band;
High/Medium/Low ride the appropriate patch or scheduled release. Each fix is
correlated with a
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
