package com.github.chipolaris.bootforum;

import java.util.Collections;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DisplayOption;
import com.github.chipolaris.bootforum.domain.EmailOption;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.Person;
import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.domain.RegistrationOption;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.enumeration.AccountStatus;
import com.github.chipolaris.bootforum.enumeration.UserRole;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.DiscussionService;
import com.github.chipolaris.bootforum.service.ForumService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.StatService;
import com.github.chipolaris.bootforum.service.SystemInfoService;
import com.github.chipolaris.bootforum.service.UserService;

@Component
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

	private static final String REGISTRATION_EMAIL_TEMPLATE = 
			"<p>Thank you <b>&lt;username&gt;</b> for registration with &lt;app-name&gt;</p>"
		  + "<p>Please confirm your email with the link below</p>" 
		  + "<p>&lt;confirm-link&gt;</p>"
		  + "<p>If you did not register for &lt;app-name&gt;, please disregard this and no action is necessary</p>"
		  + "<p>The &lt;app-name&gt; team</p>";

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
	private DiscussionService discussionService;
	
	@Resource
	private SystemInfoService systemInfoService;
	
	@Value("${Application.name}")
	private String applicationName;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		
		logger.info("Checking application initial data...");
		
		createAdminUser();
		
		createDisplayOption();
		createEmailOption();
		createRegistrationOption();
		
		systemInfoService.refreshStatistics();
	}

	private void createAdminUser() {
		// create admin user if not already exist in the system
		User adminUser = userService.getSystemAdminUser().getDataObject();
		
		if(adminUser != null) {
			logger.info("System Admin user already exists");
		}
		else {
			logger.info("System Admin user does not exist, creating....");
			adminUser = createAdminUserObject();
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
	
	private User createAdminUserObject() {
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
	
	private void createDisplayOption() {
		
		DisplayOption displayOption = genericService.getEntity(DisplayOption.class, 1L).getDataObject();
		
		if(displayOption != null) {
			logger.info("Display Option's already initialized");
		}
		else {
			
			logger.info("Display Option is NOT initialized. Creating..");
			
			displayOption = new DisplayOption();
			displayOption.setId(1L);
			
			// default values
			displayOption.setThemeColor("w3-theme-light-blue");
			displayOption.setThemeComponent("nova-light");
			
			displayOption.setShowMostCommentsDiscussions(true);
			displayOption.setNumMostCommentsDiscussions(5);
			
			displayOption.setShowMostRecentDiscussions(true);
			displayOption.setNumMostRecentDiscussions(5);
			
			displayOption.setShowMostViewsDiscussions(true);
			displayOption.setNumMostViewsDiscussions(5);
			
			displayOption.setShowDiscussionsForTag(true);
			displayOption.setNumDiscussionsPerTag(5);
			
			genericService.saveEntity(displayOption);
		}
	}
	
	private void createEmailOption() {

		EmailOption emailOption = genericService.getEntity(EmailOption.class, 1L).getDataObject();
		
		if(emailOption != null) {
			logger.info("Email Option's already initialized");
		}
		else {
			
			logger.info("Email Option is NOT initialized. Creating..");
			
			emailOption = new EmailOption();
			emailOption.setId(1L);
			
			emailOption.setCreateBy("system");
			emailOption.setHost("to-be-update");
			emailOption.setPort(0);
			emailOption.setUsername("to-be-update");
			emailOption.setPassword("to-be-update");
			emailOption.setTlsEnable(true);
			
			genericService.saveEntity(emailOption);
		}
	}
	
	private void createRegistrationOption() {
		
		RegistrationOption registrationOption = genericService.getEntity(RegistrationOption.class, 1L).getDataObject();
		
		if(registrationOption != null) {
			logger.info("Registration Option's already initialized");
		}
		else {
			
			logger.info("Registration Option is NOT initialized. Creating..");
			
			registrationOption = new RegistrationOption();
			registrationOption.setId(1L);
			
			registrationOption.setEnableEmailConfirm(false);
			registrationOption.setRegistrationEmailTemplate(REGISTRATION_EMAIL_TEMPLATE);
			
			genericService.saveEntity(registrationOption);
		}
	}
	
	private Forum createAnouncementsForum(User user) {
		
		Forum forum = new Forum();
		
		forum.setTitle("Anouncements");
		forum.setDescription("Latest news/announcements");
		forum.setActive(false);
		forum.setIcon("fa fa-bullhorn");
		forum.setIconColor("ff7e00"); // Amber (SAE/ECE)
		
		forum = forumService.addForum(forum, null).getDataObject();
		
		return forum;
	}
	
	private void createWelcomeDiscussion(Forum forum, User admin) {
		
		Discussion discussion = new Discussion();
		discussion.setForum(forum);
		discussion.setImportant(true);
		discussion.setSticky(true);
		discussion.setTitle("Welcome to " + applicationName);
		
		Comment comment = new Comment();
		
		comment.setContent("Welcome. Please read forum announcements from forum administrators");
		
		discussionService.addDiscussion(discussion, comment, admin, Collections.emptyList(), Collections.emptyList());
	}
}
