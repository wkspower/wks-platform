# Security policy

## Supported versions

| Version | Supported? | Notes |
| --- | --- | --- |
| 2.x (main) | ✅ | Active development; security fixes land here |
| 1.x ([`v1` branch](https://github.com/wkspower/wks-platform/tree/v1)) | ❌ | Frozen — no further features, **no security updates**. Migrate to v2. |

## Reporting a vulnerability

Please **do not** open a public GitHub issue for security reports.

Email **security@wkspower.com** with:

- a description of the vulnerability
- a minimal reproduction (proof-of-concept, HTTP request, or code snippet)
- the commit SHA / version you tested against
- your name and preferred disclosure credit (optional)

PGP encryption is optional; key available on request.

### Response time

- **Acknowledgement:** within **72 hours** of receipt.
- **Triage and severity assessment:** within 7 days.
- **Fix timeline:** communicated during triage. Critical vulnerabilities
  are prioritised over feature work.

We aim to publish a coordinated advisory once a fix is released. Credit
is given in the advisory unless you request anonymity.

## Scope

### In scope

- Authentication or authorisation bypass
- Remote code execution
- SQL injection
- Cross-site scripting (XSS) — reflected, stored, DOM
- Insecure direct object reference (IDOR)
- Cross-site request forgery (CSRF)
- Privilege escalation between WKS roles
- Sensitive data exposure in API responses, logs, or exports
- Vulnerable dependencies with a known exploit path into WKS

### Out of scope

- Self-hosted misconfigurations (weak passwords, open firewalls, etc.)
- Denial-of-service attacks requiring privileged network access
- Issues affecting only outdated browsers without mitigation
- Social engineering
- Missing security headers with no direct impact
- Rate-limiting on public marketing endpoints
- Reports from automated scanners without a demonstrated exploit

## Safe harbour

Good-faith security research conducted under this policy is welcome.
We will not pursue legal action or suspend access for researchers who:

- stay within the scope above
- avoid privacy violations, service disruption, or data destruction
- give us reasonable time to fix issues before public disclosure
- act in the spirit of coordinated disclosure
