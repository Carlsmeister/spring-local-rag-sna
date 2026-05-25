# --- Stage 1: Build Stage ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies to utilize Docker cache layers
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy src and build the package (excluding integration tests for normal builds)
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Stage 2: Runtime Stage ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Run the app under a non-privileged system user for hardened production security
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

# Copy target jar from build stage
COPY --from=build --chown=spring:spring /app/target/local-ai-*.jar app.jar

# Expose default HTTP servlet port
EXPOSE 8080

# Configure G1GC and exit-on-OOM JVM properties
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]
