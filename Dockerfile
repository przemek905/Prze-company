FROM openjdk:15-alpine

COPY ./target/prze-company-0.0.1-SNAPSHOT.jar ./app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "/app.jar"]

EXPOSE 8080
