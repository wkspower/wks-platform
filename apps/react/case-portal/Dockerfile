# Stage 1: Build the application.
#
# Build context is the repository ROOT (see docker-compose.yml) so the portal's
# internal dependency, @wkspower/case-config-schema (referenced via a relative
# file: path), is present and resolves during `yarn install`. The repo layout is
# preserved inside the image so `file:../../../packages/case-config-schema` from
# apps/react/case-portal points at /app/packages/case-config-schema.
FROM node:20 AS build

WORKDIR /app

# The internal schema package the portal depends on (its node_modules/dist are
# excluded by .dockerignore and regenerated as needed).
COPY packages/case-config-schema ./packages/case-config-schema

WORKDIR /app/apps/react/case-portal

# Manifests first for cached installs.
COPY apps/react/case-portal/package.json apps/react/case-portal/yarn.lock ./
COPY apps/react/case-portal/deployments/startup.sh ./startup.sh
COPY apps/react/case-portal/deployments/nginx.conf ./nginx.conf
RUN yarn install --frozen-lockfile

# Rest of the portal source, then build.
COPY apps/react/case-portal ./
RUN yarn build

# Stage 2: Create the production image
FROM nginxinc/nginx-unprivileged:1.27-alpine

# uid 101 (nginx) + gid 0, group-writable: startup.sh rewrites index.html at
# boot, and OpenShift restricted-v2 runs the container as an arbitrary UID
# whose only guaranteed membership is gid 0.
COPY --from=build --chown=101:0 /app/apps/react/case-portal/dist /usr/share/nginx/html
USER root
RUN chmod -R g=u /usr/share/nginx/html
USER 101
COPY --from=build /app/apps/react/case-portal/startup.sh /startup.sh
COPY --from=build /app/apps/react/case-portal/nginx.conf /etc/nginx/nginx.conf

EXPOSE 8080

# Start nginx
CMD ["/bin/sh", "/startup.sh"]
