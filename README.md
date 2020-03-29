# BootForum Project

## Overview
Full features web based forum/disscussion/bulletin board software implemented using Java, Spring Boot, Eclipselink, Primefaces, and W3.CSS.

* Cross-platform supported. All platforms that run Java: Unix/Linux, Windows, or Mac OS.
* Support all relational databases that provide JDBC connectivity. Tested on Postgresql, H2, MySQL, SQL Server, and Oracle.
* Responsive web designed using W3.CSS framework to display and function well on all screen sizes (desktop, tablet, or mobile phone)
* Very _easy_ to run: operate out of the box with minimal configuration. Run-time configuration flexibilty enabled by Spring Boot.

### Main Features:

* **Discussion/Comment**:
  * Add discussion
  * Add new/reply/quote comments
  * Add vote (per comment)
  * Comment thumbnails and attachments (configurable by administrators)
  * Replies can be viewed as a comment thread.
  * Discussions can be tagged

* **Search**
  * Search for comments (backed by Lucence Index)
  * Search members (by username)

* **Members**
  * View user profile
  * User reputation

* **Forums**:
  * Unlimited nested forum groups / sub-groups (category/subcategory)

* **Administration**:
  * Manage forums: add, edit, delete forum group / sub forum groups (unlimited nested structure)
  * Manage discussions: add tags, moderate (mark active/inactive)
  * Manage tags: create / edit, apply to discussions
  * Manage users: edit (change role, password) / delete / mark active/inactive, etc

* **UI**: 
  * Change look and feel based on each user'r preferences
  
### Prerequisites to Run
**Java 8** installed
A relational database. This software has been tested in **Postgresql**, **H2**, **MySQL**, **SQL Server**, **Oracle**.

### Recommended Environment for Development
**JDK 8**or later
**Maven 3** or later
IDE: recommend tools with Spring deveopment plugin such as Eclipse based **Spring Tool Suite**
