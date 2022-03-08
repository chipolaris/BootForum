# syntax=docker/dockerfile:1

FROM openjdk:11.0.13-jre-buster
ARG JAR_FILE=target/BootForum-*.war
COPY ${JAR_FILE} BootForum.war
ENTRYPOINT ["java","-jar","/BootForum.war"]