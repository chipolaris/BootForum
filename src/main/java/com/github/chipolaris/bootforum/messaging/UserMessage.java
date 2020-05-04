package com.github.chipolaris.bootforum.messaging;

public class UserMessage implements StompMessage {

	private String username;
	private String content;
	private Long timeMillis;
	private Boolean avatarExists;

	public UserMessage() {
	}

	public UserMessage(String username, String content, Long timeMillis, Boolean avatarExists) {
		this.username = username;
		this.content = content;
		this.timeMillis = timeMillis;
		this.avatarExists = avatarExists;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
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
		
		return "userMessage";
	}
}
