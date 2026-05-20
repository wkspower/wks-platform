FROM amazoncorretto:21
VOLUME /tmp
COPY target/*.jar app.jar
COPY application.yml /application.yml
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app.jar"]