# syntax=docker/dockerfile:1

FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/BootForum-*.war
COPY ${JAR_FILE} BootForum.war
ENTRYPOINT ["java","-jar","/BootForum.war"]