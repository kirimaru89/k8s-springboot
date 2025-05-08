FROM openjdk:21-jdk-slim

ARG JAR_FILE=target/step1service-0.0.1-SNAPSHOT.jar

WORKDIR /app

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

EXPOSE 8081 