# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render default port is injected as $PORT, Spring Boot reads it via server.port=${PORT:8080}
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dserver.port=${PORT}", "app.jar"]
