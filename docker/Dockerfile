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
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app

# Layer cache: resolve deps before copying src.
COPY backend/pom.xml ./pom.xml
COPY backend/.mvn ./.mvn
COPY backend/mvnw ./mvnw
RUN chmod +x ./mvnw && ./mvnw -B -q dependency:go-offline

COPY backend/src ./src
# Embed SPA dist inside the JAR's static resources.
COPY --from=frontend-build /app/dist ./src/main/resources/static

RUN ./mvnw -B -q -DskipTests package


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
