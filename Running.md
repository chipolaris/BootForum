# Running BootForum

## Overview
BootForum can be run as a standalone Java application or as an embedded web application inside a Servlet container such as Apache Tomcat. 
First, download the [WAR file](https://github.com/chipolaris/BootForum/releases/download/v0.0.1/BootForum-0.0.1-SNAPSHOT.war "Latest Release") or [build the **BootForum.war**](Building.md). Also make sure JDK 8 or later is installed

## Run as standalone Java application
BootForum can be run as a standalone Java application by using the **java -jar** on command line:
> java -jar BootForum.war
#### Note:
Run-time configuration properties are specified in the **application.properties** file. The template can be found at [application.properties file](./src/main/resources/application.properties)
### Specify configuration file at runtime
By default, BootForum looks for the **application.properties** file in the following locations: 
1. A **/config** subdir of the current directory.
2. The current directory
3. A classpath **/config** package
4. The classpath root
### To specify a custom configuration file name, use the **--spring.config.name** option:
> java -jar BootForum.war --spring.config.name=myconfig.properties
* Additional information on running with an external configuration file can be found here:
[Externalized Configuration](https://docs.spring.io/spring-boot/docs/2.2.0.RELEASE/reference/html/spring-boot-features.html#boot-features-external-config-application-property-files "Externalized Configuration")

## Run as a web application inside a Servlet Container
BootForum can also be run as a regular web application by placing the **BootForum.war** inside the **\<Servet-Container>/webapps** folder. **Apache Tomcat 9** or later is recommended. When run in a Servlet Container or Java EE Application Server, BootForum can be configured to use server's JNDI Datasource definition or JDBC connection values in **application.properties** file
