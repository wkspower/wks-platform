# Camunda 8 (experimental — not supported)

> ⚠️ **This is not part of the supported local environment.**
> The Camunda 8 integration here targets an **outdated** platform version
> (`8.5.0-alpha1`) and is **pending a planned modernization**. It is kept for
> reference only and is provided **as-is**.

## Status / known limitations

- **Pinned to a pre-release**: `CAMUNDA8_PLATFORM_VERSION=8.5.0-alpha1` (see the root `.env`).
- **Cannot build from source**: the `c8-external-tasks` service has no source directory
  under `apps/java/services/` and is not a Maven module. It can only run from a previously
  published image (`ghcr.io/wkspower/c8-external-tasks`).
- **Not covered by CI**: the release workflow no longer builds the C8 service.

The supported local engine is **Camunda 7**, wired into the root `docker-compose.yml`.

## Running it anyway (advanced)

These files are *overlays* on the root stack. Stack them with `-f` and disable the
default Camunda 7 services you don't want (they share ports such as 8081):

```bash
docker compose \
  -f docker-compose.yml \
  -f experimental/camunda8/docker-compose.camunda8.yaml \
  up -d
```

Seeding and the C8 event hub have matching overlays in this folder
(`docker-compose.demo-data-loader.camunda8.yaml`, `docker-compose.event-hub.camunda8.yaml`).

## Planned upgrade

Modernizing to a current Camunda 8 release (and restoring a buildable
`c8-external-tasks`) is tracked as a future effort. Until then, prefer Camunda 7.
