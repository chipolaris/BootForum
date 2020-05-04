package com.github.chipolaris.bootforum.messaging;

public class ChannelMessage implements StompMessage {

	public ChannelMessage(String message, long timestamp) {
		this.message = message;
		this.timeMillis = timestamp;	
	}
	
	private String message;
	private Long timeMillis;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	public Long getTimeMillis() {
		return timeMillis;
	}
	public void setTimeMillis(Long timeMillis) {
		this.timeMillis = timeMillis;
	}
	
	@Override
	public String getType() {
		return "channelMessage";
	}
}
