# Running BootForum

## Overview
BootForum can be run as a standalone Java application or as an embedded web application inside a Servlet container such as Apache Tomcat. 
First, download the [WAR file](https://github.com/chipolaris/BootForum/releases/download/v.0.01/BootForum-0.0.1-SNAPSHOT.war "Latest Release") or [build the **BootForum.war**](Building.md). Also make sure JDK 8 or later is installed

## Run as standalone Java application
BootForum can be run as a standalone Java application by using the **java -jar** on command line:
> java -jar BootForum.war
#### Note:
Run-time configuration properties are specified in the **application.properties** file. The template can be found at [application.properties file](./src/main/resources/application.properties)
### Specify configuration file at runtime
By default, BootForum looks for the **application.properties** file in the following locations: 
1. A **/config** subdir of the current directory.
2. The current directory
### To specify a custom configuration file name, use the **--spring.config.name** option:
> java -jar BootForum.war --spring.config.name=myconfig.properties
* Additional information on running with an external configuration file can be found here:
[Externalized Configuration](https://docs.spring.io/spring-boot/docs/2.2.0.RELEASE/reference/html/spring-boot-features.html#boot-features-external-config-application-property-files "Externalized Configuration")

## Run as a web application inside a Servlet Container
**BootForum** can also be run as a regular web application by placing the **BootForum.war** inside the **\<Servet-Container>/webapps** folder. **Apache Tomcat 9** or later is recommended. When run in a Servlet Container or Java EE Application Server, BootForum can be configured to use server's JNDI Datasource definition or JDBC connection values in **application.properties** file

## Run in Docker
**BootForum** is also provided as a Docker-Hub's image in: **ch3nguyen/bootforum**  
Use the included **[docker-compose.yml](./docker-compose.yml)** to run in your local Docker container. It bundles a Postgres instance as a back end DB for your convenience  


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
	    ports:
	      - 8080:8080
	    volumes:
	      - ./bootforum/files:/tmp/BootForum/files
	  db:
	    container_name: postgres
	    image: 'postgres:13.1-alpine'
	    volumes:
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
* After login as an administrator, access the "Administration" area and create Forum or Forum Group. After which, discussions can be started by any authenticated user.
