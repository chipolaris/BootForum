package com.github.chipolaris.bootforum.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import com.github.chipolaris.bootforum.jsf.bean.FileHandler;

@Controller
public class ChatChannelController {

	private static final Logger logger = LoggerFactory.getLogger(ChatChannelController.class);
	
	@Resource
	private FileHandler fileHandler;
	
	@Resource
	private ChatManager chatManager;
	
	@MessageMapping("/chat/postMessage/{channelId}")
	@SendTo("/channel/{channelId}")
	public UserMessage handlePostMessage(PostMessage message, @DestinationVariable Long channelId, Authentication authentication) throws Exception {
		
		String username = authentication != null ? authentication.getName() : "Anonymous";
		
		logger.info(String.format("user %s posts message to channel %d", username, channelId));
		
		Boolean avatarExists = fileHandler.isAvatarExists(username);
		
		return new UserMessage(username, HtmlUtils.htmlEscape(message.getMessageText()), System.currentTimeMillis(), avatarExists);
	}
	
	@MessageMapping("/chat/postImage/{channelId}")
	@SendTo("/channel/{channelId}")
	public UserImage handlePostImage(PostImage message, @DestinationVariable Long channelId, Authentication authentication) throws Exception {
		
		String username = authentication != null ? authentication.getName() : "Anonymous";
		
		logger.info(String.format("user %s post image to channel %d", username, channelId));
		
		Boolean avatarExists = fileHandler.isAvatarExists(username);
		
		return new UserImage(username, message.getImageBase64(), System.currentTimeMillis(), avatarExists);
	}
	
	@GetMapping("/chat/channelUsers/{channelId}")
	@ResponseBody
	public List<ConnectedUser> getJoinedUsers(@PathVariable String channelId) {
		
		List<ConnectedUser> joinedUsers = new ArrayList<>();
		
		Map<String, Integer> users = chatManager.getSubscribedUserMap().get("/channel/" + channelId);
		
		for(String username : users.keySet()) {
			
			ConnectedUser connectedUser = chatManager.getConnectedUserMap().get(username);
			
			if(connectedUser != null) {
				connectedUser.setAvatarExists(fileHandler.isAvatarExists(username));
			
				joinedUsers.add(connectedUser);
			}
		}
		
		return joinedUsers;
	}
	
	@GetMapping("/chat/connectedUsers")
	@ResponseBody
	public List<ConnectedUser> getConnectedUsers() {
		
		List<ConnectedUser> connectedUsers = new ArrayList<>();
		
		Map<String, ConnectedUser> users = chatManager.getConnectedUserMap();
		
		for(ConnectedUser user : users.values()) {
			
			user.setAvatarExists(fileHandler.isAvatarExists(user.getUsername()));
			connectedUsers.add(user);
		}
		
		return connectedUsers;
	}
}
