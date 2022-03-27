package com.github.chipolaris.bootforum;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.domain.ChatRoom;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DiscussionStat;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.Person;
import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.domain.Tag;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.enumeration.AccountStatus;
import com.github.chipolaris.bootforum.enumeration.UserRole;
import com.github.chipolaris.bootforum.event.DiscussionUpdateEvent;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.DiscussionService;
import com.github.chipolaris.bootforum.service.ForumService;
import com.github.chipolaris.bootforum.service.GenericService;
import com.github.chipolaris.bootforum.service.OptionService;
import com.github.chipolaris.bootforum.service.ServiceResponse;
import com.github.chipolaris.bootforum.service.StatService;
import com.github.chipolaris.bootforum.service.SystemInfoService;
import com.github.chipolaris.bootforum.service.UserService;

@Component
public class DataInitializer implements ApplicationRunner {

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
	
	@Resource
	private OptionService optionService;
	
	@Value("${Application.name}")
	private String applicationName;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.info("Checking application initial data...");
		
		createAdminUser();
		
		this.optionService.init();
		
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
				Discussion discussion = createWelcomeDiscussion(announcementsForum, adminUser);
				
				createBulletinTag(discussion);
				
				// create first chat room
				createFirstChatRoom();
			}
		}
	}
	
	private void createBulletinTag(Discussion discussion) {
		
		Tag tag = new Tag();
		
		tag.setLabel("Bulletin");
		tag.setColor("1e90ff"); //DodgerBlue
		tag.setIcon("pi pi-book");
		
		genericService.saveEntity(tag);
		
		discussion.setTags(List.of(tag));
		
		genericService.updateEntity(discussion);
		
		applicationEventPublisher.publishEvent(new DiscussionUpdateEvent(this, discussion));
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
	
	private Forum createAnouncementsForum(User user) {
		
		Forum forum = new Forum();
		
		forum.setTitle("Anouncements");
		forum.setDescription("Latest news/announcements");
		forum.setActive(false);
		forum.setIcon("pi pi-volume-up");
		forum.setIconColor("ff7e00"); // Amber (SAE/ECE)
		
		forum = forumService.addForum(forum, null).getDataObject();
		
		return forum;
	}
	
	private Discussion createWelcomeDiscussion(Forum forum, User admin) {
		
		Discussion discussion = new Discussion();
		discussion.setForum(forum);
		discussion.setStat(new DiscussionStat());
		discussion.setImportant(true);
		discussion.setSticky(true);
		discussion.setTitle("Welcome to " + applicationName);
		
		Comment comment = new Comment();
		
		comment.setContent("Welcome. Please read forum announcements from forum administrators");
		
		discussionService.addDiscussion(discussion, comment, admin, Collections.emptyList(), Collections.emptyList());
		
		return discussion;
	}
	
	private void createFirstChatRoom() {
		
		ChatRoom chatRoom = new ChatRoom();
		
		chatRoom.setLabel("First Chat Room");
		chatRoom.setColor("800080");
		chatRoom.setIcon("pi pi-heart-fill");
		chatRoom.setDisabled(false);
		
		genericService.saveEntity(chatRoom);
	}
}
