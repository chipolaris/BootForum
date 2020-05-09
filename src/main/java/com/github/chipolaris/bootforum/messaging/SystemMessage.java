package com.github.chipolaris.bootforum.messaging;

public class SystemMessage implements StompMessage {

	private String content;
	private Long timeMillis;
	
	public SystemMessage(String content, Long timeMillis) {
		super();
		this.content = content;
		this.timeMillis = timeMillis;
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

	@Override
	public String getType() {
		return "systemMessage";
	}
}
