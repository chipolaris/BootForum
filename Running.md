# Running BootForum

## Overview
BootForum can be run as a stand-alone Java application as it is bundled with an embedded Apache Tomcat. The only real requirement it has is JDK version 11 or later. If a SQL database (PostgreSQL, MySQL, SQL Server, Oracle, etc) is not available, you can use the H2 database bundled in the BootForum.war file.

## Quick Start
1. If not already, install JDK 11 or later.
2. Download the [WAR file](https://github.com/chipolaris/BootForum/releases/download/v.0.03/BootForum-0.0.3-SNAPSHOT.war "Latest Release") or [build the **BootForum.war**](Building.md).
3. Create an **application.properties** file to configure the desired DB connection using the following template 
	
		################################################################################
		## JDBC connection templates, use the appropriate one below to fit your 
		## environment
		## Note that spring.datasource.driverClassName can be omitted as 
		## it can be derived from url value
		################################################################################
		
		#### H2 database configuration
		#spring.datasource.url=jdbc:h2:file:~/BootForum/database/db
		#spring.datasource.username=sa
		#spring.datasource.password=
		#spring.datasource.driverClassName=org.h2.Driver
		
		#### PostgreSQL database configuration
		spring.datasource.url=jdbc:postgresql://localhost:5433/BootForum
		spring.datasource.username=BootForum
		spring.datasource.password=secret
		#spring.datasource.driverClassName=org.postgresql.Driver
		
		#### MySQL database configuration
		#spring.datasource.url=jdbc:mysql://localhost/BootForum
		#spring.datasource.username=BootForum
		#spring.datasource.password=secret
		#spring.datasource.driverClassName=com.mysql.jdbc.Driver
		
		### SQL Server database configuration
		#spring.datasource.url=jdbc:sqlserver://localhost;databaseName=BootForum
		#spring.datasource.username=BootForum
		#spring.datasource.password=secret
		#spring.datasource.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver
		
4. In the directory where **BootForum.war** and **application.properties** reside, execute **java -jar** on command line:

		java -jar BootForum.war

#### Note:
The **application.properties** file specifies the run-time configurations. The full template can be found at [application.properties file](./src/main/resources/application.properties)
### Specify configuration file at runtime
By default, BootForum looks for the **application.properties** file in the following locations: 
1. A **/config** subdir of the current directory.
2. The current directory
### To specify a custom configuration file name, use the **--spring.config.name** option:
> java -jar BootForum.war --spring.config.name=myconfig.properties
* Additional information on running with an external configuration file can be found here:
[Externalized Configuration](https://docs.spring.io/spring-boot/docs/2.2.0.RELEASE/reference/html/spring-boot-features.html#boot-features-external-config-application-property-files "Externalized Configuration")

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
* Also created on the first time the application starts on a brand new database instance are: 
	* Forum **"Announcement"**  
	* Discussion **"Welcome to BootForum"**
	* Chat Room **"First Chat Room"**
* After login as an administrator, access the "Administration" area and create Forum or Forum Group. After which, discussions can be started by any authenticated user.

