package com.github.chipolaris.bootforum.jsf.bean;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ChatManager {

	private static final Logger logger = LoggerFactory.getLogger(ChatManager.class);
	
	private Map<String, Date> connectedUsersMap = 
			Collections.synchronizedMap(new HashMap<>());
	private Set<ConnectedUser> connectedUsers = 
			Collections.synchronizedSet(new TreeSet<>());
	private Map<String, Set<ConnectedUser>> subscribedUsers = 
			Collections.synchronizedMap(new HashMap<String, Set<ConnectedUser>>());
	
	public Map<String, Date> getConnectedUsersMap() {
		return connectedUsersMap;
	}
	public void setConnectedUsersMap(Map<String, Date> connectedUsersMap) {
		this.connectedUsersMap = connectedUsersMap;
	}
	
	public Set<ConnectedUser> getConnectedUsers() {
		return connectedUsers;
	}
	public void setConnectedUsers(Set<ConnectedUser> connectedUsers) {
		this.connectedUsers = connectedUsers;
	}

	public Map<String, Set<ConnectedUser>> getSubscribedUsers() {
		return subscribedUsers;
	}
	public void setSubscribedUsers(Map<String, Set<ConnectedUser>> subscribedUsers) {
		this.subscribedUsers = subscribedUsers;
	}

	public void addSubscribedUser(String topicName, String username) {
		
		logger.info(String.format("Adding subscribe username '%s' to topic '%s'", username, topicName));
		
		if(!subscribedUsers.containsKey(topicName)) {
			subscribedUsers.put(topicName, new TreeSet<>());
		}
		
		Set<ConnectedUser> connectedUsers = subscribedUsers.get(topicName);
		
		ConnectedUser connectedUser = new ConnectedUser(username);
		
		if(!connectedUsers.contains(connectedUser)) {
			connectedUsers.add(connectedUser);
		}
	}	

	public void removeSubscribedUser(String topicName, String username) {
		logger.info(String.format("Removing subscribe username '%s' to topic '%s'", username, topicName));
		
		Set<ConnectedUser> connectedUsers = subscribedUsers.get(topicName);
		
		if(connectedUsers != null) {
			connectedUsers.remove(new ConnectedUser(username));
		}
	}
	
	public void addConnectedUser(String username) {
		
		logger.info(String.format("Adding connected username '%s'", username));
		
		// add or replace the old key with new date/timestamp
		connectedUsersMap.put(username, Calendar.getInstance().getTime());
						
		ConnectedUser connectedUser = new ConnectedUser(username);
		if(connectedUsers.contains(connectedUser)) {
			connectedUsers.remove(connectedUser);
		}
		// add connectedUser back so it contains new timestamp
		this.connectedUsers.add(connectedUser);
	}
	
	public void removeConnectedUser(String username) {
		
		logger.info(String.format("Removing username '%s'", username));
		
		// 
		connectedUsersMap.remove(username);		
		
		ConnectedUser connectedUser = new ConnectedUser(username);
		connectedUsers.remove(connectedUser);
	}

	/**
	 * Comparable class to represent a connected User with a timestamp
	 */
	public class ConnectedUser implements Comparable<ConnectedUser> {

		public ConnectedUser(String username) {
			this.username = username;
			this.connectedDate = Calendar.getInstance().getTime();
		}
		
		private String username;
		private Date connectedDate;
		
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}

		public Date getConnectedDate() {
			return connectedDate;
		}
		public void setConnectedDate(Date connectedDate) {
			this.connectedDate = connectedDate;
		}
		
		/*
		 * compare in the reverse order of connectedTimestamp 
		 */
		@Override
		public int compareTo(ConnectedUser other) {
			return -this.connectedDate.compareTo(other.getConnectedDate());
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((username == null) ? 0 : username.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConnectedUser other = (ConnectedUser) obj;
			
			if (username == null) {
				if (other.username != null)
					return false;
			} 
			else if (!username.equals(other.username)) {
				return false;
			}
			return true;
		}
	}	
}
