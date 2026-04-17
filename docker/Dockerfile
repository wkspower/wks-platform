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
# Uses `mvn` from the base image (the project's `./mvnw` stays in the repo
# for local dev only — inside a fresh container the wrapper re-downloads
# Maven 3.9 for no gain).
#
# BuildKit cache mount on /root/.m2 persists the Maven local repo across
# builds — first build downloads ~200 MB of Spring Boot + CIB seven jars,
# subsequent builds skip the download entirely.
#
# Single `mvn package` step: dependency:go-offline was dropped because the
# cache mount already handles deps-persistence across source edits, and
# go-offline over-resolves (test + provided scopes) which slows first build.
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app

COPY backend/pom.xml ./pom.xml
COPY backend/src ./src
# Embed SPA dist inside the JAR's static resources.
COPY --from=frontend-build /app/dist ./src/main/resources/static

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests package


# ---------------------------------------------------------------------------
# Stage 3: minimal runtime
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime
ENV TZ=UTC \
    SPRING_PROFILES_ACTIVE=dev \
    JAVA_OPTS=""

WORKDIR /app
COPY --from=backend-build /app/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
