FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
# Resolve dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B
# Copy source
COPY src src
# Build the application
RUN mvn clean package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]