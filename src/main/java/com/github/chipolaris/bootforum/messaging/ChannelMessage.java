package com.github.chipolaris.bootforum.messaging;

public class ChannelMessage implements StompMessage {

	public ChannelMessage(String username, String action, long timestamp, Boolean avatarExists) {
		this.username = username;
		this.action = action;
		this.timeMillis = timestamp;
		this.avatarExists = avatarExists;
	}
	
	private String username;
	private String action;
	private Long timeMillis;
	private Boolean avatarExists;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}

	public Long getTimeMillis() {
		return timeMillis;
	}
	public void setTimeMillis(Long timeMillis) {
		this.timeMillis = timeMillis;
	}
	
	public Boolean getAvatarExists() {
		return avatarExists;
	}
	public void setAvatarExists(Boolean avatarExists) {
		this.avatarExists = avatarExists;
	}
	
	@Override
	public String getType() {
		return "channelMessage";
	}
}
