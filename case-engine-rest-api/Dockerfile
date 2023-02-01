FROM amazoncorretto:17.0.5
VOLUME /tmp
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-Dspring.profiles.active=prod", "-jar","/app.jar"]