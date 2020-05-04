package com.github.chipolaris.bootforum.messaging;

public class PostMessage {

	private String messageText;

	public PostMessage() {
	}

	public PostMessage(String messageText) {
		this.messageText = messageText;
	}

	public String getMessageText() {
		return messageText;
	}
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
}
