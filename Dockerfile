# syntax=docker/dockerfile:1

FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/BootForum-0.0.1-SNAPSHOT.war
COPY ${JAR_FILE} BootForum.war
ENTRYPOINT ["java","-jar","/BootForum.war"]