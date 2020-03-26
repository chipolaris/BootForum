# Running BootForum

## Overview
BootForum can be run as a standalone Java application or as an embedded web application inside a Servlet container such as Apache Tomcat. 
First, if not already, [build the **BootForum.war**](Building.md). Also make sure JDK 8 or later is installed

## Run as standalone Java application
BootForum can be run as a standalone Java application by using the **java -jar** on command line:
> java -jar BootForum.war
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
BootForum can also be run as a regular web application by placing the **BootForum.war** inside the **<Servet-Container>/webapps** folder
