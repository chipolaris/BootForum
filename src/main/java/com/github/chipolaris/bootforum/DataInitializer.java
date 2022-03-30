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

import com.github.chipolaris.bootforum.domain.AvatarOption;
import com.github.chipolaris.bootforum.domain.ChatRoom;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.CommentOption;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.DiscussionStat;
import com.github.chipolaris.bootforum.domain.DisplayOption;
import com.github.chipolaris.bootforum.domain.EmailOption;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.Person;
import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.domain.PrivateMessageOption;
import com.github.chipolaris.bootforum.domain.RegistrationOption;
import com.github.chipolaris.bootforum.domain.RemoteIPFilterOption;
import com.github.chipolaris.bootforum.domain.RemoteIPFilterOption.FilterType;
import com.github.chipolaris.bootforum.domain.Tag;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.enumeration.AccountStatus;
import com.github.chipolaris.bootforum.enumeration.UserRole;
import com.github.chipolaris.bootforum.event.AvatarOptionLoadEvent;
import com.github.chipolaris.bootforum.event.CommentOptionLoadEvent;
import com.github.chipolaris.bootforum.event.DiscussionUpdateEvent;
import com.github.chipolaris.bootforum.event.DisplayOptionLoadEvent;
import com.github.chipolaris.bootforum.event.EmailOptionLoadEvent;
import com.github.chipolaris.bootforum.event.PrivateMessageOptionLoadEvent;
import com.github.chipolaris.bootforum.event.RegistrationOptionLoadEvent;
import com.github.chipolaris.bootforum.event.RemoteIPFilterOptionLoadEvent;
import com.github.chipolaris.bootforum.service.AckCodeType;
import com.github.chipolaris.bootforum.service.DiscussionService;
import com.github.chipolaris.bootforum.service.ForumService;
import com.github.chipolaris.bootforum.service.GenericService;
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
	
	@Value("${Application.name}")
	private String applicationName;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.info("Checking application initial data...");
		
		createAdminUser();
		
		createRegistrationOption();
		createEmailOption();
		createDisplayOption();
		createRemoteIPFilterOption();
		createCommentOption();
		createPrivateMessageOption();
		createAvatarOption();
		
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
	
	private void createRegistrationOption() {
		
		RegistrationOption registrationOption = genericService.findEntity(RegistrationOption.class, 1L).getDataObject();
		
		if(registrationOption != null) {
			logger.info("Registration Option has already been created");
		}
		else {
			
			logger.info("Registration Option has NOT been created. Creating..");
			
			registrationOption = new RegistrationOption();
			registrationOption.setId(1L);
			
			registrationOption.setEnableCaptcha(true);
			registrationOption.setEnableEmailConfirm(true);
			registrationOption.setRegistrationEmailSubject(REGISTRATION_EMAIL_SUBJECT);
			registrationOption.setRegistrationEmailTemplate(REGISTRATION_EMAIL_TEMPLATE);
			registrationOption.setPasswordResetEmailSubject(PASSWORD_RESET_EMAIL_SUBJECT);
			registrationOption.setPasswordResetEmailTemplate(PASSWORD_RESET_EMAIL_TEMPLATE);
			
			genericService.saveEntity(registrationOption);
		}
		
		applicationEventPublisher.publishEvent(new RegistrationOptionLoadEvent(this, registrationOption));
	}
	
	/*
	 * Default values on first time system initialization
	 */
	private static final String REGISTRATION_EMAIL_SUBJECT = "Confirm account registration at BootForum";
	
	private static final String REGISTRATION_EMAIL_TEMPLATE = 
			"<p><strong>Hi #username</strong>,</p>"
		  +	"<p>This email&nbsp;<strong>#email</strong>&nbsp;has been used for account registration on <strong>BootForum</strong>.</p>"
		  +	"<p>If that wasn&#39;t your intention, kindly ignore this email. Otherwise, please lick on this link #confirm-url to activate your account.</p>"
		  +	"<p>Regards,</p>";
	
	private static final String PASSWORD_RESET_EMAIL_SUBJECT = "Password reset requested at BootForum";
	
	private static final String PASSWORD_RESET_EMAIL_TEMPLATE =
			"<p><strong>Hi #username</strong>,</p>"
		  + "<p>Here is the <strong>#reset-url</strong> to reset your password in <strong>BootForum</strong></p>"
		  + "<p>If you didn&#39;t request this, kindly ignore this email.</p>"
		  + "<p>Regards,</p>";
	
	private void createEmailOption() {

		EmailOption emailOption = genericService.findEntity(EmailOption.class, 1L).getDataObject();
		
		if(emailOption != null) {
			logger.info("Email Option has already been created");
		}
		else {
			
			logger.info("Email Option has NOT been created. Creating..");
			
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
		
		applicationEventPublisher.publishEvent(new EmailOptionLoadEvent(this, emailOption));
	}
	
	private void createDisplayOption() {
		
		DisplayOption displayOption = genericService.findEntity(DisplayOption.class, 1L).getDataObject();
		
		if(displayOption != null) {
			logger.info("Display Option has already been created");
		}
		else {
			
			logger.info("Display Option has NOT been created. Creating..");
			
			displayOption = new DisplayOption();
			displayOption.setId(1L);
			
			// default values
			displayOption.setThemeColor("w3-theme-light-blue");
			displayOption.setThemeComponent("saga");
			
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
		
		applicationEventPublisher.publishEvent(new DisplayOptionLoadEvent(this, displayOption));
	}
	
	private void createRemoteIPFilterOption() {
		
		RemoteIPFilterOption remoteIPFilterOption = genericService.findEntity(RemoteIPFilterOption.class, 1L).getDataObject();
		
		if(remoteIPFilterOption != null) {
			logger.info("Remote IP Filter Option has already been created");
		}
		else {
			
			logger.info("Remote IP Filter Option has NOT been created. Creating..");
			
			remoteIPFilterOption = new RemoteIPFilterOption();
			
			remoteIPFilterOption.setId(1L);
			remoteIPFilterOption.setFilterType(FilterType.NONE);
			
			genericService.saveEntity(remoteIPFilterOption);
		}
		
		applicationEventPublisher.publishEvent(new RemoteIPFilterOptionLoadEvent(this, remoteIPFilterOption));
	}
	
	
	private void createCommentOption() {
		
		CommentOption commentOption = genericService.findEntity(CommentOption.class, 1L).getDataObject();
		
		if(commentOption != null) {
			logger.info("Comment Option's already initialized");
		}
		else {
			
			logger.info("Comment Option is NOT initialized. Creating..");
			
			commentOption = new CommentOption();
			commentOption.setId(1L);
			
			// default values
			commentOption.setMinCharDiscussionTitle(1);
			commentOption.setMaxCharDiscussionTitle(80);
			commentOption.setMinCharDiscussionContent(1);
			commentOption.setMaxCharDiscussionContent(10000*1024); // 10,2400KB ~ 10MB
			commentOption.setMaxDiscussionThumbnail(5);
			commentOption.setMaxDiscussionAttachment(5);
			commentOption.setMaxByteDiscussionThumbnail(5000*1024); // 5120KB ~ 5MB
			commentOption.setMaxByteDiscussionAttachment(5000*1024); // 5120KB ~ 5MB
			commentOption.setAllowDiscussionTitleEdit(true);
			
			commentOption.setMinCharCommentTitle(1);
			commentOption.setMaxCharCommentTitle(80);
			commentOption.setMinCharCommentContent(1);
			commentOption.setMaxCharCommentContent(10000*1024); // 10,000KB ~ 10MB
			commentOption.setMaxCommentThumbnail(3);
			commentOption.setMaxCommentAttachment(3);
			commentOption.setMaxByteCommentThumbnail(5000*1024); // 1000KB ~ 5MB
			commentOption.setMaxByteCommentAttachment(5000*1024); // 1000KB ~ 5MB
			commentOption.setAllowCommentEdit(true);
			
			genericService.saveEntity(commentOption);
		}
		
		applicationEventPublisher.publishEvent(new CommentOptionLoadEvent(this, commentOption));
	}

	private void createPrivateMessageOption() {
		
		PrivateMessageOption privateMessageOption = genericService.findEntity(PrivateMessageOption.class, 1L).getDataObject();
		
		if(privateMessageOption != null) {
			logger.info("Private Message Option has already been created");
		}
		else {
			logger.info("Private Message Option has NOT been created. Creating..");
			
			privateMessageOption = new PrivateMessageOption();
			privateMessageOption.setId(1L);
			
			// default values
			privateMessageOption.setMinCharTitle(1);
			privateMessageOption.setMaxCharTitle(80);
			privateMessageOption.setMinCharContent(1);
			privateMessageOption.setMaxCharContent(1000*1024); // 1,240KB ~1MB
			privateMessageOption.setMaxAttachment(3);
			privateMessageOption.setMaxByteAttachment(5000*1024); // 5120KB ~ 5MB			
			
			genericService.saveEntity(privateMessageOption);
		}
		
		applicationEventPublisher.publishEvent(new PrivateMessageOptionLoadEvent(this, privateMessageOption));
	}
	
	private void createAvatarOption() {
		
		AvatarOption avatarOption = genericService.findEntity(AvatarOption.class, 1L).getDataObject();
		
		if(avatarOption != null) {
			logger.info("Avatar Option has already been created");
		}
		else {
			logger.info("Avatar Option has NOT been created. Creating..");
		
			avatarOption = new AvatarOption();
			avatarOption.setId(1L);
			
			avatarOption.setMaxFileSize(500*1024); // ~500KB
			avatarOption.setMaxWidth(800);
			avatarOption.setMaxHeight(800);
			
			genericService.saveEntity(avatarOption);
		}
		
		applicationEventPublisher.publishEvent(new AvatarOptionLoadEvent(this, avatarOption));
	}
}
