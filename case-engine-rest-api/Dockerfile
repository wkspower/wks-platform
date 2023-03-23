FROM maven:3.9.0-amazoncorretto-17 AS build

RUN mkdir /platform
ADD pom.xml /platform/pom.xml
ADD case-engine-rest-api /platform/case-engine-rest-api
ADD case-engine /platform/case-engine
ADD case-engine-loader /platform/case-engine-loader
ADD api-security /platform/api-security
ADD bpm-engine-c7-external-service /platform/bpm-engine-c7-external-service
ADD bpm-engine-c7-client /platform/bpm-engine-c7-client
ADD bpm-engine-c8-client /platform/bpm-engine-c8-client
ADD bpm-engine-client /platform/bpm-engine-client
ADD bpm-engine-interface /platform/bpm-engine-interface
ADD email-to-case /platform/email-to-case
ADD rest-client /platform/rest-client
ADD configs/certs /platform/certs
RUN mvn -f /platform/pom.xml -Dmaven.test.skip=true package

FROM amazoncorretto:17.0.5

COPY --from=build /platform/case-engine-rest-api/target/*.jar app.jar
COPY --from=build /platform/case-engine-rest-api/application.yml application.yml

EXPOSE 8084

ENTRYPOINT java -Djavax.net.ssl.trustStore=/keystore.p12 -Djavax.net.ssl.trustStorePassword=$TRUST_STORE_PASSWORD -jar /app.jar
