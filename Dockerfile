
# ==========================================
# Stage 1: Build Spring Boot Application
# ==========================================

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven wrapper and project configuration
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies and build application
COPY src/ src/

RUN ./mvnw -B clean package -DskipTests


# ==========================================
# Stage 2: Run Spring Boot Application
# ==========================================

FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy executable JAR from build stage
COPY --from=build /app/target/URL_Shortner-0.0.1-SNAPSHOT.jar app.jar

# Run application as non-root user
USER 10001:10001

# Application port
EXPOSE 8080

# Start Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
