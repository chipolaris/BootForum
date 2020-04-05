package com.github.chipolaris.bootforum;

import java.util.ArrayList;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.Person;
import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.enumeration.AccountStatus;
import com.github.chipolaris.bootforum.enumeration.UserRole;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.CommentService;
import com.github.chipolaris.bootforum.service.ForumService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.StatService;
import com.github.chipolaris.bootforum.service.UserService;

@Component
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private StatService statService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private ForumService forumService;
	
	@Resource
	private CommentService commentService;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent arg0) {
		
		logger.info("Checking application intial data...");
		
		// create admin user if not already exist in the system
		User adminUser = userService.getSystemAdminUser().getDataObject();
		
		if(adminUser != null) {
			logger.info("System Admin user already exists");
		}
		else {
			logger.info("System Admin user does not exist, creating....");
			adminUser = createAdminUser();
			ServiceResponse<Void> response = userService.addUser(adminUser);
			
			if(response.getAckCode() != AckCodeType.SUCCESS) {
				logger.error("System Admin user does not exist but unable to create one");
				for(String msg : response.getMessages()) {
					logger.error(msg);
				}
			}
			else {
				logger.info("System Admin user created successfully. Creating Announcements forum and Welcome discussion");
				Forum announcementsForum = createAnouncementsForum(adminUser);
				createWelcomeDiscussion(announcementsForum, adminUser);
			}
		}
	}

	private User createAdminUser() {
		User admin = new User();
		
		admin.setUsername("admin");
		admin.setPassword("secret");
		admin.setUserRole(UserRole.ADMIN);
		admin.setAccountStatus(AccountStatus.ACTIVE);
		
		Person person = new Person();
		admin.setPerson(person);
		
		person.setFirstName("System");
		person.setLastName("Admin");
		person.setEmail("admin@updateme.net");
		
		admin.setPreferences(new Preferences());
		admin.setStat(new UserStat());
		
		// Security Challenges
		
		return admin;
	}
	
	private Forum createAnouncementsForum(User user) {
		
		Forum forum = new Forum();
		
		forum.setTitle("Anouncements");
		forum.setIcon("fa fa-bullhorn");
		forum.setIconColor("ff7e00"); // Amber (SAE/ECE)
		
		forum = forumService.addForum(forum, null).getDataObject();
		
		return forum;
	}
	
	private void createWelcomeDiscussion(Forum forum, User admin) {
		
		Discussion discussion = new Discussion();
		discussion.setForum(forum);
		discussion.setSticky(true);
		discussion.setTitle("Welcome to BootForum");
		
		Comment comment = new Comment();
		
		comment.setContent("Welcome. Please read forum announcements from forum administrators");
		
		commentService.addDiscussion(discussion, comment, admin, new ArrayList<>(), new ArrayList<>());
	}
}
