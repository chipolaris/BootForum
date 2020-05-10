package com.github.chipolaris.bootforum.listener;

import java.security.Principal;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import com.github.chipolaris.bootforum.jsf.bean.FileHandler;
import com.github.chipolaris.bootforum.messaging.ChannelMessage;
import com.github.chipolaris.bootforum.messaging.ChatManager;
import com.github.chipolaris.bootforum.messaging.SystemMessage;

@Component
public class WebSocketEventsListener {

	private static final Logger logger = LoggerFactory.getLogger(WebSocketEventsListener.class);
	
	@Resource
	private ChatManager chatManager;
	
	@Resource
	private SimpMessagingTemplate template;
	
	@Resource
	private FileHandler fileHandler;
	
	@EventListener
	private void handleSessionConnectedEvent(SessionConnectedEvent event) {
		
		String username = "Anonymous";
		Principal user = event.getUser();
		if(user != null) {
			username = user.getName();
		}
		
		logger.info(String.format("Connect username: %s", username));
		
		chatManager.addConnectedUser(username);
		
		this.template.convertAndSend("/system",
				new SystemMessage(String.format("User \"%s\" connected", username), System.currentTimeMillis()));
	}
	
	@EventListener
	private void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
		
		String username = "Anonymous";
		Principal user = event.getUser();
		if(user != null) {
			username = user.getName();
		}
		
		logger.info(String.format("Disconnect username: %s", username));
		
		chatManager.removeConnectedUser(username);
		
		this.template.convertAndSend("/system",
				new SystemMessage(String.format("User \"%s\" disconnected", username), System.currentTimeMillis()));
	}
	
	@EventListener
	private void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
		
		Message<byte[]> message = event.getMessage();
		//https://stackoverflow.com/questions/54330744/spring-boot-websocket-how-to-get-notified-on-client-subscriptions
		// String simpDestination = (String) message.getHeaders().get("simpDestination");
		
		// https://stackoverflow.com/questions/54658349/detect-destination-channel-of-sessionunsubscribeevent
		SimpMessageHeaderAccessor simpMessageHeaderAccessor = SimpMessageHeaderAccessor.wrap(message);
		String simpDestination = simpMessageHeaderAccessor.getDestination();
		String subscriptionId = simpMessageHeaderAccessor.getSubscriptionId();
		Map<String, Object> sessionAttributes = simpMessageHeaderAccessor.getSessionAttributes();
		
		String username = "Anonymous";
		Principal user = event.getUser();
		if(user != null) {
			username = user.getName();
		}
		
		boolean addResult = chatManager.addSubscribedUser(simpDestination, username);
		sessionAttributes.put(subscriptionId, simpDestination);
		
		if(addResult) {
			logger.info(String.format("User: %s subscribes to simpDestination: %s", username, simpDestination));
			
			Boolean avatarExists = fileHandler.isAvatarExists(username);
			
			this.template.convertAndSend(simpDestination,
					new ChannelMessage(username, "entered", System.currentTimeMillis(), avatarExists));
		}
		else {
			logger.info(String.format("Another session from user: %s subscribes to simpDestination: %s", username, simpDestination));
		}
	}
	
	@EventListener
	private void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
		
		Message<byte[]> message = event.getMessage();
		//https://stackoverflow.com/questions/54330744/spring-boot-websocket-how-to-get-notified-on-client-subscriptions
		SimpMessageHeaderAccessor simpMessageHeaderAccessor = SimpMessageHeaderAccessor.wrap(message);
		
		String username = "Anonymous";
		Principal user = event.getUser();
		if(user != null) {
			username = user.getName();
		}
		
		String subscriptionId = simpMessageHeaderAccessor.getSubscriptionId();
		Map<String, Object> sessionAttributes = simpMessageHeaderAccessor.getSessionAttributes();
		String simpDestination = (String)sessionAttributes.get(subscriptionId);
		
		boolean removeResult = chatManager.removeSubscribedUser(simpDestination, username);
		
		if(removeResult) {
			logger.info(String.format("User %s unsubscribes from simpDestination: %s", username, simpDestination));
			
			this.template.convertAndSend(simpDestination,
					new ChannelMessage(username, "left", System.currentTimeMillis(), null));
		}
		else {
			logger.info(String.format("A session from user %s unsubscribes to simpDestination: %s", username, simpDestination));
		}
	}
}
