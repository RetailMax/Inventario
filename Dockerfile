FROM maven:3.9.6-eclipse-temurin-21 as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim

WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY wallet /app/wallet

ENV TNS_ADMIN=/app/wallet

EXPOSE 8080


ENTRYPOINT ["java", "-jar", "app.jar"]