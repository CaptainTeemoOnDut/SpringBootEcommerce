# =========================
# Stage 1: Build
# =========================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Maven Wrapper
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven Wrapper executable
RUN chmod +x mvnw

# Download dependencies first
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build Spring Boot application
RUN ./mvnw clean package -DskipTests


# =========================
# Stage 2: Run
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the generated JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Spring Boot default port
EXPOSE 8443

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]