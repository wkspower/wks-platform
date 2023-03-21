FROM amazoncorretto:17.0.5

ENV IMPORT_DIR_PATH /opt/data

COPY target/*.jar app.jar
COPY application.yml /application.yml
COPY ./data /opt/data

ENTRYPOINT ["java","-jar","/app.jar"]