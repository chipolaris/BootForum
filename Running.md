# Running BootForum

## Overview
BootForum can be run as a stand-alone Java application as it is bundled with an embedded Apache Tomcat. The only real requirement it has is JDK version 11 or later. If a SQL database (PostgreSQL, MySQL, SQL Server, Oracle, etc) is not available, you can use the H2 database bundled in the BootForum.war. It run on any OS where JDK can be installed: Linux, Windows, Mac OS, etc.

## Quick Start

1. If not already, install JDK 11 or later.
2. Follow the [Quick Start Instruction](QuickStart.md)
3. Log in as administrator and configure options

## Runtime Configuration:
The **application.properties** file provides the run-time configurations. The full template can be found at [application.properties file](./src/main/resources/application.properties)
### Specify configuration file at runtime
By default, BootForum looks for the **application.properties** file in the following locations:
1. A **/config** subdir of the current directory.
2. The current directory
### To specify a custom configuration file name, use the **--spring.config.name** option:
> java -jar BootForum.war --spring.config.name=myconfig.properties
* Additional information on running with an external configuration file can be found here:
[Externalized Configuration](https://docs.spring.io/spring-boot/docs/2.2.0.RELEASE/reference/html/spring-boot-features.html#boot-features-external-config-application-property-files "Externalized Configuration")

## Run in Docker
**BootForum** is also provided as a Docker-Hub's image in: **ch3nguyen/bootforum**. An example of running BootForum docker image connecting to an instance of PostgreSQL is as follows:

docker run -p 8080:8080 -e "spring.datasource.url=jdbc:postgresql://localhost:5432/BootForum"  
 -e "spring.datasource.username=BootForum" -e  "spring.datasource.password=secret"  
 -e "spring.datasource.driverClassName=org.postgresql.Driver" ch3nguyen/bootforum


### Docker Compose
If desired, use the included **[docker-compose.yml](./docker-compose.yml)** to run in your local Docker container. It includes a Postgres instance as the DB for your convenience  

version: '2'

services:
 app:
   image: 'ch3nguyen/bootforum:latest'
   container_name: app
   depends_on:
     db:
       condition: service_healthy
   links:
     - db
   environment:
     - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/BootForum
     - SPRING_DATASOURCE_USERNAME=BootForum
     - SPRING_DATASOURCE_PASSWORD=secret
     - File.uploadDirectory=/var/BootForum/files
     - Lucene.indexDirectory=/var/BootForum/index
   ports:
     - 8080:8080
   volumes:
     # map the host's current-directory/BootForum to app's BootForum/files & BootForum/index
     - ./BootForum/files:/var/BootForum/files
     - ./BootForum/index:/var/BootForum/index
 db:
   container_name: postgres
   image: 'postgres:13.1-alpine'
   volumes:
     # map the host's current-directory/postresql/data to postgresql's data
     - ./postgresql/data:/var/lib/postgresql/data
   environment:
     - POSTGRES_USER=BootForum
     - POSTGRES_PASSWORD=secret
   healthcheck:
     test: ["CMD-SHELL", "pg_isready -U BootForum"]
     interval: 5s
     timeout: 5s
     retries: 5

## Data Initialization
* The first time **BootForum** starts, it creates an administrator account with username **admin** and password **secret**. The password should be changed immediately after login into the app.
* Also created on the first time the application starts on a brand new database instance are:
* Forum **"Announcement"**  
* Discussion **"Welcome to BootForum"**
* Chat Room **"First Chat Room"**
* Tag **"Bulletin"**
* After login as an administrator, access the "Administration" area and create Forum or Forum Group. After which, discussions can be started by any authenticated user.
