package com.github.chipolaris.bootforum.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chipolaris.bootforum.dao.UserDAO;
import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.domain.Forum;
import com.github.chipolaris.bootforum.domain.ForumGroup;
import com.github.chipolaris.bootforum.domain.Person;
import com.github.chipolaris.bootforum.domain.Preferences;
import com.github.chipolaris.bootforum.domain.User;
import com.github.chipolaris.bootforum.domain.UserStat;
import com.github.chipolaris.bootforum.enumeration.AccountStatus;
import com.github.chipolaris.bootforum.enumeration.UserRole;
import com.github.chipolaris.bootforum.event.ForumGroupAddEvent;
import com.github.chipolaris.bootforum.event.UserRegistrationEvent;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

@Service @Transactional
public class SimulateDataService {

	private static final Logger logger = LoggerFactory.getLogger(SimulateDataService.class);
	
	private Random random = new Random();
	
	private Lorem lorem = LoremIpsum.getInstance();
	
	@Resource
	private GenericService genericService;
	
	@Resource
	private ForumService forumService;
	
	@Resource
	private CommentService commentService;
	
	@Resource
	private PasswordEncoder passwordEncoder;
	
	private String encodedPassword;
	
	@Resource
	private UserDAO userDAO;
	
	@Resource
	private ApplicationEventPublisher applicationEventPublisher;
	
	@PostConstruct
	private void init() {
		this.encodedPassword = passwordEncoder.encode("secret");
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> simulateUsers(int numUsers) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		for(int i = 0; i < numUsers; i++) {
			
			try {
			
				User randomUser = createRandomUser();
						
				applicationEventPublisher.publishEvent(
						new UserRegistrationEvent(this, randomUser));
			}
			catch(Exception e) {
				
			}
		}
		
		return response;
	}
	
	private User createRandomUser() {
		
		User user = new User();
		
		LoremIpsum loremIpsum = LoremIpsum.getInstance();
		
		user.setUsername(createUsername());
		user.setPassword(this.encodedPassword);
		user.setUserRole(UserRole.USER);
		user.setAccountStatus(AccountStatus.ACTIVE);
		
		Person person = new Person();
		user.setPerson(person);
		
		person.setFirstName(StringUtils.capitalize(loremIpsum.getFirstName()));
		person.setLastName(StringUtils.capitalize(loremIpsum.getLastName()));
		person.setEmail(loremIpsum.getEmail());
		
		user.setPreferences(new Preferences());
		user.setStat(new UserStat());
		
		// Security Challenges
		
		userDAO.createUser(user);
		
		return user;
	}

	private String createUsername() {
		
		String username = null;
		
		while(username == null) {
			username = RandomStringUtils.randomAlphanumeric(5, 7);
			if(userDAO.usernameExists(username)) {
				username = null; // regenerate a new random username
			}
		}
		
		return username;
	}
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> simulateDiscussion(String forumGroupTitle, 
			String forumTitle, String discussionTitle, int numComments, 
			List<String> commentors) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		ForumGroup simulatedForumGroup = createSimulatedForumGroup(forumGroupTitle);
		Forum simulatedForum = createSimulatedForum(simulatedForumGroup, forumTitle);
		createSimulatedDiscussion(simulatedForum, discussionTitle, numComments, commentors);
		
		// publish event
		applicationEventPublisher.publishEvent(new ForumGroupAddEvent(this, simulatedForumGroup));
		
		return response;
	}
	
	
	@Transactional(readOnly = false)
	public ServiceResponse<Void> simulateForumData(String forumGroupTitle, 
			String forumTitle, String discussionTitle, int numDiscussions, 
			List<String> commentors) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		ForumGroup simulatedForumGroup = createSimulatedForumGroup(forumGroupTitle);
		Forum simulatedForum = createSimulatedForum(simulatedForumGroup, forumTitle);
		
		for(int i = 0; i < numDiscussions; i++) {
			// create a discussion with random number of comments between 2 and 10
			createSimulatedDiscussion(simulatedForum, StringUtils.capitalize(lorem.getWords(4, 8)) + " [Simulated " + i + "]", 
					2 + random.nextInt(9), commentors);
		}
		
		// publish event
		applicationEventPublisher.publishEvent(new ForumGroupAddEvent(this, simulatedForumGroup));
		
		return response;
	}
	
	private ForumGroup createSimulatedForumGroup(String forumGroupTitle) {
		
		ForumGroup forumGroup = new ForumGroup();
		
		forumGroup.setTitle(forumGroupTitle);
		forumGroup.setIcon("fa fa-play");
		forumGroup.setIconColor("ff0000"); // red
		
		forumGroup = forumService.addForumGroup(forumGroup, null).getDataObject();
		
		return forumGroup;
	}

	private Forum createSimulatedForum(ForumGroup forumGroup, String forumTitle) {
		
		Forum forum = new Forum();
		
		forum.setTitle(forumTitle);
		forum.setIcon("fa fa-play");
		forum.setIconColor("ff0000"); // red
		
		forum = forumService.addForum(forum, forumGroup).getDataObject();
		
		return forum;
	}
	
	private User findUser(String username) {
		
		Map<String, Object> filters = new HashMap<String, Object>();
		
		filters.put("username", username);
		
		return genericService.getEntities(User.class, filters).getDataObject().get(0);
	}

	private void createSimulatedDiscussion(Forum forum, String discussionTitle, int numComments, List<String> commentors) {
		
		logger.info("Creating simulated discussion.....");
		
		Discussion discussion = new Discussion();
		discussion.setForum(forum);
		discussion.setTitle(discussionTitle);
		
		Comment comment = new Comment();
		
		comment.setContent(lorem.getParagraphs(2, 4));
		
		String discussionCreator = commentors.get(random.nextInt(commentors.size()));
		User admin = findUser(discussionCreator);
		
		if(admin != null) {
		
			commentService.addDiscussion(discussion, comment, admin, Collections.emptyList(), Collections.emptyList());
		
			createSimulateComments(discussion, numComments, commentors);
		}
	}

	private void createSimulateComments(Discussion discussion, int commentCount, List<String> commentors) {
		
		List<Comment> createdComments = new ArrayList<>();
		
		for(int i = 0; i < commentCount; i++) {
			
			String commentor = commentors.get(random.nextInt(commentors.size()));
			User user = findUser(commentor);
			
			if(user == null) { 
				logger.info(String.format("Commentor '%s' not found", commentor));
				continue;
			}
			
			Comment comment = new Comment();
			
			if(createdComments.size() > 0) {
				comment.setReplyTo(createdComments.get(random.nextInt(createdComments.size())));
			}
			comment.setDiscussion(discussion);
			
			comment.setTitle(StringUtils.capitalize(lorem.getWords(4, 8)));
			comment.setContent(lorem.getParagraphs(2, 4));
			
			commentService.addReply(comment, user, Collections.emptyList(), Collections.emptyList());
			
			createdComments.add(comment);
		}
	}
}
