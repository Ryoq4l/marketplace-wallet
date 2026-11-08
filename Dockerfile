FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY marketplace-common/pom.xml ./marketplace-common/pom.xml
COPY marketplace-common/src ./marketplace-common/src
RUN cd marketplace-common && mvn clean install -DskipTests

COPY marketplace-wallet/pom.xml ./marketplace-wallet/pom.xml
COPY marketplace-wallet/src ./marketplace-wallet/src
RUN cd marketplace-wallet && mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/marketplace-wallet/target/*.jar app.jar
EXPOSE 6767
ENTRYPOINT ["java", "-jar", "app.jar"]