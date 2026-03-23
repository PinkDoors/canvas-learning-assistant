# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-23 AS builder

WORKDIR /build

# Pre-download dependencies
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# Copy source code
COPY src ./src

# Build the jar
RUN mvn clean package -DskipTests


# ---------- Runtime stage ----------
FROM eclipse-temurin:23-jdk

WORKDIR /app

# Copy the jar from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]