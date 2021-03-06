package com.github.chipolaris.bootforum.messaging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.chipolaris.bootforum.messaging.ConnectedUser;

@Component
public class ChatManager {

	private static final Logger logger = LoggerFactory.getLogger(ChatManager.class);
	
	private Map<String, ConnectedUser> connectedUserMap = 
			Collections.synchronizedMap(new HashMap<>());
	
	private Map<String, Map<String, Integer>> subscribedUserMap = 
			Collections.synchronizedMap(new HashMap<String, Map<String, Integer>>());
	
	public Map<String, ConnectedUser> getConnectedUserMap() {
		return connectedUserMap;
	}
	public void setConnectedUserMap(Map<String, ConnectedUser> connectedUserMap) {
		this.connectedUserMap = connectedUserMap;
	}

	public Map<String, Map<String, Integer>> getSubscribedUserMap() {
		return subscribedUserMap;
	}
	public void setSubscribedUserMap(Map<String, Map<String, Integer>> subscribedUserMap) {
		this.subscribedUserMap = subscribedUserMap;
	}

	public boolean addSubscribedUser(String channelName, String username) {
		
		logger.info(String.format("Adding subscribe username '%s' to topic '%s'", username, channelName));
		
		boolean result = false;
		
		if(!subscribedUserMap.containsKey(channelName)) {
			subscribedUserMap.put(channelName, new HashMap<>());
		}
		
		Map<String, Integer> connectedUsers = subscribedUserMap.get(channelName);
		
		Integer count = connectedUsers.get(username);
		if(count == null) {
			connectedUsers.put(username, 1);
			result = true;
		}
		else {
			connectedUsers.put(username, count + 1);
		}
		
		return result;
	}	

	public boolean removeSubscribedUser(String channelName, String username) {
		
		logger.info(String.format("Removing subscribe username '%s' from topic '%s'", username, channelName));
		
		boolean result = false;
		
		Map<String, Integer> connectedUsers = subscribedUserMap.get(channelName);
		
		if(connectedUsers != null) {
			
			Integer count = connectedUsers.get(username);
			
			if(count != null) {
				
				count--;
				
				if(count <= 0) {
					connectedUsers.remove(username);
					result = true;
				}
				else {
					connectedUsers.put(username, count);
				}
			}
		}
		
		
		return result;
	}
	
	public void addConnectedUser(String username) {
		
		logger.info(String.format("Adding connected username '%s'", username));
		
		ConnectedUser connectedUser = connectedUserMap.get(username);
		
		if(connectedUser != null) {
			connectedUser.addSessionCount(1);
		}
		else {
			connectedUserMap.put(username, new ConnectedUser(username));
		}
	}
	
	public void removeConnectedUser(String username) {
		
		logger.info(String.format("Removing username '%s'", username));
		
		ConnectedUser connectedUser = connectedUserMap.get(username);
		if(connectedUser != null) {
			
			connectedUser.addSessionCount(-1);
			
			if(connectedUser.getSessionCount() == 0) {
				connectedUserMap.remove(username);
			}
		}
	}	
}
