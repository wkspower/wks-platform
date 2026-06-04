# Multi-stage build. Build context is the apps/java Maven reactor root
# (this service depends on sibling library modules), NOT this service dir.
#
#   docker build -f apps/java/services/case-engine-rest-api/Dockerfile apps/java
#
# Stage 1: compile the service + the modules it needs (-am = also-make).
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /build
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q clean package -pl services/case-engine-rest-api -am -DskipTests

# Stage 2: runtime image (matches the jar's Java 21 target).
FROM amazoncorretto:21
VOLUME /tmp
COPY --from=build /build/services/case-engine-rest-api/target/*.jar app.jar
COPY --from=build /build/services/case-engine-rest-api/application.yml /application.yml
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app.jar"]
