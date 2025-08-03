#FROM maven:3.9.8-eclipse-temurin-21 AS builder
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src
#RUN mvn clean package -DskipTests

FROM openjdk:21
LABEL authors="niki7"
WORKDIR /app
COPY target/WBC3-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
# запуск
ENTRYPOINT ["java","-jar","app.jar"]