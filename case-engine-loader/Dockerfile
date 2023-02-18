FROM amazoncorretto:17.0.5

ENV IMPORT_DIR_PATH /opt/data

VOLUME /tmp

COPY target/*.jar app.jar
COPY application.yml /application.yml
COPY scripts/entrypoint.sh /entrypoint.sh
COPY ./data /opt/data

RUN chmod +x /entrypoint.sh

EXPOSE 7100

ENTRYPOINT /entrypoint.sh