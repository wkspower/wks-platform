# WKS Platform

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

WKS Platform is an open-source **Case Management & Process Automation** solution built on
Spring Boot, React and Camunda.

📖 **[Documentation](https://docs.wkspower.com/docs/Introduction/)** &nbsp;·&nbsp;
[Contact](https://share-eu1.hsforms.com/1tpt0kdYDS5CbimQTH7xmVA2dcag3) &nbsp;·&nbsp;
[Subscribe for updates](https://share-eu1.hsforms.com/1gpWZRXQwSoWQNgCeuztetQ2dcag3)

> The Docker Compose stack in this repo is for **local development only** — it is not
> production-hardened.

## Quick start (full stack)

Brings up the full stack — backend, case portal, and a demo seed — with images pulled from the
public GitHub Container Registry (no Maven/Yarn build required):

```bash
git clone https://github.com/wkspower/wks-platform.git
cd wks-platform
cp .env-sample .env
docker compose up -d
```

On first run the `demo-data-loader` bootstraps a Keycloak realm and a `demo` user and seeds
sample cases. Wait for it to finish, then open [http://localhost:3001](http://localhost:3001)
and log in with `demo` / `demo`.

## Minimal stack (no auth, no authorization)

Runs just the case engine + portal — **no Mongo, Keycloak, OPA, Camunda or MinIO**. It uses an
embedded H2 datastore and an in-process dev-token issuer, so the portal logs you in
automatically (no credentials), authorization is off, and there's no workflow engine. Built from
source because the published images predate these toggles:

```bash
COMPOSE_PROFILES=app,portal \
WKS_SPRING_PROFILES=db-h2 WKS_AUTH_MODE=dev-token WKS_AUTHZ_OPA_ENABLED=false \
WKS_BPM_ENGINE=none WKS_TENANCY_MULTI_TENANT=false WKS_SEED_ENABLED=true \
REACT_APP_AUTH_MODE=dev-token REACT_APP_AUTH_ISSUER_URL=http://localhost:8081/dev-auth \
docker compose up -d --build
```

Then open [http://localhost:3001](http://localhost:3001) — you're logged in automatically.

---

For every other configuration — per-concern toggles, filesystem storage, notifications, Traefik,
building from source, Camunda 8 — see the
**[Installation docs](https://docs.wkspower.com/docs/Installation/)**.

## License

WKS Platform is released under the [MIT License](LICENSE).

## Contact

Questions, feedback or contributions: hello@wkspower.com
