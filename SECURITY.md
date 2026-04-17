# Security policy

## Authentication model (v2)

- **Credentials.** Stored as Argon2id hashes (Spring Security's
  `Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()`). Plaintext is
  never stored, logged, or returned.
- **Session token.** A short-lived JWT (default TTL: 8 hours, configurable
  via `WKS_JWT_TTL_HOURS`) signed with HS256 using `WKS_JWT_SECRET`.
  Tokens carry only the user id, email, and roles — no other PII.
- **Transport.** The JWT is delivered **only** in an `HttpOnly`,
  `SameSite=Lax`, `Secure` (in production) cookie named `WKS_SESSION`.
  Never in response bodies, never in localStorage, never in URL params.
- **Filter chain.** Stateless — no `JSESSIONID`, no `HttpSession`. CSRF is
  disabled on `/api/**` (the `HttpOnly` + `SameSite=Lax` cookie combination
  already blocks the cross-site abuse surface CSRF tokens defend against).
- **Logout.** Clears the cookie. Because JWTs are stateless, Phase 0 does
  not maintain a server-side revocation list — the 8-hour TTL is the
  mitigation. A DB-backed revocation list is a Phase 1 follow-up.
- **First-boot admin.** See README "First-boot admin credentials" — in
  production both `WKS_ADMIN_EMAIL` and `WKS_ADMIN_PASSWORD` are mandatory
  and the application fails to start (`WKS-API-051`) if either is absent.

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
