FROM amazoncorretto:17.0.5
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=prod", "-jar","/app.jar"]