FROM --platform=$BUILDPLATFORM amazoncorretto:17.0.5
ARG TARGETPLATFORM
ARG BUILDPLATFORM
VOLUME /tmp
COPY target/*.jar app.jar
COPY application.yml /application.yml
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app.jar"]