FROM --platform=$BUILDPLATFORM eclipse-temurin:19-jre-focal
ARG TARGETPLATFORM
ARG BUILDPLATFORM
VOLUME /tmp
COPY target/*.jar app.jar
COPY application.yml /application.yml
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app.jar"]