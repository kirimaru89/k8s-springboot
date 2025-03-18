# FROM openjdk:21-jdk-slim
# WORKDIR /app
# COPY target/k8s-demo-1.0-SNAPSHOT.jar app.jar
# ENTRYPOINT ["java", "-jar", "app.jar"]
# EXPOSE 8080


# First stage: Build the JAR file
FROM --platform=linux/arm64 maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy only the POM file first
COPY pom.xml .

# Download dependencies only (this will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline

# Now copy source code
COPY src ./src

# Build the application (dependencies are already downloaded)
RUN mvn package -DskipTests && \
    ls -la target/ && \
    find /app -name "*.jar"

# Second stage: Run the Spring Boot app
FROM --platform=linux/arm64 eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080