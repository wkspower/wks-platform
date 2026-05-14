# syntax=docker/dockerfile:1.7
#
# WKS Platform — single-image build (frontend + backend → one JAR → one runtime).
# Named stages; never reference by index.

# ---------------------------------------------------------------------------
# Stage 1: build the React SPA
# ---------------------------------------------------------------------------
FROM node:22-alpine AS frontend-build
WORKDIR /app

# Layer cache: lockfile first so deps don't re-install on source edits.
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY frontend/ ./
RUN npm run build


# ---------------------------------------------------------------------------
# Stage 2: build the Spring Boot JAR (embeds the SPA static assets)
#
# Uses ./mvnw (Maven 3.9.9 pinned in .mvn/wrapper/maven-wrapper.properties).
# The wrapper dist is cached in /root/.m2 via BuildKit cache mount so the
# network download is skipped on all but the very first cold build.
#
# Separate dependency:go-offline layer: cached until pom.xml changes, so
# source-only edits skip dep resolution entirely.
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app

COPY backend/mvnw ./mvnw
COPY backend/.mvn ./.mvn
RUN chmod +x ./mvnw

COPY backend/pom.xml ./pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp dependency:go-offline

COPY backend/src ./src
# Embed SPA dist inside the JAR's static resources.
COPY --from=frontend-build /app/dist ./src/main/resources/static

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp -DskipTests package


# ---------------------------------------------------------------------------
# Stage 3: minimal runtime
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime
# wget: required by docker-compose healthcheck (not present in jre-alpine base).
RUN apk add --no-cache wget

ENV TZ=UTC \
    JAVA_OPTS=""

WORKDIR /app
COPY --from=backend-build /app/target/*.jar /app/app.jar
COPY case-types /app/case-types

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
