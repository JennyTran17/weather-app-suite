# weather-app-suite/weather-producer/Dockerfile

# --- Stage 1: Build the Spring Boot application (named 'build') ---
# Using Eclipse Temurin JDK and installing Maven manually
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Install Maven on top of the JDK image
# This requires updating package lists and then installing the 'maven' package
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY src ./src
RUN mvn clean install -DskipTests

# --- Stage 2: Create the final, smaller runtime image ---
# This stage remains the same, as eclipse-temurin:21-jre-jammy seems reachable
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copy the built JAR from the 'build' stage
COPY --from=build /app/target/weather-producer-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]