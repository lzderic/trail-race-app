FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1) Install the shared-db-migrations module into the container's local repo
COPY shared-db-migrations/pom.xml ./shared-db-migrations/pom.xml
COPY shared-db-migrations/src ./shared-db-migrations/src
RUN mvn -f shared-db-migrations/pom.xml clean install -DskipTests -B

# 2) Resolve command-service dependencies (cached layer)
COPY race_application_command_service/pom.xml ./race_application_command_service/pom.xml
RUN mvn -f race_application_command_service/pom.xml dependency:go-offline -B || true

# 3) Build the command service
COPY race_application_command_service/src ./race_application_command_service/src
RUN mvn -f race_application_command_service/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/race_application_command_service/target/*.jar app.jar
USER appuser
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

