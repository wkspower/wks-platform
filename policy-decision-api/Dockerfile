FROM bitnami/minideb as build

ARG TARGETPLATFORM
ARG TARGETOS
ARG TARGETARCH

RUN apt -y update \
    && apt install -y curl \
    && curl -q -L https://github.com/open-policy-agent/opa/releases/download/v0.50.2/opa_${TARGETOS}_${TARGETARCH}_static -o opa \
    && chmod +x opa \
    && mv opa /usr/bin

COPY policy-decision-api/wks_policy_rules.rego /app
RUN opa build /app --optimize=1 --entrypoint "wks/authz"
RUN ls -la /app
RUN ls -la

FROM nginx:1.20.0-alpine
# RUN mkdir /usr/share/nginx/html/bundles,
# COPY --from=build /bundle.tar.gz /usr/share/nginx/html/bundles
CMD ["nginx"]