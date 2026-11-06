# Stage 1: build the Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests


# Stage 2: run the application
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd --system appgroup \
    && useradd --system \
        --gid appgroup \
        --no-create-home \
        appuser

COPY --from=build \
     --chown=appuser:appgroup \
     /app/target/*.jar \
     app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]