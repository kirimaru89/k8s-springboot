# First stage: Build the JAR file
FROM --platform=linux/arm64 maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy only the POM file first
COPY pom.xml .

# Use cache mount for dependencies
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline

# Now copy source code
COPY src ./src

# Build the application
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests && \
    ls -la target/ && \
    find /app -name "*.jar"

# Second stage: Run the Spring Boot app
FROM --platform=linux/arm64 eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080