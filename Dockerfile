FROM maven:3.9-eclipse-temurin-21 AS build
ARG GITHUB_TOKEN
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 6767
ENTRYPOINT ["java", "-jar", "app.jar"]