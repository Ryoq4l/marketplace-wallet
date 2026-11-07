FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

ARG GITHUB_TOKEN
ENV GITHUB_TOKEN=$GITHUB_TOKEN
COPY settings.xml /root/.m2/settings.xml

COPY pom.xml .
RUN GITHUB_TOKEN=$GITHUB_TOKEN

RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 6767
ENTRYPOINT ["java", "-jar", "app.jar"]