FROM maven:3.9.6-eclipse-temurin-21 as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim

WORKDIR /app
RUN 
COPY --from=build /app/target/*.jar app.jar

ENV TNS_ADMIN=/app/wallet

EXPOSE 8080


ENTRYPOINT ["java", "-jar", "app.jar"]
