# BootForum Project

## Overview
Full features web based forum/disscussion/bulletin board software implemented using Java, Spring Boot, Eclipselink, Primefaces, and W3.CSS.

* Cross-platform supported. All platforms that run Java: Unix/Linux, Windows, or Mac OS.
* Support all relational databases that provide JDBC connectivity. Tested on Postgresql, H2, MySQL, SQL Server, and Oracle.
* Responsive web designed using W3.CSS framework to display and function well on all screen sizes (desktop, tablet, or mobile phone)
* Very _easy_ to run: operate out of the box with minimal configuration. Run-time configuration flexibilty enabled by Spring Boot.

### View [**BootForum Screenshots**](Screenshots.md "Screenshots")

### Main Features:

* **Discussion/Comment**:
  * Add discussion
  * Add new/reply/quote comment
  * Edit comment
  * Add vote up/down (per comment)
  * Comment thumbnails and attachments (configurable by administrators)
  * Replies can be viewed as a comment thread.
  * Tagged discussions (by administrator)

* **Live Chat**
  * Chat live with other connected members
  * Attach images
  * HTML Emoji supported
  * Chat channels are managed (add/edit/delete) by system admin

* **Search**
  * Search for comments (backed by Lucence Index)
  * Search members (by username)

* **Members**
  * View user profile
  * User reputation

* **Forums**:
  * Unlimited nested forum groups / sub-groups (categories/subcategories)

* **User Profile**:
  * User managed profile: account info, avatar ...

* **Private Messages**:
  * Send/Receive messages to other forum members 

* **Administration**:
  * Manage forums: add, edit, delete forum group / sub forum groups (unlimited nested structure)
  * Manage discussions: add tags, moderate (mark active/inactive)
  * Manage tags: create / edit, apply to discussions
  * Manage users: edit (change role, password) / delete / mark active/inactive, etc
  * Customizable home page display

* **UI**: 
  * Change look and feel based on each user's preferences
  
### Prerequisites to Run
**Java 8**
A relational database. **BootForum** has been tested with **Postgresql**, **H2**, **MySQL**, **SQL Server**, **Oracle**.

### Try BootForum
Head to [**Running Instructions**](Running.md "Running Instructions") to download and tryout BootForum in your environment. Note that a separate relational database is not required as **BootForum** is bundled with an embbeded H2 database.

### Recommended Environment for Development
For developers, the followings are recommended
* **JDK 8** or later
* **Maven 3** or later
* IDE: recommend tools with Spring deveopment plugin such as Eclipse based **Spring Tool Suite**
