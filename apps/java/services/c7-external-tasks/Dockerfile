# Multi-stage build. Build context is the apps/java Maven reactor root
# (this service depends on sibling library modules), NOT this service dir.
#
#   docker build -f apps/java/services/c7-external-tasks/Dockerfile apps/java
#
# Stage 1: compile the service + the modules it needs (-am = also-make).
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /build
COPY . .
# Optional release stamp: CI passes --build-arg REVISION=X.Y.Z so the jar version
# matches the image tag. Unset (dev/local builds) = the pom's ${revision} default.
ARG REVISION=
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q clean package -pl services/c7-external-tasks -am -DskipTests ${REVISION:+-Drevision=$REVISION}

# Stage 2: runtime image (matches the jar's Java 21 target).
FROM amazoncorretto:21
VOLUME /tmp
COPY --from=build /build/services/c7-external-tasks/target/*.jar app.jar
COPY --from=build /build/services/c7-external-tasks/application.yml /application.yml
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app.jar"]
