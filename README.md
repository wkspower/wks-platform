# WKS Platform

Open-source case management for system integrators.

---

## What is WKS?

WKS Platform is an open-source case management platform for system integrators.
Define case types in YAML, drive lifecycles with BPMN workflows, and ship to
your clients — no backend code required.

Evaluate it in 30 minutes with `docker compose up`.

## Quick start

Requires Docker 24+ and ~2 GB of free RAM. No Java, no Node, no additional
configuration.

```bash
git clone https://github.com/wkspower/wks-platform.git
cd wks-platform/docker
docker compose up --build
```

Open `http://localhost:8080/`. The REST health probe is at
`http://localhost:8080/api/health`. Cold boot after the first image build
takes under two minutes on a 16 GB SSD dev machine.

## v1 is archived

v1 code lives on the [`v1` branch](https://github.com/wkspower/wks-platform/tree/v1)
and the [`v1-final` tag](https://github.com/wkspower/wks-platform/releases/tag/v1-final).
It is frozen — no new features, no security updates. v2 is a full rewrite
because v1's monorepo-of-microservices (seven services, shared Docker
network, Keycloak + OPA for auth) made evaluation painful and deployment
brittle. v2 is a single-container monolith with strict hexagonal
boundaries so the evaluation takes 30 minutes instead of 30 hours.

## Architecture

Single-container monolith: Spring Boot backend with an embedded CIB seven
BPMN engine behind a `WorkflowEngine` port, a React SPA frontend, H2 in
development / PostgreSQL in production via JPA + Flyway, Docker-first
deployment. Case types are YAML files; lifecycle rules are BPMN processes;
the UI is rendered from a server-generated JSON schema so no case-specific
frontend code is needed.

See [docs/architecture.md](./docs/architecture.md) for the full decision log (document in progress).

## Links

- **API** — `http://localhost:8080/swagger-ui/index.html` (real endpoints arrive in Story 1.4)
- **Architecture** — [docs/architecture.md](./docs/architecture.md) _(in progress)_
- **Security policy** — [SECURITY.md](./SECURITY.md)
- **Code of conduct** — [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md)

## Project status

v2 is under active development on `v2-develop`. The roadmap is published as
per-epic story specs under `_bmad-output/implementation-artifacts/` in
the planning repo. Pre-1.0 releases will be cut from `main` once the
Phase-0 epics (1–9) are complete.

## License

WKS Platform v2 is licensed under the **Apache License, Version 2.0**
(see [`LICENSE`](./LICENSE) and [`NOTICE`](./NOTICE)).

v1 was licensed under the MIT License, and the [`v1` branch](https://github.com/wkspower/wks-platform/tree/v1)
retains MIT unchanged. v2 relicenses to Apache 2.0 for the explicit
patent grant and to align with the embedded CIB seven engine, which is
also Apache 2.0.
