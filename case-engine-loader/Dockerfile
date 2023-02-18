FROM amazoncorretto:17.0.5

VOLUME /tmp

COPY target/*.jar app.jar
COPY application.yml /application.yml
COPY scripts/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8081

ENTRYPOINT /entrypoint.sh