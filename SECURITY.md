# Security Policy

The wks-platform team takes the security of the platform seriously. This document
explains how to report a vulnerability, which versions receive security fixes,
and how quickly we aim to respond.

It complements [`CONTRIBUTING.md`](./CONTRIBUTING.md#security-fixes), which
describes how a security fix is branched, committed, and released. For the
support window and release cadence, see the
[Support & Release Policy](https://docs.wkspower.com/docs/release-policy).

## Reporting a vulnerability

**Please do not open a public issue or pull request for a security
vulnerability.** This repository is public, and a public report can arm
attackers before users have a chance to upgrade.

Instead, report privately through GitHub's
[**private vulnerability reporting**](https://github.com/wkspower/wks-platform/security/advisories/new):

1. Go to the **Security** tab → **Report a vulnerability**.
2. Describe the issue, the affected component/version, and (if possible) a
   minimal reproduction and impact assessment.

We follow **coordinated disclosure**: we will work with you on a fix and a
disclosure timeline, credit you in the advisory unless you prefer otherwise, and
ask that you give us a reasonable window to release a fix before any public
disclosure. If you have not received an acknowledgement within the window below,
please follow up on the same private report.

> Found a vulnerability in a **third-party dependency** rather than in our own
> code? It is usually already public via the advisory databases (GHSA/CVE). You
> can open a normal issue or PR referencing the advisory — see the dependency
> guidance in [`CONTRIBUTING.md`](./CONTRIBUTING.md#security-fixes).

## Our response targets

| Stage | Target |
|---|---|
| Acknowledge the report | within **3 business days** |
| Initial assessment & severity triage | within **7 business days** |
| Status updates while we work | at least every **7 days** until resolved |

## Remediation SLAs

Once a vulnerability is confirmed and applicable to a supported version, we aim
to ship a fix within the following windows. Severity is assessed with **CVSS**,
and the clock is **escalated** when a vulnerability is listed in
[CISA KEV](https://www.cisa.gov/known-exploited-vulnerabilities-catalog) (known
to be actively exploited) or has a high [EPSS](https://www.first.org/epss/)
exploit-probability score — those move to the Critical track regardless of base
score.

| Severity (CVSS) | Target time to a released fix |
|---|---|
| **Critical** (9.0–10.0) | **72 hours** — may warrant an out-of-band patch release |
| **High** (7.0–8.9) | **14 days** |
| **Medium** (4.0–6.9) | **30 days** |
| **Low** (0.1–3.9) | **90 days** — may ride the next regular release |

A Critical/High fix typically ships as an out-of-band patch on the supported
line (see [out-of-band patches](./CONTRIBUTING.md#out-of-band-patches)); Lows
generally ride the next scheduled release.

### Accepted exceptions

When an advisory cannot be remediated (no patched version exists upstream, or a
fix would break a supported integration), we **dismiss the corresponding
Dependabot alert with a documented reason** rather than leaving it silently open,
and track it as a known exception. A current example is the transitive
`quill@1.3.7` advisory pulled via `formiojs`, which has no patched release.

## Supported versions

Security fixes are applied to the **current stable minor** and the **previous
minor** line. Older minors are end-of-life and should be upgraded.

| Version | Status | Security fixes |
|---|---|---|
| `1.5.x` | Current (Stabilization release — Spring Boot 4 / Java 21 / Camunda 7.24) | ✅ |
| `1.4.x` | Previous stable (Java 17 / Spring Boot 3) | ✅ until `1.5.0` GA + a 90-day grace period |
| `< 1.4` | End-of-life | ❌ — please upgrade |

See the [Support & Release Policy](https://docs.wkspower.com/docs/release-policy)
for how long each line is supported and when releases ship.

## How we disclose

After a fix is released, we publish a
[GitHub Security Advisory](https://github.com/wkspower/wks-platform/security/advisories)
and reference it in the release notes for the tag that contains the fix, so users
can correlate the advisory with the version that resolves it.
