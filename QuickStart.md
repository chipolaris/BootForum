
# Quick Start

## Required Software 
- JDK 11+
- An (optional) relational database, one of the followings
    - PostgreSQL
    - MySQL
    - SQL Server 
    - Oracle
    - Others..

**Note**: if no database is available, the embedded H2 database is included in BootForum  

## Download
Latest [war file](https://github.com/chipolaris/BootForum/releases/download/v.0.05/BootForum-0.0.5-SNAPSHOT.war). Rename to **BootForum.war** if desired<br>

## Running BootForum
1. Create an **application.properties** file for database connection configurations. Use one of the following templates for your respective database type
 
       #### H2 database configuration
       spring.datasource.url=jdbc:h2:file:~/BootForum/database/db
       spring.datasource.username=sa
       spring.datasource.password=
       spring.datasource.driverClassName=org.h2.Driver

       #### PostgreSQL database configuration
       spring.datasource.url=jdbc:postgresql://localhost:5433/BootForum
       spring.datasource.username=BootForum
       spring.datasource.password=secret
       spring.datasource.driverClassName=org.postgresql.Driver

       #### MySQL database configuration
       spring.datasource.url=jdbc:mysql://localhost/BootForum
       spring.datasource.username=BootForum
       spring.datasource.password=secret
       spring.datasource.driverClassName=com.mysql.jdbc.Driver

       ### SQL Server database configuration
       spring.datasource.url=jdbc:sqlserver://localhost;databaseName=BootForum
       spring.datasource.username=BootForum
       spring.datasource.password=secret
       spring.datasource.driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver
 
3. In the directory where BootForum.war and application.properties reside, execute the followings on command line: <br>
      > **java -jar BootForum.war** 
4. BootForum is available at http://localhost:8080/BootForum/
5. Log in as administrator to change the admin password, initial account username/password is **admin/secret**

* By default, uploaded data (avatars, attachements, thumbnails...) is stored in BootForum folder in user home directory, e.g., **<user.home>/BootForum**. To customize this and other parameters, see this **document**
