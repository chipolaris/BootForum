# BootForum [Java Bulletin Board]

## Overview
Full features web based forum/discussion/bulletin board software implemented using Java, Spring Boot, EclipseLink, PrimeFaces, and W3.CSS.

* Cross-platform supported. All platforms that run Java: Unix/Linux, Windows, or Mac OS.
* Pre-bundled with embedded H2 database but will work with all relational databases that provide JDBC connectivity. Tested on PostgreSQL, H2, MySQL, SQL Server, and Oracle.
* Pre-bundled with Tomcat 9. No need to install a separate Web container.
* Responsive design using W3.CSS framework to display and function well on all screen sizes (desktop, tablet, or mobile phone)
* _**Very easy**_ to operate: run out of the box with minimal configuration. Run-time configuration flexibility enabled by Spring Boot.

###  [**View BootForum Screenshots**](Screenshots.md "Screenshots")

### Main Features:

* **Discussion/Comment**:
  * Add discussion
  * Add / reply / quote comment
  * Edit own comment
  * Add vote up/down (per comment)
  * Comment thumbnails and attachments (file size and count are configurable by administrators)
  * Replies can be viewed as a comment thread.
  * Tagged discussions: discussions can be tagged by administrator

* **Live Chat**
  * Chat live with other connected members
  * Attach images
  * HTML Emoji supported
  * Chat channels are managed (add / edit / delete) by system administrator

* **Search**
  * Search comments/discussions by keywords (backed by Lucence Index)
  * Search members (by username)

* **Members**
  * View user profile
  * User reputation

* **Forums**:
  * Flexible forum structures. Unlimited nested forum groups / sub-groups (categories / sub-categories)

* **User Profile**:
  * User managed profile: account info, avatar ...

* **Private Messages**:
  * Send/Receive messages to other forum members 

* **Administration**:
  * Administrator Dash Board
  * Manage forums: add, edit, delete forum group / sub forum groups (unlimited nested structure)
  * Manage discussions: add tags, moderate (mark active/inactive)
  * Manage tags: create / edit, apply to discussions
  * Manage users: edit (change role, password) / delete / mark active/inactive, etc
  * Customizable home page display

* **UI Theme**: 
  * Change look and feel based on each user's preferences
  
### Prerequisites
**Java 11**
Recommended a relational database. If a stand-alone database is not available, the bundled H2 database can be used. **BootForum** has been tested with **PostgreSQL**, **H2**, **MySQL**, **SQL Server**, **Oracle**.

### Try BootForum
Head to [**Running Instructions**](Running.md "Running Instructions") to download and try BootForum in your environment. Note that a separate relational database is recommended but is not required as **BootForum** is bundled with an embedded H2 database.

### Recommended Environment for Development
For developers, the followings are recommended
* **JDK 11** or later
* **Maven 3** or later
* IDE: recommend tools with Spring development plugin such as Eclipse based **Spring Tool Suite**

### Note

#### 11/06/2021:
* Runtime Docker image is provided in Docker-Hub's with image id **ch3nguyen/bootforum**
 
#### 12/12/2021:
Project is updated from JDK version 8 to 11.
* Another option is to use the Dockerized distribution (which include PostgreSQL database)
