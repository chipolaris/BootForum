package com.github.chipolaris.bootforum.messaging;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import com.github.chipolaris.bootforum.jsf.bean.FileHandler;

@Controller
public class ChatChannelController {

	private static final Logger logger = LoggerFactory.getLogger(ChatChannelController.class);
	
	@Resource
	private FileHandler fileHandler;
	
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
}
